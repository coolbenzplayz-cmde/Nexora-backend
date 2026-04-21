package org.example.nexora.transfer;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.example.nexora.common.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "peer_to_peer_transfers", indexes = {
        @Index(name = "idx_p2p_sender_id", columnList = "senderId"),
        @Index(name = "idx_p2p_receiver_id", columnList = "receiverId"),
        @Index(name = "idx_p2p_reference", columnList = "reference"),
        @Index(name = "idx_p2p_status", columnList = "status"),
        @Index(name = "idx_p2p_created_at", columnList = "createdAt")
})
public class PeerToPeerTransfer extends BaseEntity {

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "reference", nullable = false, unique = true)
    private String reference;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransferStatus status = TransferStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "transfer_type", nullable = false)
    private TransferType transferType = TransferType.INSTANT;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "fee_amount", precision = 19, scale = 4)
    private BigDecimal feeAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", precision = 19, scale = 4)
    private BigDecimal totalAmount;

    @Column(name = "is_recurring", nullable = false)
    private Boolean isRecurring = false;

    @Column(name = "recurring_interval")
    private String recurringInterval; // DAILY, WEEKLY, MONTHLY

    @Column(name = "next_recurring_at")
    private LocalDateTime nextRecurringAt;

    @Column(name = "recurring_count")
    private Integer recurringCount = 0;

    @Column(name = "max_recurring_count")
    private Integer maxRecurringCount;

    @Column(name = "sender_balance_before", precision = 19, scale = 4)
    private BigDecimal senderBalanceBefore;

    @Column(name = "sender_balance_after", precision = 19, scale = 4)
    private BigDecimal senderBalanceAfter;

    @Column(name = "receiver_balance_before", precision = 19, scale = 4)
    private BigDecimal receiverBalanceBefore;

    @Column(name = "receiver_balance_after", precision = 19, scale = 4)
    private BigDecimal receiverBalanceAfter;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "device_fingerprint")
    private String deviceFingerprint;

    @Column(name = "risk_score")
    private Double riskScore = 0.0;

    @Column(name = "is_flagged", nullable = false)
    private Boolean isFlagged = false;

    @Column(name = "flag_reason")
    private String flagReason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum TransferStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        CANCELLED,
        FLAGGED,
        SCHEDULED
    }

    public enum TransferType {
        INSTANT,
        SCHEDULED,
        RECURRING,
        REQUESTED
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (reference == null) {
            reference = generateReference();
        }
        if (totalAmount == null) {
            totalAmount = amount.add(feeAmount);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void markAsCompleted() {
        this.status = TransferStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void markAsFailed(String reason) {
        this.status = TransferStatus.FAILED;
        this.failedAt = LocalDateTime.now();
        this.failureReason = reason;
    }

    public void markAsCancelled() {
        this.status = TransferStatus.CANCELLED;
    }

    public void markAsFlagged(String reason) {
        this.status = TransferStatus.FLAGGED;
        this.isFlagged = true;
        this.flagReason = reason;
    }

    public void markAsScheduled() {
        this.status = TransferStatus.SCHEDULED;
    }

    public void markAsProcessing() {
        this.status = TransferStatus.PROCESSING;
    }

    public void setRiskScore(double score) {
        this.riskScore = score;
        if (score > 0.7) {
            markAsFlagged("High risk score: " + score);
        }
    }

    public boolean isCompleted() {
        return status == TransferStatus.COMPLETED;
    }

    public boolean isFailed() {
        return status == TransferStatus.FAILED;
    }

    public boolean isPending() {
        return status == TransferStatus.PENDING;
    }

    public boolean isFlagged() {
        return status == TransferStatus.FLAGGED;
    }

    public boolean isScheduled() {
        return status == TransferStatus.SCHEDULED;
    }

    public boolean canBeProcessed() {
        return status == TransferStatus.PENDING || status == TransferStatus.SCHEDULED;
    }

    public boolean isRecurringTransfer() {
        return isRecurring && recurringInterval != null;
    }

    public void scheduleNextRecurring() {
        if (!isRecurringTransfer()) return;

        LocalDateTime next = calculateNextRecurringDate();
        if (next != null) {
            this.nextRecurringAt = next;
            this.recurringCount++;
            
            // Check if we've reached the maximum recurring count
            if (maxRecurringCount != null && recurringCount >= maxRecurringCount) {
                this.isRecurring = false;
                this.nextRecurringAt = null;
            }
        }
    }

    private LocalDateTime calculateNextRecurringDate() {
        if (nextRecurringAt == null) {
            return LocalDateTime.now().plusDays(1); // Default to tomorrow
        }

        switch (recurringInterval.toUpperCase()) {
            case "DAILY":
                return nextRecurringAt.plusDays(1);
            case "WEEKLY":
                return nextRecurringAt.plusWeeks(1);
            case "MONTHLY":
                return nextRecurringAt.plusMonths(1);
            default:
                return null;
        }
    }

    private String generateReference() {
        return "P2P-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
    }

    public boolean isHighRisk() {
        return riskScore > 0.7 || isFlagged;
    }

    public boolean requiresManualReview() {
        return isHighRisk() || amount.compareTo(new BigDecimal("10000")) > 0;
    }
}
