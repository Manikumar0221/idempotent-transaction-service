package com.fde.assessment.dto;

import com.fde.assessment.model.TransactionEnums.TransactionStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponseDTO(
        String transactionId,
        String requestId,
        Long sequenceNumber,
        String accountId,
        String transactionType,
        BigDecimal amount,
        TransactionStatus status,
        int retryCount,
        String errorMessage,
        LocalDateTime updatedAt
) {}