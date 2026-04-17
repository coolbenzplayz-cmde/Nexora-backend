package org.example.nexora.transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponse {

    private String reference;
    private Long senderId;
    private Long receiverId;
    private BigDecimal amount;
    private String status;
    private LocalDateTime createdAt;

    public TransactionResponse(Transaction tx) {
        this.reference = tx.getReference();
        this.senderId = tx.getSenderId();
        this.receiverId = tx.getReceiverId();
        this.amount = tx.getAmount();
        this.status = tx.getStatus();
        this.createdAt = tx.getCreatedAt();
    }

    public String getReference() { return reference; }
    public Long getSenderId() { return senderId; }
    public Long getReceiverId() { return receiverId; }
    public BigDecimal getAmount() { return amount; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}