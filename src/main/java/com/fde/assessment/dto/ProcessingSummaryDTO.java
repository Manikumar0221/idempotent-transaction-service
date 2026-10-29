package com.fde.assessment.dto;

import java.util.Map;

public record ProcessingSummaryDTO(
        long totalRequests,
        Map<String, Long> statusCounts,
        Map<String, String> accountBalances
) {}