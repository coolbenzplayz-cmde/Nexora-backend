package org.example.nexora.transaction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
}
