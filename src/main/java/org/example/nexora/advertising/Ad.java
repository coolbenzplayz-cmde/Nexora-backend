package org.example.nexora.advertising;

import jakarta.persistence.*;
import org.example.nexora.common.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    public Ad() {
    }

    public Ad(String title, String description, String advertiserId) {
        this.title = title;
        this.description = description;
        this.advertiserId = advertiserId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public AdStatus getStatus() {
        return status;
    }

    public void setStatus(AdStatus status) {
        this.status = status;
    }

    public AdFormat getFormat() {
        return format;
    }

    public void setFormat(AdFormat format) {
        this.format = format;
    }

    public String getAdvertiserId() {
        return advertiserId;
    }

    public void setAdvertiserId(String advertiserId) {
        this.advertiserId = advertiserId;
    }

    public String getTargetAudience() {
        return targetAudience;
    }

    public void setTargetAudience(String targetAudience) {
        this.targetAudience = targetAudience;
    }

    public Integer getImpressions() {
        return impressions;
    }

    public void setImpressions(Integer impressions) {
        this.impressions = impressions;
    }

    public Integer getClicks() {
        return clicks;
    }

    public void setClicks(Integer clicks) {
        this.clicks = clicks;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }

    public BigDecimal getSpent() {
        return spent;
    }

    public void setSpent(BigDecimal spent) {
        this.spent = spent;
    }

    public BigDecimal getCostPerClick() {
        return costPerClick;
    }

    public void setCostPerClick(BigDecimal costPerClick) {
        this.costPerClick = costPerClick;
    }

    public BigDecimal getCostPerImpression() {
        return costPerImpression;
    }

    public void setCostPerImpression(BigDecimal costPerImpression) {
        this.costPerImpression = costPerImpression;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public Integer getDailyBudget() {
        return dailyBudget;
    }

    public void setDailyBudget(Integer dailyBudget) {
        this.dailyBudget = dailyBudget;
    }

    public Integer getReachLimit() {
        return reachLimit;
    }

    public void setReachLimit(Integer reachLimit) {
        this.reachLimit = reachLimit;
    }

    public void incrementImpressions() {
        this.impressions++;
    }

    public void incrementClicks() {
        this.clicks++;
    }
}
