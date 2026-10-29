package com.fde.assessment;

import com.fde.assessment.dto.TransactionRequestDTO;
import com.fde.assessment.dto.TransactionResponseDTO;
import com.fde.assessment.model.AccountBalance;
import com.fde.assessment.model.TransactionEnums.ProcessingStatus;
import com.fde.assessment.model.TransactionEnums.TransactionType;
import com.fde.assessment.model.TransactionRecord;
import com.fde.assessment.repository.AccountBalanceRepository;
import com.fde.assessment.repository.TransactionRecordRepository;
import com.fde.assessment.service.TransactionIngestionService;
import com.fde.assessment.service.TransactionProcessorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
public class TransactionServiceTests {

    @Autowired
    private TransactionIngestionService ingestionService;

    @Autowired
    private TransactionProcessorService processorService;

    @Autowired
    private TransactionRecordRepository transactionRepository;

    @Autowired
    private AccountBalanceRepository accountRepository;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("Scenario 1 & 2: Out-of-order sequence resolution upon predecessor arrival")
    void testOutOfOrderSequenceResolution() {
        TransactionRequestDTO req2 = TransactionRequestDTO.builder()
                .transactionId("TXN-2002").requestId("REQ-2").sequenceNumber(2L)
                .accountId("ACC-201").transactionType(TransactionType.DEBIT)
                .amount(new BigDecimal("40.00")).currency("USD").build();

        TransactionResponseDTO res2 = ingestionService.processBatch(List.of(req2)).get(0);
        assertEquals(ProcessingStatus.PENDING_ORDER, res2.getStatus());

        TransactionRequestDTO req1 = TransactionRequestDTO.builder()
                .transactionId("TXN-2001").requestId("REQ-1").sequenceNumber(1L)
                .accountId("ACC-201").transactionType(TransactionType.CREDIT)
                .amount(new BigDecimal("100.00")).currency("USD").build();

        TransactionResponseDTO res1 = ingestionService.processBatch(List.of(req1)).get(0);
        assertEquals(ProcessingStatus.PROCESSED, res1.getStatus());

        AccountBalance account = accountRepository.findById("ACC-201").orElseThrow();
        assertEquals(0, new BigDecimal("60.00").compareTo(account.getBalance()));
        assertEquals(3L, account.getExpectedNextSequence());
    }

    @Test
    @DisplayName("Scenario 3: Safely ignores duplicate request IDs")
    void testDuplicateDetection() {
        TransactionRequestDTO req1 = TransactionRequestDTO.builder()
                .transactionId("TXN-2001").requestId("REQ-1").sequenceNumber(1L)
                .accountId("ACC-201").transactionType(TransactionType.CREDIT)
                .amount(new BigDecimal("100.00")).currency("USD").build();

        ingestionService.processBatch(List.of(req1));

        TransactionResponseDTO resDup = ingestionService.processBatch(List.of(req1)).get(0);
        assertEquals(ProcessingStatus.DUPLICATE, resDup.getStatus());

        AccountBalance account = accountRepository.findById("ACC-201").orElseThrow();
        assertEquals(0, new BigDecimal("100.00").compareTo(account.getBalance()));
    }

    @Test
    @DisplayName("Scenario 5: Rejects invalid transaction amounts")
    void testInvalidAmountValidation() {
        TransactionRequestDTO req = TransactionRequestDTO.builder()
                .transactionId("TXN-2004").requestId("REQ-4").sequenceNumber(1L)
                .accountId("ACC-202").transactionType(TransactionType.DEBIT)
                .amount(new BigDecimal("-10.00")).currency("USD").build();

        TransactionResponseDTO res = ingestionService.processBatch(List.of(req)).get(0);
        assertEquals(ProcessingStatus.FAILED, res.getStatus());
    }

    @Test
    @DisplayName("Scenario 6: Scheduled retry resolves transient error")
    void testTransientFailureRetry() {
        processorService.registerSimulatedTransientFailure("REQ-5");

        TransactionRequestDTO req = TransactionRequestDTO.builder()
                .transactionId("TXN-2005").requestId("REQ-5").sequenceNumber(1L)
                .accountId("ACC-203").transactionType(TransactionType.CREDIT)
                .amount(new BigDecimal("500.00")).currency("USD").build();

        TransactionResponseDTO res = ingestionService.processBatch(List.of(req)).get(0);
        assertEquals(ProcessingStatus.RETRY_PENDING, res.getStatus());

        processorService.retryTransientFailures();

        TransactionRecord record = transactionRepository.findByRequestId("REQ-5").orElseThrow();
        assertEquals(ProcessingStatus.PROCESSED, record.getStatus());

        AccountBalance account = accountRepository.findById("ACC-203").orElseThrow();
        assertEquals(0, new BigDecimal("500.00").compareTo(account.getBalance()));
    }
}