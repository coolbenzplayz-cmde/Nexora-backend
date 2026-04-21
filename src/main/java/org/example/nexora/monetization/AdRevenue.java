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
@Table(name = "ad_revenue", indexes = {
        @Index(name = "idx_ad_revenue_creator_id", columnList = "creatorId"),
        @Index(name = "idx_ad_revenue_video_id", columnList = "videoId"),
        @Index(name = "idx_ad_revenue_date", columnList = "revenueDate"),
        @Index(name = "idx_ad_revenue_status", columnList = "status")
})
public class AdRevenue extends BaseEntity {

    @Column(name = "creator_id", nullable = false)
    private Long creatorId;

    @Column(name = "video_id")
    private Long videoId;

    @Column(name = "ad_campaign_id")
    private String adCampaignId;

    @Enumerated(EnumType.STRING)
    @Column(name = "ad_type", nullable = false)
    private AdType adType;

    @Enumerated(EnumType.STRING)
    @Column(name = "ad_position", nullable = false)
    private AdPosition adPosition;

    @Column(name = "impressions", nullable = false)
    private Long impressions = 0L;

    @Column(name = "clicks", nullable = false)
    private Long clicks = 0L;

    @Column(name = "revenue_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal revenueAmount;

    @Column(name = "cpm_rate", precision = 19, scale = 4)
    private BigDecimal cpmRate;

    @Column(name = "cpc_rate", precision = 19, scale = 4)
    private BigDecimal cpcRate;

    @Column(name = "revenue_date", nullable = false)
    private LocalDateTime revenueDate = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RevenueStatus status = RevenueStatus.PENDING;

    @Column(name = "is_paid", nullable = false)
    private Boolean isPaid = false;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "payment_reference")
    private String paymentReference;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum AdType {
        PRE_ROLL,         // Before video
        MID_ROLL,         // During video
        POST_ROLL,        // After video
        BANNER,           // Banner ad
        OVERLAY,          // Overlay ad
        SPONSORED_CONTENT // Sponsored content
    }

    public enum AdPosition {
        TOP,              // Top of screen
        BOTTOM,           // Bottom of screen
        LEFT,             // Left side
        RIGHT,            // Right side
        CENTER,           // Center overlay
        FULL_SCREEN       // Full screen
    }

    public enum RevenueStatus {
        PENDING,          // Revenue pending verification
        VERIFIED,         // Revenue verified
        PAID,            // Revenue paid to creator
        DISPUTED,        // Revenue under dispute
        CANCELLED        // Revenue cancelled
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (revenueDate == null) {
            revenueDate = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void calculateCpmRevenue(BigDecimal cpmRate) {
        this.cpmRate = cpmRate;
        // Revenue = (impressions / 1000) * CPM
        this.revenueAmount = BigDecimal.valueOf(impressions)
                .divide(BigDecimal.valueOf(1000), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(cpmRate);
    }

    public void calculateCpcRevenue(BigDecimal cpcRate) {
        this.cpcRate = cpcRate;
        // Revenue = clicks * CPC
        this.revenueAmount = BigDecimal.valueOf(clicks).multiply(cpcRate);
    }

    public void incrementImpressions(long count) {
        this.impressions += count;
        recalculateRevenue();
    }

    public void incrementClicks(long count) {
        this.clicks += count;
        recalculateRevenue();
    }

    private void recalculateRevenue() {
        if (cpmRate != null) {
            calculateCpmRevenue(cpmRate);
        } else if (cpcRate != null) {
            calculateCpcRevenue(cpcRate);
        }
    }

    public void markAsVerified() {
        this.status = RevenueStatus.VERIFIED;
    }

    public void markAsPaid(String paymentReference) {
        this.status = RevenueStatus.PAID;
        this.isPaid = true;
        this.paidAt = LocalDateTime.now();
        this.paymentReference = paymentReference;
    }

    public void markAsDisputed() {
        this.status = RevenueStatus.DISPUTED;
    }

    public double getClickThroughRate() {
        if (impressions == 0) return 0.0;
        return (double) clicks / impressions * 100.0;
    }

    public boolean isValidForPayment() {
        return status == RevenueStatus.VERIFIED && !isPaid && revenueAmount.compareTo(BigDecimal.ZERO) > 0;
    }
}
