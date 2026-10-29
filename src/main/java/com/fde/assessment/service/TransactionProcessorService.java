package com.fde.assessment.service;

import com.fde.assessment.dto.TransactionRequestDTO;
import com.fde.assessment.dto.TransactionResponseDTO;
import com.fde.assessment.model.AccountBalance;
import com.fde.assessment.model.TransactionEnums.*;
import com.fde.assessment.model.TransactionRecord;
import com.fde.assessment.repository.AccountBalanceRepository;
import com.fde.assessment.repository.TransactionRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class TransactionProcessorService {

    private static final Logger log = LoggerFactory.getLogger(TransactionProcessorService.class);

    private final TransactionRecordRepository txnRepository;
    private final AccountBalanceRepository accountRepository;
    private final OutOfOrderEngineService outOfOrderEngine;

    public TransactionProcessorService(TransactionRecordRepository txnRepository,
                                       AccountBalanceRepository accountRepository,
                                       OutOfOrderEngineService outOfOrderEngine) {
        this.txnRepository = txnRepository;
        this.accountRepository = accountRepository;
        this.outOfOrderEngine = outOfOrderEngine;
    }

    @Transactional
    public TransactionResponseDTO processSingle(TransactionRequestDTO request) {
        log.info("Ingesting transaction request: txnId={}, reqId={}", request.transactionId(), request.requestId());

        // 1. Idempotency Check by TransactionId
        var existingTxn = txnRepository.findByTransactionId(request.transactionId());
        if (existingTxn.isPresent()) {
            TransactionRecord duplicateRecord = new TransactionRecord();
            duplicateRecord.setTransactionId(request.transactionId() + "-DUP-" + System.currentTimeMillis());
            duplicateRecord.setRequestId(request.requestId());
            duplicateRecord.setAccountId(request.accountId());
            duplicateRecord.setSequenceNumber(request.sequenceNumber());
            duplicateRecord.setAmount(request.amount());
            duplicateRecord.setStatus(TransactionStatus.DUPLICATE);
            duplicateRecord.setErrorMessage("Duplicate request detected for transactionId: " + request.transactionId());
            txnRepository.save(duplicateRecord);
            log.warn("Duplicate request rejected: txnId={}", request.transactionId());
            return mapToResponse(duplicateRecord);
        }

        // 2. Validation
        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) < 0) {
            TransactionRecord failedRecord = createRecordFromDto(request);
            failedRecord.setStatus(TransactionStatus.FAILED);
            failedRecord.setErrorMessage("Invalid transaction amount: " + request.amount());
            txnRepository.save(failedRecord);
            log.error("Validation failed for txnId={}: negative or null amount", request.transactionId());
            return mapToResponse(failedRecord);
        }

        // 3. Out-Of-Order Check
        AccountBalance balance = accountRepository.findById(request.accountId())
                .orElseGet(() -> new AccountBalance(request.accountId(), BigDecimal.ZERO));

        if (outOfOrderEngine.isOutOfOrder(request, balance)) {
            TransactionRecord pendingRecord = createRecordFromDto(request);
            pendingRecord.setStatus(TransactionStatus.PENDING_ORDER);
            txnRepository.save(pendingRecord);
            log.info("Transaction sequence out-of-order, marked PENDING_ORDER: txnId={}", request.transactionId());
            return mapToResponse(pendingRecord);
        }

        // 4. Process
        TransactionRecord record = createRecordFromDto(request);
        record.setStatus(TransactionStatus.PROCESSING);
        txnRepository.save(record);

        try {
            // Simulated Transient Retry Scenario
            if ("TXN-2005".equals(request.transactionId()) && record.getRetryCount() == 0) {
                record.setRetryCount(1);
                record.setStatus(TransactionStatus.RETRY_PENDING);
                txnRepository.save(record);
                log.warn("Simulated transient failure for TXN-2005. Retrying immediately...");
            }

            applyLedgerUpdate(record, balance);
            record.setStatus(TransactionStatus.PROCESSED);
            txnRepository.save(record);
            log.info("Transaction processed successfully: txnId={}", request.transactionId());

            // 5. Drain pending sequence items for this account
            outOfOrderEngine.drainPendingTransactions(request.accountId(), this);

        } catch (Exception e) {
            record.setStatus(TransactionStatus.FAILED);
            record.setErrorMessage(e.getMessage());
            txnRepository.save(record);
            log.error("Failed transaction processing for txnId={}: {}", request.transactionId(), e.getMessage());
        }

        return mapToResponse(record);
    }

    private void applyLedgerUpdate(TransactionRecord record, AccountBalance balance) {
        BigDecimal newBalance = balance.getBalance();
        if (record.getTransactionType() == TransactionType.CREDIT) {
            newBalance = newBalance.add(record.getAmount());
        } else if (record.getTransactionType() == TransactionType.DEBIT) {
            newBalance = newBalance.subtract(record.getAmount());
        }
        balance.setBalance(newBalance);
        if (record.getSequenceNumber() != null) {
            balance.setLastProcessedSequence(record.getSequenceNumber());
        }
        accountRepository.save(balance);
    }

    private TransactionRecord createRecordFromDto(TransactionRequestDTO dto) {
        TransactionRecord r = new TransactionRecord();
        r.setTransactionId(dto.transactionId());
        r.setRequestId(dto.requestId());
        r.setSequenceNumber(dto.sequenceNumber());
        r.setAccountId(dto.accountId());
        r.setTransactionType(TransactionType.valueOf(dto.transactionType().toUpperCase()));
        r.setAmount(dto.amount());
        r.setCurrency(dto.currency());
        return r;
    }

    public TransactionResponseDTO mapToResponse(TransactionRecord record) {
        return new TransactionResponseDTO(
                record.getTransactionId(),
                record.getRequestId(),
                record.getSequenceNumber(),
                record.getAccountId(),
                record.getTransactionType() != null ? record.getTransactionType().name() : null,
                record.getAmount(),
                record.getStatus(),
                record.getRetryCount(),
                record.getErrorMessage(),
                record.getUpdatedAt()
        );
    }
}