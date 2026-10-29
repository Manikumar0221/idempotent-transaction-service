package com.fde.assessment.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "account_balances")
public class AccountBalance {

    @Id
    private String accountId;

    private BigDecimal balance = BigDecimal.ZERO;
    private Long lastProcessedSequence = 0L;
    private LocalDateTime lastUpdated = LocalDateTime.now();

    public AccountBalance() {}

    public AccountBalance(String accountId, BigDecimal balance) {
        this.accountId = accountId;
        this.balance = balance;
    }

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) {
        this.balance = balance;
        this.lastUpdated = LocalDateTime.now();
    }

    public Long getLastProcessedSequence() { return lastProcessedSequence; }
    public void setLastProcessedSequence(Long lastProcessedSequence) { this.lastProcessedSequence = lastProcessedSequence; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
}