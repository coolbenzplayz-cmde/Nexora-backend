package org.example.nexora.monetization;

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
@Table(name = "creator_earnings", indexes = {
        @Index(name = "idx_creator_earnings_creator_id", columnList = "creatorId"),
        @Index(name = "idx_creator_earnings_video_id", columnList = "videoId"),
        @Index(name = "idx_creator_earnings_date", columnList = "earningsDate"),
        @Index(name = "idx_creator_earnings_type", columnList = "earningsType")
})
public class CreatorEarnings extends BaseEntity {

    @Column(name = "creator_id", nullable = false)
    private Long creatorId;

    @Column(name = "video_id")
    private Long videoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "earnings_type", nullable = false)
    private EarningsType earningsType;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "earnings_date", nullable = false)
    private LocalDateTime earningsDate = LocalDateTime.now();

    @Column(name = "description")
    private String description;

    @Column(name = "is_paid", nullable = false)
    private Boolean isPaid = false;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "payment_reference")
    private String paymentReference;

    @Column(name = "view_count")
    private Long viewCount = 0L;

    @Column(name = "engagement_rate")
    private Double engagementRate = 0.0;

    @Column(name = "cpm_rate", precision = 19, scale = 4)
    private BigDecimal cpmRate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum EarningsType {
        VIDEO_VIEWS,      // Earnings from video views
        AD_REVENUE,       // Earnings from advertisements
        SUBSCRIPTION,     // Earnings from user subscriptions
        DONATIONS,        // Direct donations from users
        SPONSORSHIP,      // Sponsorship deals
        MERCHANDISE,      // Merchandise sales
        AFFILIATE,        // Affiliate marketing
        BONUS,           // Platform bonuses
        REFUND          // Refunded earnings
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (earningsDate == null) {
            earningsDate = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void markAsPaid(String paymentReference) {
        this.isPaid = true;
        this.paidAt = LocalDateTime.now();
        this.paymentReference = paymentReference;
    }

    public void calculateEarningsFromViews(long views, BigDecimal cpmRate) {
        this.viewCount = views;
        this.cpmRate = cpmRate;
        // Earnings = (views / 1000) * CPM
        this.amount = BigDecimal.valueOf(views)
                .divide(BigDecimal.valueOf(1000), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(cpmRate);
    }

    public void calculateEarningsFromEngagement(long views, double engagementRate, BigDecimal baseRate) {
        this.viewCount = views;
        this.engagementRate = engagementRate;
        // Bonus earnings based on engagement
        BigDecimal engagementMultiplier = BigDecimal.valueOf(1.0 + (engagementRate * 0.5));
        this.amount = baseRate.multiply(engagementMultiplier);
    }
}
