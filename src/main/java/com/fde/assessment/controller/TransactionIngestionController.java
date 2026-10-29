package com.fde.assessment.controller;

import com.fde.assessment.dto.TransactionRequestDTO;
import com.fde.assessment.dto.TransactionResponseDTO;
import com.fde.assessment.service.TransactionIngestionService;
import com.fde.assessment.service.TransactionProcessorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionIngestionController {

    private final TransactionProcessorService processorService;
    private final TransactionIngestionService ingestionService;

    public TransactionIngestionController(TransactionProcessorService processorService,
                                          TransactionIngestionService ingestionService) {
        this.processorService = processorService;
        this.ingestionService = ingestionService;
    }

    @PostMapping
    public ResponseEntity<TransactionResponseDTO> submitTransaction(@Valid @RequestBody TransactionRequestDTO request) {
        TransactionResponseDTO response = processorService.processSingle(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/batch")
    public ResponseEntity<List<TransactionResponseDTO>> submitBatch(@RequestBody List<@Valid TransactionRequestDTO> requests) {
        List<TransactionResponseDTO> responses = ingestionService.processBatch(requests);
        return ResponseEntity.ok(responses);
    }
}