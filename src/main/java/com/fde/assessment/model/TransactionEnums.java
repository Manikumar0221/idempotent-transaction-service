package com.fde.assessment.model;

public class TransactionEnums {

    public enum TransactionType {
        CREDIT, DEBIT, REVERSAL
    }

    public enum TransactionStatus {
        RECEIVED,
        PROCESSING,
        PROCESSED,
        DUPLICATE,
        FAILED,
        RETRY_PENDING,
        PENDING_ORDER
    }
}