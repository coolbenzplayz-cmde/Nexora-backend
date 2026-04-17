package org.example.nexora.transaction;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity(name = "LedgerTransaction")
@Table(name = "ledger_transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String reference;

    private Long senderId;

    private Long receiverId;

    private BigDecimal amount;

    private String status;

    private LocalDateTime createdAt = LocalDateTime.now();

    public Transaction() {}

    public Transaction(String reference,
                       Long senderId,
                       Long receiverId,
                       BigDecimal amount,
                       String status) {
        this.reference = reference;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.amount = amount;
        this.status = status;
    }

    public Long getId() { return id; }

    public String getReference() { return reference; }

    public Long getSenderId() { return senderId; }

    public Long getReceiverId() { return receiverId; }

    public BigDecimal getAmount() { return amount; }

    public String getStatus() { return status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}