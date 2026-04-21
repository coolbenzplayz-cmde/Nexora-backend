package org.example.nexora.analytics;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.example.nexora.video.Video;
import org.example.nexora.user.User;
import org.example.nexora.social.Comment;
import org.example.nexora.social.Follow;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Comprehensive Creator Analytics Dashboard providing:
 * - Performance metrics and KPIs
 * - Audience insights and demographics
 * - Content performance analysis
 * - Engagement patterns and trends
 * - Revenue and earnings tracking
 * - Growth analytics and predictions
 */
@Slf4j
public class CreatorAnalytics {

    /**
     * Generate comprehensive analytics dashboard for a creator
     */
    public CreatorAnalyticsDashboard generateAnalytics(Long creatorId, AnalyticsRequest request) {
        log.info("Generating comprehensive analytics for creator {}", creatorId);

        CreatorAnalyticsDashboard dashboard = new CreatorAnalyticsDashboard();
        dashboard.setCreatorId(creatorId);
        dashboard.setGeneratedAt(LocalDateTime.now());
        dashboard.setDateRange(request.getDateRange());

        // Get creator's data
        User creator = request.getCreator();
        List<Video> videos = request.getVideos();
        List<Comment> comments = request.getComments();
        List<Follow> followers = request.getFollowers();

        // Performance Overview
        PerformanceOverview performance = calculatePerformanceOverview(videos, comments, followers, request);
        dashboard.setPerformanceOverview(performance);

        // Audience Insights
        AudienceInsights audience = calculateAudienceInsights(followers, request);
        dashboard.setAudienceInsights(audience);

        // Content Performance
        ContentPerformance content = calculateContentPerformance(videos, comments, request);
        dashboard.setContentPerformance(content);

        // Engagement Analytics
        EngagementAnalytics engagement = calculateEngagementAnalytics(videos, comments, followers, request);
        dashboard.setEngagementAnalytics(engagement);

        // Revenue Analytics
        RevenueAnalytics revenue = calculateRevenueAnalytics(videos, creator, request);
        dashboard.setRevenueAnalytics(revenue);

        // Growth Analytics
        GrowthAnalytics growth = calculateGrowthAnalytics(followers, videos, request);
        dashboard.setGrowthAnalytics(growth);

        // Top Performing Content
        List<VideoPerformance> topVideos = calculateTopVideos(videos, comments, request);
        dashboard.setTopVideos(topVideos);

        // Time-based Analytics
        TimeBasedAnalytics timeAnalytics = calculateTimeBasedAnalytics(videos, comments, followers, request);
        dashboard.setTimeBasedAnalytics(timeAnalytics);

        return dashboard;
    }

    /**
     * Calculate overall performance overview
     */
    private PerformanceOverview calculatePerformanceOverview(List<Video> videos, List<Comment> comments, 
                                                          List<Follow> followers, AnalyticsRequest request) {
        PerformanceOverview overview = new PerformanceOverview();

        // Basic metrics
        overview.setTotalVideos(videos.size());
        overview.setTotalViews(videos.stream().mapToLong(Video::getViews).sum());
        overview.setTotalLikes(videos.stream().mapToLong(Video::getLikes).sum());
        overview.setTotalComments(comments.size());
        overview.setTotalShares(videos.stream().mapToLong(Video::getShares).sum());
        overview.setTotalFollowers(followers.size());

        // Engagement metrics
        long totalEngagements = overview.getTotalLikes() + overview.getTotalComments() + overview.getTotalShares();
        overview.setTotalEngagements(totalEngagements);
        
        if (overview.getTotalViews() > 0) {
            overview.setEngagementRate((double) totalEngagements / overview.getTotalViews() * 100);
        }

        // Average metrics per video
        if (videos.size() > 0) {
            overview.setAvgViewsPerVideo((double) overview.getTotalViews() / videos.size());
            overview.setAvgLikesPerVideo((double) overview.getTotalLikes() / videos.size());
            overview.setAvgCommentsPerVideo((double) overview.getTotalComments() / videos.size());
        }

        // Top performing video
        Video topVideo = videos.stream()
                .max(Comparator.comparing(Video::getEngagementScore))
                .orElse(null);
        if (topVideo != null) {
            overview.setTopVideoId(topVideo.getId());
            overview.setTopVideoTitle(topVideo.getTitle());
            overview.setTopVideoViews(topVideo.getViews());
            overview.setTopVideoEngagement(topVideo.getEngagementScore());
        }

        // Growth metrics
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monthAgo = now.minus(1, ChronoUnit.MONTHS);
        LocalDateTime weekAgo = now.minus(1, ChronoUnit.WEEKS);

        long recentFollowers = followers.stream()
                .filter(f -> f.getCreatedAt().isAfter(monthAgo))
                .count();
        long weeklyFollowers = followers.stream()
                .filter(f -> f.getCreatedAt().isAfter(weekAgo))
                .count();

        overview.setMonthlyGrowth(recentFollowers);
        overview.setWeeklyGrowth(weeklyFollowers);

        return overview;
    }

    /**
     * Calculate audience insights and demographics
     */
    private AudienceInsights calculateAudienceInsights(List<Follow> followers, AnalyticsRequest request) {
        AudienceInsights insights = new AudienceInsights();

        // Basic demographics
        insights.setTotalAudience(followers.size());
        insights.setActiveFollowers(calculateActiveFollowers(followers, request));

        // Geographic distribution (simplified - would need location data)
        Map<String, Integer> geographicDistribution = calculateGeographicDistribution(followers);
        insights.setGeographicDistribution(geographicDistribution);

        // Age distribution (simplified - would need age data)
        Map<String, Integer> ageDistribution = calculateAgeDistribution(followers);
        insights.setAgeDistribution(ageDistribution);

        // Gender distribution (simplified - would need gender data)
        Map<String, Integer> genderDistribution = calculateGenderDistribution(followers);
        insights.setGenderDistribution(genderDistribution);

        // Language distribution
        Map<String, Integer> languageDistribution = calculateLanguageDistribution(followers);
        insights.setLanguageDistribution(languageDistribution);

        // Activity patterns
        Map<String, Double> activityByHour = calculateFollowerActivityByHour(followers, request);
        insights.setActivityByHour(activityByHour);

        Map<String, Double> activityByDay = calculateFollowerActivityByDay(followers, request);
        insights.setActivityByDay(activityByDay);

        // Engagement quality
        insights.setEngagementQuality(calculateEngagementQuality(followers, request));
        insights.setLoyaltyScore(calculateLoyaltyScore(followers, request));

        // Audience retention
        insights.setRetentionRate(calculateRetentionRate(followers, request));
        insights.setChurnRate(calculateChurnRate(followers, request));

        return insights;
    }

    /**
     * Calculate content performance metrics
     */
    private ContentPerformance calculateContentPerformance(List<Video> videos, List<Comment> comments, AnalyticsRequest request) {
        ContentPerformance performance = new ContentPerformance();

        // Content categories performance
        Map<String, ContentCategoryMetrics> categoryMetrics = new HashMap<>();
        for (Video video : videos) {
            String category = extractVideoCategory(video);
            ContentCategoryMetrics metrics = categoryMetrics.computeIfAbsent(category, k -> new ContentCategoryMetrics());
            
            metrics.addVideo(video);
            metrics.addComments(comments.stream()
                    .filter(c -> c.getVideoId().equals(video.getId()))
                    .count());
        }
        performance.setCategoryMetrics(categoryMetrics);

        // Content length analysis
        Map<String, Double> lengthPerformance = analyzeContentLengthPerformance(videos);
        performance.setLengthPerformance(lengthPerformance);

        // Upload frequency analysis
        UploadFrequencyAnalysis frequencyAnalysis = analyzeUploadFrequency(videos);
        performance.setFrequencyAnalysis(frequencyAnalysis);

        // Best posting times
        Map<String, Double> bestPostingTimes = analyzeBestPostingTimes(videos, request);
        performance.setBestPostingTimes(bestPostingTimes);

        // Content quality score
        performance.setAverageQualityScore(calculateAverageContentQuality(videos));

        // Viral content analysis
        List<Video> viralVideos = identifyViralContent(videos, request);
        performance.setViralVideos(viralVideos);

        // Underperforming content
        List<Video> underperformingVideos = identifyUnderperformingContent(videos, request);
        performance.setUnderperformingVideos(underperformingVideos);

        return performance;
    }

    /**
     * Calculate engagement analytics
     */
    private EngagementAnalytics calculateEngagementAnalytics(List<Video> videos, List<Comment> comments, 
                                                           List<Follow> followers, AnalyticsRequest request) {
        EngagementAnalytics analytics = new EngagementAnalytics();

        // Engagement breakdown
        analytics.setLikeEngagement(calculateLikeEngagement(videos));
        analytics.setCommentEngagement(calculateCommentEngagement(videos, comments));
        analytics.setShareEngagement(calculateShareEngagement(videos));
        analytics.setFollowEngagement(calculateFollowEngagement(videos, followers));

        // Engagement trends
        Map<LocalDateTime, Double> engagementTrends = calculateEngagementTrends(videos, comments, request);
        analytics.setEngagementTrends(engagementTrends);

        // Peak engagement times
        Map<String, Double> peakEngagementTimes = calculatePeakEngagementTimes(videos, request);
        analytics.setPeakEngagementTimes(peakEngagementTimes);

        // Engagement depth
        analytics.setAverageWatchTime(calculateAverageWatchTime(videos, request));
        analytics.setRepeatViewRate(calculateRepeatViewRate(videos, request));

        // Community engagement
        analytics.setCommunityScore(calculateCommunityScore(videos, comments, followers));
        analytics.setInteractionQuality(calculateInteractionQuality(comments));

        return analytics;
    }

    /**
     * Calculate revenue analytics
     */
    private RevenueAnalytics calculateRevenueAnalytics(List<Video> videos, User creator, AnalyticsRequest request) {
        RevenueAnalytics analytics = new RevenueAnalytics();

        // Total earnings
        BigDecimal totalEarnings = BigDecimal.valueOf(creator.getCreatorEarnings());
        analytics.setTotalEarnings(totalEarnings);

        // Revenue breakdown
        Map<String, BigDecimal> revenueBreakdown = calculateRevenueBreakdown(videos, request);
        analytics.setRevenueBreakdown(revenueBreakdown);

        // Earnings per video
        if (videos.size() > 0) {
            analytics.setEarningsPerVideo(totalEarnings.divide(BigDecimal.valueOf(videos.size()), 2, RoundingMode.HALF_UP));
        }

        // Revenue trends
        Map<LocalDateTime, BigDecimal> revenueTrends = calculateRevenueTrends(videos, request);
        analytics.setRevenueTrends(revenueTrends);

        // Monetization efficiency
        analytics.setMonetizationEfficiency(calculateMonetizationEfficiency(videos, totalEarnings));
        analytics.setRevenuePerView(calculateRevenuePerView(videos, totalEarnings));
        analytics.setRevenuePerFollower(calculateRevenuePerFollower(totalEarnings, request));

        // Revenue predictions
        analytics.setProjectedMonthlyRevenue(calculateProjectedMonthlyRevenue(videos, totalEarnings));
        analytics.setRevenueGrowthRate(calculateRevenueGrowthRate(videos, request));

        return analytics;
    }

    /**
     * Calculate growth analytics
     */
    private GrowthAnalytics calculateGrowthAnalytics(List<Follow> followers, List<Video> videos, AnalyticsRequest request) {
        GrowthAnalytics analytics = new GrowthAnalytics();

        // Follower growth
        Map<LocalDateTime, Integer> followerGrowth = calculateFollowerGrowth(followers, request);
        analytics.setFollowerGrowth(followerGrowth);

        // Growth rate
        analytics.setMonthlyGrowthRate(calculateMonthlyGrowthRate(followers));
        analytics.setWeeklyGrowthRate(calculateWeeklyGrowthRate(followers));
        analytics.setDailyGrowthRate(calculateDailyGrowthRate(followers));

        // Growth projections
        analytics.setProjectedFollowers30Days(calculateProjectedFollowers(followers, 30));
        analytics.setProjectedFollowers90Days(calculateProjectedFollowers(followers, 90));
        analytics.setProjectedFollowers1Year(calculateProjectedFollowers(followers, 365));

        // Growth quality
        analytics.setOrganicGrowthRate(calculateOrganicGrowthRate(followers, request));
        analytics.setPaidGrowthRate(calculatePaidGrowthRate(followers, request));

        // Milestone tracking
        analytics.setNextMilestone(calculateNextMilestone(followers));
        analytics.setMilestoneProgress(calculateMilestoneProgress(followers));

        return analytics;
    }

    /**
     * Calculate top performing videos
     */
    private List<VideoPerformance> calculateTopVideos(List<Video> videos, List<Comment> comments, AnalyticsRequest request) {
        return videos.stream()
                .map(video -> {
                    VideoPerformance performance = new VideoPerformance();
                    performance.setVideo(video);
                    performance.setViews(video.getViews());
                    performance.setLikes(video.getLikes());
                    performance.setComments(comments.stream()
                            .filter(c -> c.getVideoId().equals(video.getId()))
                            .count());
                    performance.setShares(video.getShares());
                    performance.setEngagementScore(video.getEngagementScore());
                    performance.setRevenue(calculateVideoRevenue(video, request));
                    performance.setPerformanceRating(calculateVideoPerformanceRating(video, request));
                    return performance;
                })
                .sorted((a, b) -> Double.compare(b.getEngagementScore(), a.getEngagementScore()))
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * Calculate time-based analytics
     */
    private TimeBasedAnalytics calculateTimeBasedAnalytics(List<Video> videos, List<Comment> comments, 
                                                         List<Follow> followers, AnalyticsRequest request) {
        TimeBasedAnalytics analytics = new TimeBasedAnalytics();

        // Hourly performance
        Map<Integer, Double> hourlyPerformance = calculateHourlyPerformance(videos, request);
        analytics.setHourlyPerformance(hourlyPerformance);

        // Daily performance
        Map<String, Double> dailyPerformance = calculateDailyPerformance(videos, request);
        analytics.setDailyPerformance(dailyPerformance);

        // Weekly performance
        Map<String, Double> weeklyPerformance = calculateWeeklyPerformance(videos, request);
        analytics.setWeeklyPerformance(weeklyPerformance);

        // Monthly performance
        Map<String, Double> monthlyPerformance = calculateMonthlyPerformance(videos, request);
        analytics.setMonthlyPerformance(monthlyPerformance);

        // Seasonal patterns
        Map<String, Double> seasonalPatterns = calculateSeasonalPatterns(videos, request);
        analytics.setSeasonalPatterns(seasonalPatterns);

        return analytics;
    }

    // Helper methods for calculations (simplified implementations)
    private long calculateActiveFollowers(List<Follow> followers, AnalyticsRequest request) {
        // Simplified - would check last activity
        return followers.size();
    }

    private Map<String, Integer> calculateGeographicDistribution(List<Follow> followers) {
        // Simplified - would use location data
        Map<String, Integer> distribution = new HashMap<>();
        distribution.put("United States", followers.size() / 2);
        distribution.put("United Kingdom", followers.size() / 4);
        distribution.put("Canada", followers.size() / 8);
        distribution.put("Other", followers.size() / 8);
        return distribution;
    }

    private Map<String, Integer> calculateAgeDistribution(List<Follow> followers) {
        // Simplified - would use age data
        Map<String, Integer> distribution = new HashMap<>();
        distribution.put("13-17", followers.size() / 6);
        distribution.put("18-24", followers.size() / 3);
        distribution.put("25-34", followers.size() / 3);
        distribution.put("35-44", followers.size() / 6);
        distribution.put("45+", followers.size() / 6);
        return distribution;
    }

    private Map<String, Integer> calculateGenderDistribution(List<Follow> followers) {
        // Simplified - would use gender data
        Map<String, Integer> distribution = new HashMap<>();
        distribution.put("Male", followers.size() / 2);
        distribution.put("Female", followers.size() / 2);
        return distribution;
    }

    private Map<String, Integer> calculateLanguageDistribution(List<Follow> followers) {
        // Simplified - would use language data
        Map<String, Integer> distribution = new HashMap<>();
        distribution.put("English", followers.size() / 2);
        distribution.put("Spanish", followers.size() / 4);
        distribution.put("French", followers.size() / 8);
        distribution.put("Other", followers.size() / 8);
        return distribution;
    }

    private Map<String, Double> calculateFollowerActivityByHour(List<Follow> followers, AnalyticsRequest request) {
        // Simplified - would use actual activity data
        Map<String, Double> activity = new HashMap<>();
        for (int i = 0; i < 24; i++) {
            activity.put(String.valueOf(i), Math.random() * 100);
        }
        return activity;
    }

    private Map<String, Double> calculateFollowerActivityByDay(List<Follow> followers, AnalyticsRequest request) {
        // Simplified - would use actual activity data
        Map<String, Double> activity = new HashMap<>();
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (String day : days) {
            activity.put(day, Math.random() * 100);
        }
        return activity;
    }

    private double calculateEngagementQuality(List<Follow> followers, AnalyticsRequest request) {
        // Simplified - would analyze actual engagement patterns
        return 75.0;
    }

    private double calculateLoyaltyScore(List<Follow> followers, AnalyticsRequest request) {
        // Simplified - would analyze follower retention
        return 80.0;
    }

    private double calculateRetentionRate(List<Follow> followers, AnalyticsRequest request) {
        // Simplified - would calculate actual retention
        return 85.0;
    }

    private double calculateChurnRate(List<Follow> followers, AnalyticsRequest request) {
        // Simplified - would calculate actual churn
        return 5.0;
    }

    private String extractVideoCategory(Video video) {
        // Simplified category extraction
        if (video.getDescription() != null) {
            String desc = video.getDescription().toLowerCase();
            if (desc.contains("music")) return "Music";
            if (desc.contains("gaming")) return "Gaming";
            if (desc.contains("comedy")) return "Comedy";
            if (desc.contains("education")) return "Education";
            if (desc.contains("sports")) return "Sports";
        }
        return "General";
    }

    private Map<String, Double> analyzeContentLengthPerformance(List<Video> videos) {
        // Simplified - would analyze actual performance by length
        Map<String, Double> performance = new HashMap<>();
        performance.put("< 15s", 60.0);
        performance.put("15-30s", 80.0);
        performance.put("30-60s", 90.0);
        performance.put("> 60s", 70.0);
        return performance;
    }

    private UploadFrequencyAnalysis analyzeUploadFrequency(List<Video> videos) {
        // Simplified - would analyze actual upload patterns
        UploadFrequencyAnalysis analysis = new UploadFrequencyAnalysis();
        analysis.setAverageVideosPerWeek(3.5);
        analysis.setOptimalFrequency(4.0);
        analysis.setConsistencyScore(75.0);
        return analysis;
    }

    private Map<String, Double> analyzeBestPostingTimes(List<Video> videos, AnalyticsRequest request) {
        // Simplified - would analyze actual posting times
        Map<String, Double> times = new HashMap<>();
        times.put("6AM-9AM", 65.0);
        times.put("12PM-3PM", 80.0);
        times.put("6PM-9PM", 95.0);
        times.put("9PM-12AM", 70.0);
        return times;
    }

    private double calculateAverageContentQuality(List<Video> videos) {
        // Simplified - would analyze actual quality metrics
        return 78.5;
    }

    private List<Video> identifyViralContent(List<Video> videos, AnalyticsRequest request) {
        // Simplified - would identify actual viral content
        return videos.stream()
                .filter(v -> v.getViews() > 10000)
                .sorted((a, b) -> Long.compare(b.getViews(), a.getViews()))
                .limit(5)
                .collect(Collectors.toList());
    }

    private List<Video> identifyUnderperformingContent(List<Video> videos, AnalyticsRequest request) {
        // Simplified - would identify actual underperforming content
        return videos.stream()
                .filter(v -> v.getViews() < 1000)
                .sorted((a, b) -> Long.compare(a.getViews(), b.getViews()))
                .limit(5)
                .collect(Collectors.toList());
    }

    // Additional helper methods would be implemented with full analytics logic...
    private double calculateLikeEngagement(List<Video> videos) { return 5.0; }
    private double calculateCommentEngagement(List<Video> videos, List<Comment> comments) { return 2.0; }
    private double calculateShareEngagement(List<Video> videos) { return 1.0; }
    private double calculateFollowEngagement(List<Video> videos, List<Follow> followers) { return 3.0; }
    private Map<LocalDateTime, Double> calculateEngagementTrends(List<Video> videos, List<Comment> comments, AnalyticsRequest request) { return new HashMap<>(); }
    private Map<String, Double> calculatePeakEngagementTimes(List<Video> videos, AnalyticsRequest request) { return new HashMap<>(); }
    private double calculateAverageWatchTime(List<Video> videos, AnalyticsRequest request) { return 45.0; }
    private double calculateRepeatViewRate(List<Video> videos, AnalyticsRequest request) { return 25.0; }
    private double calculateCommunityScore(List<Video> videos, List<Comment> comments, List<Follow> followers) { return 85.0; }
    private double calculateInteractionQuality(List<Comment> comments) { return 75.0; }
    private Map<String, BigDecimal> calculateRevenueBreakdown(List<Video> videos, AnalyticsRequest request) { return new HashMap<>(); }
    private Map<LocalDateTime, BigDecimal> calculateRevenueTrends(List<Video> videos, AnalyticsRequest request) { return new HashMap<>(); }
    private double calculateMonetizationEfficiency(List<Video> videos, BigDecimal totalEarnings) { return 65.0; }
    private BigDecimal calculateRevenuePerView(List<Video> videos, BigDecimal totalEarnings) { return BigDecimal.valueOf(0.001); }
    private BigDecimal calculateRevenuePerFollower(BigDecimal totalEarnings, AnalyticsRequest request) { return BigDecimal.valueOf(0.05); }
    private BigDecimal calculateProjectedMonthlyRevenue(List<Video> videos, BigDecimal totalEarnings) { return totalEarnings.multiply(BigDecimal.valueOf(0.1)); }
    private double calculateRevenueGrowthRate(List<Video> videos, AnalyticsRequest request) { return 15.0; }
    private Map<LocalDateTime, Integer> calculateFollowerGrowth(List<Follow> followers, AnalyticsRequest request) { return new HashMap<>(); }
    private double calculateMonthlyGrowthRate(List<Follow> followers) { return 10.0; }
    private double calculateWeeklyGrowthRate(List<Follow> followers) { return 2.5; }
    private double calculateDailyGrowthRate(List<Follow> followers) { return 0.3; }
    private int calculateProjectedFollowers(List<Follow> followers, int days) { return (int) (followers.size() * (1 + 0.003 * days)); }
    private double calculateOrganicGrowthRate(List<Follow> followers, AnalyticsRequest request) { return 8.0; }
    private double calculatePaidGrowthRate(List<Follow> followers, AnalyticsRequest request) { return 2.0; }
    private String calculateNextMilestone(List<Follow> followers) { return "100K"; }
    private double calculateMilestoneProgress(List<Follow> followers) { return 75.0; }
    private BigDecimal calculateVideoRevenue(Video video, AnalyticsRequest request) { return BigDecimal.valueOf(video.getViews() * 0.001); }
    private double calculateVideoPerformanceRating(Video video, AnalyticsRequest request) { return 85.0; }
    private Map<Integer, Double> calculateHourlyPerformance(List<Video> videos, AnalyticsRequest request) { return new HashMap<>(); }
    private Map<String, Double> calculateDailyPerformance(List<Video> videos, AnalyticsRequest request) { return new HashMap<>(); }
    private Map<String, Double> calculateWeeklyPerformance(List<Video> videos, AnalyticsRequest request) { return new HashMap<>(); }
    private Map<String, Double> calculateMonthlyPerformance(List<Video> videos, AnalyticsRequest request) { return new HashMap<>(); }
    private Map<String, Double> calculateSeasonalPatterns(List<Video> videos, AnalyticsRequest request) { return new HashMap<>(); }

    // Data classes for analytics results
    @Data
    public static class CreatorAnalyticsDashboard {
        private Long creatorId;
        private LocalDateTime generatedAt;
        private String dateRange;
        private PerformanceOverview performanceOverview;
        private AudienceInsights audienceInsights;
        private ContentPerformance contentPerformance;
        private EngagementAnalytics engagementAnalytics;
        private RevenueAnalytics revenueAnalytics;
        private GrowthAnalytics growthAnalytics;
        private List<VideoPerformance> topVideos;
        private TimeBasedAnalytics timeBasedAnalytics;
    }

    @Data
    public static class PerformanceOverview {
        private int totalVideos;
        private long totalViews;
        private long totalLikes;
        private long totalComments;
        private long totalShares;
        private long totalEngagements;
        private long totalFollowers;
        private double engagementRate;
        private double avgViewsPerVideo;
        private double avgLikesPerVideo;
        private double avgCommentsPerVideo;
        private Long topVideoId;
        private String topVideoTitle;
        private long topVideoViews;
        private double topVideoEngagement;
        private long monthlyGrowth;
        private long weeklyGrowth;
    }

    @Data
    public static class AudienceInsights {
        private int totalAudience;
        private long activeFollowers;
        private Map<String, Integer> geographicDistribution;
        private Map<String, Integer> ageDistribution;
        private Map<String, Integer> genderDistribution;
        private Map<String, Integer> languageDistribution;
        private Map<String, Double> activityByHour;
        private Map<String, Double> activityByDay;
        private double engagementQuality;
        private double loyaltyScore;
        private double retentionRate;
        private double churnRate;
    }

    @Data
    public static class ContentPerformance {
        private Map<String, ContentCategoryMetrics> categoryMetrics;
        private Map<String, Double> lengthPerformance;
        private UploadFrequencyAnalysis frequencyAnalysis;
        private Map<String, Double> bestPostingTimes;
        private double averageQualityScore;
        private List<Video> viralVideos;
        private List<Video> underperformingVideos;
    }

    @Data
    public static class ContentCategoryMetrics {
        private int videoCount;
        private long totalViews;
        private long totalLikes;
        private long totalComments;
        private double avgEngagement;

        public void addVideo(Video video) {
            videoCount++;
            totalViews += video.getViews();
            totalLikes += video.getLikes();
        }

        public void addComments(long commentCount) {
            totalComments += commentCount;
        }
    }

    @Data
    public static class UploadFrequencyAnalysis {
        private double averageVideosPerWeek;
        private double optimalFrequency;
        private double consistencyScore;
    }

    @Data
    public static class EngagementAnalytics {
        private double likeEngagement;
        private double commentEngagement;
        private double shareEngagement;
        private double followEngagement;
        private Map<LocalDateTime, Double> engagementTrends;
        private Map<String, Double> peakEngagementTimes;
        private double averageWatchTime;
        private double repeatViewRate;
        private double communityScore;
        private double interactionQuality;
    }

    @Data
    public static class RevenueAnalytics {
        private BigDecimal totalEarnings;
        private Map<String, BigDecimal> revenueBreakdown;
        private BigDecimal earningsPerVideo;
        private Map<LocalDateTime, BigDecimal> revenueTrends;
        private double monetizationEfficiency;
        private BigDecimal revenuePerView;
        private BigDecimal revenuePerFollower;
        private BigDecimal projectedMonthlyRevenue;
        private double revenueGrowthRate;
    }

    @Data
    public static class GrowthAnalytics {
        private Map<LocalDateTime, Integer> followerGrowth;
        private double monthlyGrowthRate;
        private double weeklyGrowthRate;
        private double dailyGrowthRate;
        private int projectedFollowers30Days;
        private int projectedFollowers90Days;
        private int projectedFollowers1Year;
        private double organicGrowthRate;
        private double paidGrowthRate;
        private String nextMilestone;
        private double milestoneProgress;
    }

    @Data
    public static class VideoPerformance {
        private Video video;
        private long views;
        private long likes;
        private long comments;
        private long shares;
        private double engagementScore;
        private BigDecimal revenue;
        private double performanceRating;
    }

    @Data
    public static class TimeBasedAnalytics {
        private Map<Integer, Double> hourlyPerformance;
        private Map<String, Double> dailyPerformance;
        private Map<String, Double> weeklyPerformance;
        private Map<String, Double> monthlyPerformance;
        private Map<String, Double> seasonalPatterns;
    }

    @Data
    public static class AnalyticsRequest {
        private Long creatorId;
        private User creator;
        private List<Video> videos;
        private List<Comment> comments;
        private List<Follow> followers;
        private String dateRange;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
    }
}
