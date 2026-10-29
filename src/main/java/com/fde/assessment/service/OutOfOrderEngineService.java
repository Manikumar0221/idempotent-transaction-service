package com.fde.assessment.service;

import com.fde.assessment.dto.TransactionRequestDTO;
import com.fde.assessment.model.AccountBalance;
import com.fde.assessment.model.TransactionEnums.TransactionStatus;
import com.fde.assessment.model.TransactionRecord;
import com.fde.assessment.repository.TransactionRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OutOfOrderEngineService {

    private static final Logger log = LoggerFactory.getLogger(OutOfOrderEngineService.class);
    private final TransactionRecordRepository txnRepository;

    public OutOfOrderEngineService(TransactionRecordRepository txnRepository) {
        this.txnRepository = txnRepository;
    }

    public boolean isOutOfOrder(TransactionRequestDTO request, AccountBalance balance) {
        if (request.sequenceNumber() == null) return false;
        long expectedNext = balance.getLastProcessedSequence() + 1;
        return request.sequenceNumber() > expectedNext;
    }

    public void drainPendingTransactions(String accountId, TransactionProcessorService processorService) {
        List<TransactionRecord> pendingList = txnRepository.findByAccountIdAndStatusOrderBySequenceNumberAsc(
                accountId, TransactionStatus.PENDING_ORDER);

        for (TransactionRecord pending : pendingList) {
            TransactionRequestDTO dto = new TransactionRequestDTO(
                    pending.getTransactionId(),
                    pending.getRequestId(),
                    pending.getSequenceNumber(),
                    pending.getAccountId(),
                    pending.getTransactionType().name(),
                    pending.getAmount(),
                    pending.getCurrency()
            );
            log.info("Draining pending out-of-order transaction: txnId={}", pending.getTransactionId());
            txnRepository.delete(pending);
            processorService.processSingle(dto);
        }
    }
}