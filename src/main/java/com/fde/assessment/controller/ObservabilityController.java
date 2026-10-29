package com.fde.assessment.controller;

import com.fde.assessment.dto.ProcessingSummaryDTO;
import com.fde.assessment.service.TransactionIngestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/observability")
public class ObservabilityController {

    private final TransactionIngestionService ingestionService;

    public ObservabilityController(TransactionIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @GetMapping("/summary")
    public ResponseEntity<ProcessingSummaryDTO> getSummary() {
        return ResponseEntity.ok(ingestionService.getSummary());
    }
}