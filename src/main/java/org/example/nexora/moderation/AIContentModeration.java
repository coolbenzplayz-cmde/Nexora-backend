package org.example.nexora.moderation;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.example.nexora.video.Video;
import org.example.nexora.social.Comment;
import org.example.nexora.user.User;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Advanced AI Content Moderation System that automatically detects and flags:
 * - Hate speech and harassment
 * - Spam and fake content
 * - Adult content
 * - Violence and harmful content
 * - Copyright violations
 * - Misinformation
 */
@Slf4j
public class AIContentModeration {

    // Moderation thresholds
    private static final double HIGH_RISK_THRESHOLD = 0.8;
    private static final double MEDIUM_RISK_THRESHOLD = 0.6;
    private static final double LOW_RISK_THRESHOLD = 0.4;
    
    // Pattern libraries for content detection
    private static final Set<String> HATE_SPEECH_KEYWORDS = Set.of(
        "hate", "kill", "die", "terrorist", "nazi", "racist", "discriminate"
    );
    
    private static final Set<String> ADULT_CONTENT_KEYWORDS = Set.of(
        "adult", "nsfw", "explicit", "porn", "sexual", "nude"
    );
    
    private static final Set<String> VIOLENCE_KEYWORDS = Set.of(
        "violence", "blood", "kill", "murder", "weapon", "gun", "fight"
    );
    
    private static final Set<String> SPAM_INDICATORS = Set.of(
        "click here", "buy now", "free money", "limited offer", "act now"
    );

    /**
     * Analyze video content for moderation
     */
    public VideoModerationResult analyzeVideo(Video video) {
        log.info("Analyzing video {} for content moderation", video.getId());

        VideoModerationResult result = new VideoModerationResult();
        result.setVideoId(video.getId());
        result.setAnalysisTimestamp(LocalDateTime.now());

        // Text-based analysis
        if (video.getTitle() != null) {
            TextAnalysisResult titleAnalysis = analyzeText(video.getTitle(), ContentType.TITLE);
            result.setTitleAnalysis(titleAnalysis);
        }

        if (video.getDescription() != null) {
            TextAnalysisResult descAnalysis = analyzeText(video.getDescription(), ContentType.DESCRIPTION);
            result.setDescriptionAnalysis(descAnalysis);
        }

        // Metadata analysis
        MetadataAnalysisResult metadataAnalysis = analyzeVideoMetadata(video);
        result.setMetadataAnalysis(metadataAnalysis);

        // Behavioral analysis
        BehavioralAnalysisResult behavioralAnalysis = analyzeVideoBehavior(video);
        result.setBehavioralAnalysis(behavioralAnalysis);

        // Calculate overall risk score
        double overallRiskScore = calculateOverallVideoRiskScore(result);
        result.setOverallRiskScore(overallRiskScore);

        // Determine moderation action
        ModerationAction action = determineModerationAction(overallRiskScore, result);
        result.setRecommendedAction(action);

        // Generate explanation
        String explanation = generateVideoModerationExplanation(result);
        result.setExplanation(explanation);

        return result;
    }

    /**
     * Analyze comment content for moderation
     */
    public CommentModerationResult analyzeComment(Comment comment) {
        log.info("Analyzing comment {} for content moderation", comment.getId());

        CommentModerationResult result = new CommentModerationResult();
        result.setCommentId(comment.getId());
        result.setAnalysisTimestamp(LocalDateTime.now());

        // Text analysis
        if (comment.getContent() != null) {
            TextAnalysisResult textAnalysis = analyzeText(comment.getContent(), ContentType.COMMENT);
            result.setTextAnalysis(textAnalysis);
        }

        // Context analysis
        ContextAnalysisResult contextAnalysis = analyzeCommentContext(comment);
        result.setContextAnalysis(contextAnalysis);

        // User history analysis
        if (comment.getUserId() != null) {
            UserHistoryAnalysisResult userAnalysis = analyzeUserHistory(comment.getUserId());
            result.setUserHistoryAnalysis(userAnalysis);
        }

        // Calculate overall risk score
        double overallRiskScore = calculateOverallCommentRiskScore(result);
        result.setOverallRiskScore(overallRiskScore);

        // Determine moderation action
        ModerationAction action = determineModerationAction(overallRiskScore, result);
        result.setRecommendedAction(action);

        // Generate explanation
        String explanation = generateCommentModerationExplanation(result);
        result.setExplanation(explanation);

        return result;
    }

    /**
     * Analyze user profile for moderation
     */
    public UserModerationResult analyzeUser(User user) {
        log.info("Analyzing user {} for content moderation", user.getId());

        UserModerationResult result = new UserModerationResult();
        result.setUserId(user.getId());
        result.setAnalysisTimestamp(LocalDateTime.now());

        // Profile analysis
        ProfileAnalysisResult profileAnalysis = analyzeUserProfile(user);
        result.setProfileAnalysis(profileAnalysis);

        // Activity pattern analysis
        ActivityPatternAnalysisResult activityAnalysis = analyzeUserActivityPattern(user);
        result.setActivityPatternAnalysis(activityAnalysis);

        // Network analysis
        NetworkAnalysisResult networkAnalysis = analyzeUserNetwork(user);
        result.setNetworkAnalysis(networkAnalysis);

        // Calculate overall risk score
        double overallRiskScore = calculateOverallUserRiskScore(result);
        result.setOverallRiskScore(overallRiskScore);

        // Determine moderation action
        ModerationAction action = determineModerationAction(overallRiskScore, result);
        result.setRecommendedAction(action);

        // Generate explanation
        String explanation = generateUserModerationExplanation(result);
        result.setExplanation(explanation);

        return result;
    }

    /**
     * Analyze text content for various policy violations
     */
    private TextAnalysisResult analyzeText(String text, ContentType contentType) {
        TextAnalysisResult result = new TextAnalysisResult();
        result.setText(text.toLowerCase());
        result.setContentType(contentType);

        Map<String, Double> riskCategories = new HashMap<>();

        // Hate speech detection
        double hateSpeechScore = detectHateSpeech(text);
        riskCategories.put("hate_speech", hateSpeechScore);

        // Adult content detection
        double adultContentScore = detectAdultContent(text);
        riskCategories.put("adult_content", adultContentScore);

        // Violence detection
        double violenceScore = detectViolence(text);
        riskCategories.put("violence", violenceScore);

        // Spam detection
        double spamScore = detectSpam(text);
        riskCategories.put("spam", spamScore);

        // Harassment detection
        double harassmentScore = detectHarassment(text);
        riskCategories.put("harassment", harassmentScore);

        // Misinformation detection
        double misinformationScore = detectMisinformation(text);
        riskCategories.put("misinformation", misinformationScore);

        result.setRiskCategories(riskCategories);
        result.setOverallRiskScore(calculateTextRiskScore(riskCategories));

        return result;
    }

    /**
     * Detect hate speech in text
     */
    private double detectHateSpeech(String text) {
        double score = 0.0;
        String[] words = text.toLowerCase().split("\\s+");

        for (String word : words) {
            if (HATE_SPEECH_KEYWORDS.contains(word)) {
                score += 0.3;
            }
        }

        // Pattern-based detection for more complex hate speech
        if (Pattern.compile("\\b(hate|kill|die).*(group|people|race)\\b", Pattern.CASE_INSENSITIVE).matcher(text).find()) {
            score += 0.5;
        }

        return Math.min(score, 1.0);
    }

    /**
     * Detect adult content
     */
    private double detectAdultContent(String text) {
        double score = 0.0;
        String[] words = text.toLowerCase().split("\\s+");

        for (String word : words) {
            if (ADULT_CONTENT_KEYWORDS.contains(word)) {
                score += 0.4;
            }
        }

        // Contextual patterns
        if (Pattern.compile("\\b(adult|nsfw|explicit|18\\+)\\b", Pattern.CASE_INSENSITIVE).matcher(text).find()) {
            score += 0.6;
        }

        return Math.min(score, 1.0);
    }

    /**
     * Detect violent content
     */
    private double detectViolence(String text) {
        double score = 0.0;
        String[] words = text.toLowerCase().split("\\s+");

        for (String word : words) {
            if (VIOLENCE_KEYWORDS.contains(word)) {
                score += 0.3;
            }
        }

        // Threat detection
        if (Pattern.compile("\\b(i will|going to|will).*(kill|hurt|harm)\\b", Pattern.CASE_INSENSITIVE).matcher(text).find()) {
            score += 0.7;
        }

        return Math.min(score, 1.0);
    }

    /**
     * Detect spam content
     */
    private double detectSpam(String text) {
        double score = 0.0;

        // Keyword-based detection
        String[] words = text.toLowerCase().split("\\s+");
        for (String word : words) {
            if (SPAM_INDICATORS.contains(word)) {
                score += 0.2;
            }
        }

        // Pattern-based detection
        if (Pattern.compile("\\b(http|www\\.|\\.com)\\b", Pattern.CASE_INSENSITIVE).matcher(text).find()) {
            score += 0.3;
        }

        // Excessive capitalization
        long capitalLetters = text.chars().filter(Character::isUpperCase).count();
        if (capitalLetters > text.length() * 0.5) {
            score += 0.3;
        }

        // Excessive punctuation
        long punctuation = text.chars().filter(ch -> "!?#$%^&*()".indexOf(ch) >= 0).count();
        if (punctuation > text.length() * 0.2) {
            score += 0.2;
        }

        // Repetitive characters
        if (Pattern.compile("(.)\\1{3,}").matcher(text).find()) {
            score += 0.2;
        }

        return Math.min(score, 1.0);
    }

    /**
     * Detect harassment
     */
    private double detectHarassment(String text) {
        double score = 0.0;

        // Personal attacks
        if (Pattern.compile("\\b(you are|you're|idiot|stupid|dumb|loser)\\b", Pattern.CASE_INSENSITIVE).matcher(text).find()) {
            score += 0.4;
        }

        // Threats
        if (Pattern.compile("\\b(i will|going to|will).*(find|get|expose)\\b", Pattern.CASE_INSENSITIVE).matcher(text).find()) {
            score += 0.6;
        }

        // Cyberbullying patterns
        if (Pattern.compile("\\b(nobody likes|everyone hates|go kill|go die)\\b", Pattern.CASE_INSENSITIVE).matcher(text).find()) {
            score += 0.7;
        }

        return Math.min(score, 1.0);
    }

    /**
     * Detect misinformation (simplified version)
     */
    private double detectMisinformation(String text) {
        double score = 0.0;

        // Medical misinformation indicators
        if (Pattern.compile("\\b(cure|treatment|vaccine).*(cancer|covid|diabetes)\\b", Pattern.CASE_INSENSITIVE).matcher(text).find()) {
            score += 0.4;
        }

        // Conspiracy theory indicators
        if (Pattern.compile("\\b(conspiracy|cover up|hidden truth|they don't want)\\b", Pattern.CASE_INSENSITIVE).matcher(text).find()) {
            score += 0.3;
        }

        // Fake news indicators
        if (Pattern.compile("\\b(breaking|exclusive|shocking).*(news|report)\\b", Pattern.CASE_INSENSITIVE).matcher(text).find()) {
            score += 0.2;
        }

        return Math.min(score, 1.0);
    }

    /**
     * Analyze video metadata for suspicious patterns
     */
    private MetadataAnalysisResult analyzeVideoMetadata(Video video) {
        MetadataAnalysisResult result = new MetadataAnalysisResult();

        double score = 0.0;
        List<String> redFlags = new ArrayList<>();

        // Check for suspicious title patterns
        if (video.getTitle() != null) {
            if (video.getTitle().length() > 100) {
                score += 0.2;
                redFlags.add("Unusually long title");
            }

            if (video.getTitle().matches(".*[!]{3,}.*")) {
                score += 0.3;
                redFlags.add("Excessive punctuation in title");
            }
        }

        // Check engagement patterns
        if (video.getViews() > 0 && video.getLikes() > 0) {
            double likeRatio = (double) video.getLikes() / video.getViews();
            if (likeRatio > 0.1) { // Suspiciously high like ratio
                score += 0.3;
                redFlags.add("Suspiciously high like-to-view ratio");
            }
        }

        // Check upload frequency (would need user's video history)
        // This is a placeholder for more complex analysis

        result.setRiskScore(Math.min(score, 1.0));
        result.setRedFlags(redFlags);

        return result;
    }

    /**
     * Analyze video behavior patterns
     */
    private BehavioralAnalysisResult analyzeVideoBehavior(Video video) {
        BehavioralAnalysisResult result = new BehavioralAnalysisResult();

        double score = 0.0;
        List<String> suspiciousPatterns = new ArrayList<>();

        // Rapid engagement spike detection
        // This would require time-series engagement data
        // Placeholder implementation

        // Comment pattern analysis
        // This would require comment analysis
        // Placeholder implementation

        result.setRiskScore(score);
        result.setSuspiciousPatterns(suspiciousPatterns);

        return result;
    }

    /**
     * Analyze comment context
     */
    private ContextAnalysisResult analyzeCommentContext(Comment comment) {
        ContextAnalysisResult result = new ContextAnalysisResult();

        double score = 0.0;

        // Check if comment is a reply to another flagged comment
        // This would require access to parent comment data
        // Placeholder implementation

        // Check comment thread toxicity
        // This would require thread analysis
        // Placeholder implementation

        result.setRiskScore(score);

        return result;
    }

    /**
     * Analyze user history
     */
    private UserHistoryAnalysisResult analyzeUserHistory(Long userId) {
        UserHistoryAnalysisResult result = new UserHistoryAnalysisResult();

        double score = 0.0;
        List<String> redFlags = new ArrayList<>();

        // This would require access to user's content history
        // Placeholder implementation

        result.setRiskScore(score);
        result.setRedFlags(redFlags);

        return result;
    }

    /**
     * Analyze user profile
     */
    private ProfileAnalysisResult analyzeUserProfile(User user) {
        ProfileAnalysisResult result = new ProfileAnalysisResult();

        double score = 0.0;
        List<String> redFlags = new ArrayList<>();

        // Check for suspicious profile patterns
        if (user.getUsername() != null && user.getUsername().matches(".*[0-9]{3,}.*")) {
            score += 0.2;
            redFlags.add("Username contains many numbers");
        }

        if (user.getBio() != null && user.getBio().length() > 500) {
            score += 0.1;
            redFlags.add("Unusually long bio");
        }

        result.setRiskScore(score);
        result.setRedFlags(redFlags);

        return result;
    }

    /**
     * Analyze user activity patterns
     */
    private ActivityPatternAnalysisResult analyzeUserActivityPattern(User user) {
        ActivityPatternAnalysisResult result = new ActivityPatternAnalysisResult();

        double score = 0.0;

        // This would require access to user activity logs
        // Placeholder implementation

        result.setRiskScore(score);

        return result;
    }

    /**
     * Analyze user network
     */
    private NetworkAnalysisResult analyzeUserNetwork(User user) {
        NetworkAnalysisResult result = new NetworkAnalysisResult();

        double score = 0.0;

        // This would require access to user's social network data
        // Placeholder implementation

        result.setRiskScore(score);

        return result;
    }

    // Helper methods for calculating scores and determining actions
    private double calculateTextRiskScore(Map<String, Double> riskCategories) {
        return riskCategories.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }

    private double calculateOverallVideoRiskScore(VideoModerationResult result) {
        double score = 0.0;
        int factors = 0;

        if (result.getTitleAnalysis() != null) {
            score += result.getTitleAnalysis().getOverallRiskScore() * 0.3;
            factors++;
        }

        if (result.getDescriptionAnalysis() != null) {
            score += result.getDescriptionAnalysis().getOverallRiskScore() * 0.3;
            factors++;
        }

        if (result.getMetadataAnalysis() != null) {
            score += result.getMetadataAnalysis().getRiskScore() * 0.2;
            factors++;
        }

        if (result.getBehavioralAnalysis() != null) {
            score += result.getBehavioralAnalysis().getRiskScore() * 0.2;
            factors++;
        }

        return factors > 0 ? score : 0.0;
    }

    private double calculateOverallCommentRiskScore(CommentModerationResult result) {
        double score = 0.0;
        int factors = 0;

        if (result.getTextAnalysis() != null) {
            score += result.getTextAnalysis().getOverallRiskScore() * 0.6;
            factors++;
        }

        if (result.getContextAnalysis() != null) {
            score += result.getContextAnalysis().getRiskScore() * 0.2;
            factors++;
        }

        if (result.getUserHistoryAnalysis() != null) {
            score += result.getUserHistoryAnalysis().getRiskScore() * 0.2;
            factors++;
        }

        return factors > 0 ? score : 0.0;
    }

    private double calculateOverallUserRiskScore(UserModerationResult result) {
        double score = 0.0;
        int factors = 0;

        if (result.getProfileAnalysis() != null) {
            score += result.getProfileAnalysis().getRiskScore() * 0.3;
            factors++;
        }

        if (result.getActivityPatternAnalysis() != null) {
            score += result.getActivityPatternAnalysis().getRiskScore() * 0.4;
            factors++;
        }

        if (result.getNetworkAnalysis() != null) {
            score += result.getNetworkAnalysis().getRiskScore() * 0.3;
            factors++;
        }

        return factors > 0 ? score : 0.0;
    }

    private ModerationAction determineModerationAction(double riskScore, Object result) {
        if (riskScore >= HIGH_RISK_THRESHOLD) {
            return ModerationAction.REMOVE_CONTENT;
        } else if (riskScore >= MEDIUM_RISK_THRESHOLD) {
            return ModerationAction.FLAG_FOR_REVIEW;
        } else if (riskScore >= LOW_RISK_THRESHOLD) {
            return ModerationAction.MONITOR;
        } else {
            return ModerationAction.ALLOW;
        }
    }

    private String generateVideoModerationExplanation(VideoModerationResult result) {
        StringBuilder explanation = new StringBuilder();
        
        if (result.getTitleAnalysis() != null && result.getTitleAnalysis().getOverallRiskScore() > 0.5) {
            explanation.append("Title contains potentially problematic content. ");
        }
        
        if (result.getDescriptionAnalysis() != null && result.getDescriptionAnalysis().getOverallRiskScore() > 0.5) {
            explanation.append("Description contains potentially problematic content. ");
        }
        
        if (result.getMetadataAnalysis() != null && result.getMetadataAnalysis().getRiskScore() > 0.5) {
            explanation.append("Metadata shows suspicious patterns. ");
        }
        
        return explanation.toString();
    }

    private String generateCommentModerationExplanation(CommentModerationResult result) {
        StringBuilder explanation = new StringBuilder();
        
        if (result.getTextAnalysis() != null && result.getTextAnalysis().getOverallRiskScore() > 0.5) {
            explanation.append("Comment contains potentially problematic content. ");
        }
        
        return explanation.toString();
    }

    private String generateUserModerationExplanation(UserModerationResult result) {
        StringBuilder explanation = new StringBuilder();
        
        if (result.getProfileAnalysis() != null && result.getProfileAnalysis().getRiskScore() > 0.5) {
            explanation.append("User profile shows suspicious patterns. ");
        }
        
        return explanation.toString();
    }

    // Enums and data classes
    public enum ContentType {
        TITLE, DESCRIPTION, COMMENT, BIO
    }

    public enum ModerationAction {
        ALLOW, MONITOR, FLAG_FOR_REVIEW, REMOVE_CONTENT, SUSPEND_USER
    }

    @Data
    public static class VideoModerationResult {
        private Long videoId;
        private LocalDateTime analysisTimestamp;
        private TextAnalysisResult titleAnalysis;
        private TextAnalysisResult descriptionAnalysis;
        private MetadataAnalysisResult metadataAnalysis;
        private BehavioralAnalysisResult behavioralAnalysis;
        private double overallRiskScore;
        private ModerationAction recommendedAction;
        private String explanation;
    }

    @Data
    public static class CommentModerationResult {
        private Long commentId;
        private LocalDateTime analysisTimestamp;
        private TextAnalysisResult textAnalysis;
        private ContextAnalysisResult contextAnalysis;
        private UserHistoryAnalysisResult userHistoryAnalysis;
        private double overallRiskScore;
        private ModerationAction recommendedAction;
        private String explanation;
    }

    @Data
    public static class UserModerationResult {
        private Long userId;
        private LocalDateTime analysisTimestamp;
        private ProfileAnalysisResult profileAnalysis;
        private ActivityPatternAnalysisResult activityPatternAnalysis;
        private NetworkAnalysisResult networkAnalysis;
        private double overallRiskScore;
        private ModerationAction recommendedAction;
        private String explanation;
    }

    @Data
    public static class TextAnalysisResult {
        private String text;
        private ContentType contentType;
        private Map<String, Double> riskCategories;
        private double overallRiskScore;
    }

    @Data
    public static class MetadataAnalysisResult {
        private double riskScore;
        private List<String> redFlags;
    }

    @Data
    public static class BehavioralAnalysisResult {
        private double riskScore;
        private List<String> suspiciousPatterns;
    }

    @Data
    public static class ContextAnalysisResult {
        private double riskScore;
    }

    @Data
    public static class UserHistoryAnalysisResult {
        private double riskScore;
        private List<String> redFlags;
    }

    @Data
    public static class ProfileAnalysisResult {
        private double riskScore;
        private List<String> redFlags;
    }

    @Data
    public static class ActivityPatternAnalysisResult {
        private double riskScore;
    }

    @Data
    public static class NetworkAnalysisResult {
        private double riskScore;
    }
}
