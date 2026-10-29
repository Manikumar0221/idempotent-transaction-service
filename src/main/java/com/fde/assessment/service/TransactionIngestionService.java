package com.fde.assessment.service;

import com.fde.assessment.dto.ProcessingSummaryDTO;
import com.fde.assessment.dto.TransactionRequestDTO;
import com.fde.assessment.dto.TransactionResponseDTO;
import com.fde.assessment.model.AccountBalance;
import com.fde.assessment.model.TransactionEnums.TransactionStatus;
import com.fde.assessment.repository.AccountBalanceRepository;
import com.fde.assessment.repository.TransactionRecordRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TransactionIngestionService {

    private final TransactionProcessorService processorService;
    private final TransactionRecordRepository txnRepository;
    private final AccountBalanceRepository accountRepository;

    public TransactionIngestionService(TransactionProcessorService processorService,
                                       TransactionRecordRepository txnRepository,
                                       AccountBalanceRepository accountRepository) {
        this.processorService = processorService;
        this.txnRepository = txnRepository;
        this.accountRepository = accountRepository;
    }

    public List<TransactionResponseDTO> processBatch(List<TransactionRequestDTO> requests) {
        List<TransactionResponseDTO> responses = new ArrayList<>();
        for (TransactionRequestDTO req : requests) {
            responses.add(processorService.processSingle(req));
        }
        return responses;
    }

    public ProcessingSummaryDTO getSummary() {
        var records = txnRepository.findAll();
        Map<String, Long> statusCounts = records.stream()
                .collect(Collectors.groupingBy(r -> r.getStatus().name(), Collectors.counting()));

        // Ensure all statuses exist in output map
        for (TransactionStatus status : TransactionStatus.values()) {
            statusCounts.putIfAbsent(status.name(), 0L);
        }

        Map<String, String> balances = accountRepository.findAll().stream()
                .collect(Collectors.toMap(AccountBalance::getAccountId, b -> b.getBalance().toString()));

        return new ProcessingSummaryDTO(records.size(), statusCounts, balances);
    }
}