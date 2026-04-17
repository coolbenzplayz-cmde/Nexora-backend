package org.example.nexora.payment;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.example.nexora.common.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Wallet / payment ledger row.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "PaymentTransaction")
@Table(name = "transactions", indexes = {
        @Index(name = "idx_transactions_wallet_id", columnList = "wallet_id"),
        @Index(name = "idx_transactions_user_id", columnList = "user_id"),
        @Index(name = "idx_transactions_type", columnList = "type"),
        @Index(name = "idx_transactions_status", columnList = "status")
})
public class Transaction extends BaseEntity {

    @Column(name = "wallet_id", nullable = false)
    private Long walletId;

    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency = "KES";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(name = "reference_code")
    private String referenceCode;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public enum TransactionType {
        DEPOSIT,
        WITHDRAWAL,
        TRANSFER,
        TRANSFER_RECEIVED,
        PAYMENT,
        REFUND,
        TIP,
        SUBSCRIPTION
    }

    public enum TransactionStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        CANCELLED
    }
}
