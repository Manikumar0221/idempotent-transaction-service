package com.fde.assessment.repository;

import com.fde.assessment.model.TransactionRecord;
import com.fde.assessment.model.TransactionEnums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRecordRepository extends JpaRepository<TransactionRecord, Long> {
    Optional<TransactionRecord> findByTransactionId(String transactionId);
    Optional<TransactionRecord> findByRequestId(String requestId);
    List<TransactionRecord> findByAccountIdAndStatusOrderBySequenceNumberAsc(String accountId, TransactionStatus status);
}