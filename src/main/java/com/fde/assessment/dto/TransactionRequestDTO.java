package com.fde.assessment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record TransactionRequestDTO(
        @NotBlank(message = "transactionId is mandatory") String transactionId,
        @NotBlank(message = "requestId is mandatory") String requestId,
        Long sequenceNumber,
        @NotBlank(message = "accountId is mandatory") String accountId,
        @NotBlank(message = "transactionType is mandatory") String transactionType,
        @NotNull(message = "amount is mandatory") BigDecimal amount,
        String currency
) {}