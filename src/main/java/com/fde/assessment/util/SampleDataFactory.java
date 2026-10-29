package com.fde.assessment.util;

import com.fde.assessment.dto.TransactionRequestDTO;
import java.math.BigDecimal;
import java.util.List;

public class SampleDataFactory {

    public static List<TransactionRequestDTO> getL2TestDataSet() {
        return List.of(
                new TransactionRequestDTO("TXN-2002", "REQ-2", 2L, "ACC-201", "DEBIT", BigDecimal.valueOf(40.0), "USD"),
                new TransactionRequestDTO("TXN-2001", "REQ-1", 1L, "ACC-201", "CREDIT", BigDecimal.valueOf(100.0), "USD"),
                new TransactionRequestDTO("TXN-2002", "REQ-2-DUP", 2L, "ACC-201", "DEBIT", BigDecimal.valueOf(40.0), "USD"),
                new TransactionRequestDTO("TXN-2003", "REQ-3", 3L, "ACC-201", "CREDIT", BigDecimal.valueOf(25.0), "USD"),
                new TransactionRequestDTO("TXN-2004", "REQ-4", 4L, "ACC-202", "DEBIT", BigDecimal.valueOf(-10.0), "USD"),
                new TransactionRequestDTO("TXN-2005", "REQ-5", 5L, "ACC-203", "CREDIT", BigDecimal.valueOf(500.0), "USD")
        );
    }
}