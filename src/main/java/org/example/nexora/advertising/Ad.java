package org.example.nexora.advertising;

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
@Table(name = "advertisements")
public class Ad extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column
    private String imageUrl;

    @Column
    private String targetUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdStatus status = AdStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdFormat format = AdFormat.BANNER;

    @Column(nullable = false)
    private String advertiserId;

    @Column
    private String targetAudience;

    @Column
    private Integer impressions = 0;

    @Column
    private Integer clicks = 0;

    @Column
    private BigDecimal budget;

    @Column
    private BigDecimal spent;

    @Column
    private BigDecimal costPerClick;

    @Column
    private BigDecimal costPerImpression;

    @Column
    private LocalDateTime startDate;

    @Column
    private LocalDateTime endDate;

    @Column
    private Integer dailyBudget;

    @Column
    private Integer reachLimit;

    public enum AdStatus {
        DRAFT, PENDING, ACTIVE, PAUSED, COMPLETED, REJECTED
    }

    public enum AdFormat {
        BANNER, VIDEO, SPONSORED_POST, INTERSTITIAL, NATIVE
    }

    public void incrementImpressions() {
        this.impressions++;
    }

    public void incrementClicks() {
        this.clicks++;
    }
}
