package org.example.nexora.wallet;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class WithdrawRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private BigDecimal amount;

    private String status; // APPROVED, REJECTED, PENDING_REVIEW

    private LocalDateTime createdAt = LocalDateTime.now();

    public WithdrawRequest() {}

    public WithdrawRequest(Long userId, BigDecimal amount, String status) {
        this.userId = userId;
        this.amount = amount;
        this.status = status;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public BigDecimal getAmount() { return amount; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setStatus(String status) { this.status = status; }
}