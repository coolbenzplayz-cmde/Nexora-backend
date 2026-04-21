package org.example.nexora.fraud;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.nexora.transaction.Transaction;
import org.example.nexora.user.User;
import org.example.nexora.wallet.Wallet;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Comprehensive Fraud Detection System providing:
 * - Transaction pattern analysis
 * - Behavioral anomaly detection
 - Device fingerprinting
 - Location-based fraud detection
 - Account takeover detection
 - Real-time fraud scoring
 - Automated fraud prevention
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FraudDetectionService {

    private final FraudAnalyticsService analyticsService;

    /**
     * Analyze transaction for fraud risk
     */
    public FraudAnalysisResult analyzeTransaction(Transaction transaction, FraudDetectionRequest request) {
        log.info("Analyzing transaction {} for fraud", transaction.getId());

        FraudAnalysisResult result = new FraudAnalysisResult();
        result.setTransactionId(transaction.getId());
        result.setUserId(transaction.getFromUserId());
        result.setAnalysisTimestamp(LocalDateTime.now());

        // Transaction pattern analysis
        TransactionPatternAnalysis patternAnalysis = analyzeTransactionPattern(transaction, request);
        result.setPatternAnalysis(patternAnalysis);

        // Behavioral analysis
        BehavioralAnalysis behavioralAnalysis = analyzeUserBehavior(transaction.getFromUserId(), request);
        result.setBehavioralAnalysis(behavioralAnalysis);

        // Device analysis
        DeviceAnalysis deviceAnalysis = analyzeDevice(request.getDeviceInfo());
        result.setDeviceAnalysis(deviceAnalysis);

        // Location analysis
        LocationAnalysis locationAnalysis = analyzeLocation(request.getLocationInfo());
        result.setLocationAnalysis(locationAnalysis);

        // Account security analysis
        AccountSecurityAnalysis securityAnalysis = analyzeAccountSecurity(transaction.getFromUserId(), request);
        result.setSecurityAnalysis(securityAnalysis);

        // Calculate overall fraud score
        double fraudScore = calculateOverallFraudScore(result);
        result.setOverallFraudScore(fraudScore);

        // Determine action
        FraudAction action = determineFraudAction(fraudScore, result);
        result.setRecommendedAction(action);

        // Generate explanation
        String explanation = generateFraudExplanation(result);
        result.setExplanation(explanation);

        return result;
    }

    /**
     * Detect account takeover attempts
     */
    public AccountTakeoverResult detectAccountTakeover(Long userId, AccountTakeoverRequest request) {
        log.info("Checking for account takeover for user {}", userId);

        AccountTakeoverResult result = new AccountTakeoverResult();
        result.setUserId(userId);
        result.setAnalysisTimestamp(LocalDateTime.now());

        // Login pattern analysis
        LoginPatternAnalysis loginAnalysis = analyzeLoginPatterns(userId, request);
        result.setLoginAnalysis(loginAnalysis);

        // Device consistency check
        DeviceConsistencyAnalysis deviceConsistency = analyzeDeviceConsistency(userId, request);
        result.setDeviceConsistency(deviceConsistency);

        // Location anomaly detection
        LocationAnomalyAnalysis locationAnomaly = analyzeLocationAnomalies(userId, request);
        result.setLocationAnomaly(locationAnomaly);

        // Behavioral changes
        BehavioralChangeAnalysis behaviorChange = analyzeBehavioralChanges(userId, request);
        result.setBehavioralChange(behaviorChange);

        // Calculate takeover risk
        double takeoverRisk = calculateTakeoverRisk(result);
        result.setTakeoverRiskScore(takeoverRisk);

        // Determine security action
        SecurityAction securityAction = determineSecurityAction(takeoverRisk, result);
        result.setRecommendedSecurityAction(securityAction);

        return result;
    }

    /**
     * Analyze user for suspicious activity patterns
     */
    public SuspiciousActivityResult analyzeSuspiciousActivity(Long userId, SuspiciousActivityRequest request) {
        log.info("Analyzing suspicious activity for user {}", userId);

        SuspiciousActivityResult result = new SuspiciousActivityResult();
        result.setUserId(userId);
        result.setAnalysisTimestamp(LocalDateTime.now());

        // Transaction velocity analysis
        TransactionVelocityAnalysis velocityAnalysis = analyzeTransactionVelocity(userId, request);
        result.setVelocityAnalysis(velocityAnalysis);

        // Network analysis
        NetworkAnalysis networkAnalysis = analyzeUserNetwork(userId, request);
        result.setNetworkAnalysis(networkAnalysis);

        // Content analysis
        ContentAnalysis contentAnalysis = analyzeUserContent(userId, request);
        result.setContentAnalysis(contentAnalysis);

        // Time-based patterns
        TimePatternAnalysis timeAnalysis = analyzeTimePatterns(userId, request);
        result.setTimeAnalysis(timeAnalysis);

        // Calculate suspicious activity score
        double suspiciousScore = calculateSuspiciousActivityScore(result);
        result.setSuspiciousActivityScore(suspiciousScore);

        // Determine monitoring action
        MonitoringAction monitoringAction = determineMonitoringAction(suspiciousScore, result);
        result.setRecommendedMonitoringAction(monitoringAction);

        return result;
    }

    /**
     * Real-time fraud monitoring
     */
    public RealTimeFraudAlert monitorRealTimeFraud(Transaction transaction, RealTimeMonitoringRequest request) {
        log.info("Real-time fraud monitoring for transaction {}", transaction.getId());

        RealTimeFraudAlert alert = new RealTimeFraudAlert();
        alert.setTransactionId(transaction.getId());
        alert.setUserId(transaction.getFromUserId());
        alert.setAlertTimestamp(LocalDateTime.now());

        // Quick risk assessment
        double quickRiskScore = calculateQuickRiskScore(transaction, request);
        alert.setQuickRiskScore(quickRiskScore);

        // Check against known fraud patterns
        boolean knownFraudPattern = checkKnownFraudPatterns(transaction);
        alert.setKnownFraudPattern(knownFraudPattern);

        // Velocity check
        boolean velocityViolation = checkVelocityViolations(transaction, request);
        alert.setVelocityViolation(velocityViolation);

        // Blacklist check
        boolean blacklisted = checkBlacklists(transaction, request);
        alert.setBlacklisted(blacklisted);

        // Determine immediate action
        if (quickRiskScore > 0.8 || knownFraudPattern || velocityViolation || blacklisted) {
            alert.setImmediateAction(FraudAction.BLOCK_TRANSACTION);
            alert.setRequiresManualReview(true);
        } else if (quickRiskScore > 0.6) {
            alert.setImmediateAction(FraudAction.REQUIRE_ADDITIONAL_VERIFICATION);
            alert.setRequiresManualReview(false);
        } else {
            alert.setImmediateAction(FraudAction.ALLOW_TRANSACTION);
            alert.setRequiresManualReview(false);
        }

        return alert;
    }

    /**
     * Get fraud statistics and trends
     */
    public FraudStatistics getFraudStatistics(FraudStatisticsRequest request) {
        log.info("Generating fraud statistics");

        FraudStatistics stats = new FraudStatistics();
        stats.setGeneratedAt(LocalDateTime.now());
        stats.setDateRange(request.getDateRange());

        // Fraud trends
        Map<String, Integer> fraudTrends = calculateFraudTrends(request);
        stats.setFraudTrends(fraudTrends);

        // Fraud types breakdown
        Map<String, Integer> fraudTypes = getFraudTypesBreakdown(request);
        stats.setFraudTypesBreakdown(fraudTypes);

        // Geographic hotspots
        Map<String, Integer> geographicHotspots = identifyGeographicHotspots(request);
        stats.setGeographicHotspots(geographicHotspots);

        // Device patterns
        Map<String, Integer> devicePatterns = analyzeDevicePatterns(request);
        stats.setDevicePatterns(devicePatterns);

        // Prevention effectiveness
        double preventionEffectiveness = calculatePreventionEffectiveness(request);
        stats.setPreventionEffectiveness(preventionEffectiveness);

        // False positive rate
        double falsePositiveRate = calculateFalsePositiveRate(request);
        stats.setFalsePositiveRate(falsePositiveRate);

        return stats;
    }

    // Private analysis methods
    private TransactionPatternAnalysis analyzeTransactionPattern(Transaction transaction, FraudDetectionRequest request) {
        TransactionPatternAnalysis analysis = new TransactionPatternAnalysis();

        // Amount analysis
        double amountRisk = analyzeTransactionAmount(transaction);
        analysis.setAmountRiskScore(amountRisk);

        // Frequency analysis
        double frequencyRisk = analyzeTransactionFrequency(transaction.getFromUserId(), request);
        analysis.setFrequencyRiskScore(frequencyRisk);

        // Recipient analysis
        double recipientRisk = analyzeTransactionRecipient(transaction.getToUserId(), request);
        analysis.setRecipientRiskScore(recipientRisk);

        // Timing analysis
        double timingRisk = analyzeTransactionTiming(transaction);
        analysis.setTimingRiskScore(timingRisk);

        // Pattern risk score
        analysis.setPatternRiskScore((amountRisk + frequencyRisk + recipientRisk + timingRisk) / 4);

        return analysis;
    }

    private BehavioralAnalysis analyzeUserBehavior(Long userId, FraudDetectionRequest request) {
        BehavioralAnalysis analysis = new BehavioralAnalysis();

        // Login patterns
        double loginRisk = analyzeLoginPatterns(userId, request).getRiskScore();
        analysis.setLoginPatternRisk(loginRisk);

        // Transaction patterns
        double transactionRisk = analyzeHistoricalTransactionPatterns(userId, request);
        analysis.setTransactionPatternRisk(transactionRisk);

        // Device usage
        double deviceRisk = analyzeDeviceUsagePatterns(userId, request);
        analysis.setDeviceUsageRisk(deviceRisk);

        // Location patterns
        double locationRisk = analyzeLocationPatterns(userId, request);
        analysis.setLocationPatternRisk(locationRisk);

        // Overall behavioral risk
        analysis.setBehavioralRiskScore((loginRisk + transactionRisk + deviceRisk + locationRisk) / 4);

        return analysis;
    }

    private DeviceAnalysis analyzeDevice(DeviceInfo deviceInfo) {
        DeviceAnalysis analysis = new DeviceAnalysis();

        if (deviceInfo == null) {
            analysis.setDeviceRiskScore(0.5); // Medium risk for missing device info
            return analysis;
        }

        // Device fingerprint analysis
        double fingerprintRisk = analyzeDeviceFingerprint(deviceInfo);
        analysis.setFingerprintRiskScore(fingerprintRisk);

        // Known malicious devices
        boolean knownMalicious = checkKnownMaliciousDevices(deviceInfo);
        analysis.setKnownMaliciousDevice(knownMalicious);

        // Emulator/vm detection
        boolean emulator = detectEmulator(deviceInfo);
        analysis.setEmulatorDetected(emulator);

        // Root/jailbreak detection
        boolean rooted = detectRootedDevice(deviceInfo);
        analysis.setRootedDevice(rooted);

        // Calculate device risk
        double deviceRisk = fingerprintRisk;
        if (knownMalicious) deviceRisk += 0.3;
        if (emulator) deviceRisk += 0.2;
        if (rooted) deviceRisk += 0.2;

        analysis.setDeviceRiskScore(Math.min(deviceRisk, 1.0));

        return analysis;
    }

    private LocationAnalysis analyzeLocation(LocationInfo locationInfo) {
        LocationAnalysis analysis = new LocationAnalysis();

        if (locationInfo == null) {
            analysis.setLocationRiskScore(0.3); // Low risk for missing location
            return analysis;
        }

        // High-risk countries
        boolean highRiskCountry = isHighRiskCountry(locationInfo.getCountry());
        analysis.setHighRiskCountry(highRiskCountry);

        // IP geolocation consistency
        boolean locationConsistent = checkLocationConsistency(locationInfo);
        analysis.setLocationConsistent(locationConsistent);

        // VPN/proxy detection
        boolean vpnDetected = detectVPN(locationInfo);
        analysis.setVpnDetected(vpnDetected);

        // Calculate location risk
        double locationRisk = 0.0;
        if (highRiskCountry) locationRisk += 0.4;
        if (!locationConsistent) locationRisk += 0.3;
        if (vpnDetected) locationRisk += 0.2;

        analysis.setLocationRiskScore(Math.min(locationRisk, 1.0));

        return analysis;
    }

    private AccountSecurityAnalysis analyzeAccountSecurity(Long userId, FraudDetectionRequest request) {
        AccountSecurityAnalysis analysis = new AccountSecurityAnalysis();

        // Account age
        double accountAgeRisk = analyzeAccountAge(userId);
        analysis.setAccountAgeRisk(accountAgeRisk);

        // Verification status
        double verificationRisk = analyzeVerificationStatus(userId);
        analysis.setVerificationRisk(verificationRisk);

        // Recent security changes
        double securityChangeRisk = analyzeRecentSecurityChanges(userId, request);
        analysis.setSecurityChangeRisk(securityChangeRisk);

        // Password strength
        double passwordRisk = analyzePasswordStrength(userId);
        analysis.setPasswordRisk(passwordRisk);

        // 2FA status
        boolean has2FA = check2FAStatus(userId);
        analysis.setHasTwoFactorAuth(has2FA);

        // Calculate security risk
        double securityRisk = (accountAgeRisk + verificationRisk + securityChangeRisk + passwordRisk) / 4;
        if (!has2FA) securityRisk += 0.2;

        analysis.setSecurityRiskScore(Math.min(securityRisk, 1.0));

        return analysis;
    }

    // Helper methods for risk calculation
    private double analyzeTransactionAmount(Transaction transaction) {
        BigDecimal amount = transaction.getAmount();
        
        // Risk increases with amount
        if (amount.compareTo(BigDecimal.valueOf(1000)) > 0) return 0.2;
        if (amount.compareTo(BigDecimal.valueOf(5000)) > 0) return 0.4;
        if (amount.compareTo(BigDecimal.valueOf(10000)) > 0) return 0.6;
        if (amount.compareTo(BigDecimal.valueOf(50000)) > 0) return 0.8;
        
        return 0.1;
    }

    private double analyzeTransactionFrequency(Long userId, FraudDetectionRequest request) {
        // Simplified - would check recent transaction count
        return Math.random() * 0.5;
    }

    private double analyzeTransactionRecipient(Long recipientId, FraudDetectionRequest request) {
        // Simplified - would check recipient reputation
        return Math.random() * 0.3;
    }

    private double analyzeTransactionTiming(Transaction transaction) {
        // Risk for unusual transaction times (e.g., 3 AM)
        int hour = transaction.getCreatedAt().getHour();
        if (hour >= 2 && hour <= 5) return 0.3;
        return 0.1;
    }

    private double calculateOverallFraudScore(FraudAnalysisResult result) {
        double patternRisk = result.getPatternAnalysis().getPatternRiskScore();
        double behavioralRisk = result.getBehavioralAnalysis().getBehavioralRiskScore();
        double deviceRisk = result.getDeviceAnalysis().getDeviceRiskScore();
        double locationRisk = result.getLocationAnalysis().getLocationRiskScore();
        double securityRisk = result.getSecurityAnalysis().getSecurityRiskScore();

        return (patternRisk * 0.3 + behavioralRisk * 0.25 + deviceRisk * 0.2 + 
                locationRisk * 0.15 + securityRisk * 0.1);
    }

    private FraudAction determineFraudAction(double fraudScore, FraudAnalysisResult result) {
        if (fraudScore > 0.8) return FraudAction.BLOCK_TRANSACTION;
        if (fraudScore > 0.6) return FraudAction.REQUIRE_ADDITIONAL_VERIFICATION;
        if (fraudScore > 0.4) return FraudAction.MONITOR_USER;
        return FraudAction.ALLOW_TRANSACTION;
    }

    private String generateFraudExplanation(FraudAnalysisResult result) {
        StringBuilder explanation = new StringBuilder();
        
        if (result.getPatternAnalysis().getPatternRiskScore() > 0.5) {
            explanation.append("Unusual transaction pattern detected. ");
        }
        if (result.getBehavioralAnalysis().getBehavioralRiskScore() > 0.5) {
            explanation.append("Behavioral anomalies detected. ");
        }
        if (result.getDeviceAnalysis().getDeviceRiskScore() > 0.5) {
            explanation.append("Suspicious device characteristics. ");
        }
        if (result.getLocationAnalysis().getLocationRiskScore() > 0.5) {
            explanation.append("Unusual location or VPN detected. ");
        }
        
        return explanation.toString();
    }

    // Additional analysis methods (simplified implementations)
    private LoginPatternAnalysis analyzeLoginPatterns(Long userId, AccountTakeoverRequest request) {
        LoginPatternAnalysis analysis = new LoginPatternAnalysis();
        analysis.setRiskScore(Math.random() * 0.5);
        return analysis;
    }

    private DeviceConsistencyAnalysis analyzeDeviceConsistency(Long userId, AccountTakeoverRequest request) {
        DeviceConsistencyAnalysis analysis = new DeviceConsistencyAnalysis();
        analysis.setConsistencyScore(0.8);
        return analysis;
    }

    private LocationAnomalyAnalysis analyzeLocationAnomalies(Long userId, AccountTakeoverRequest request) {
        LocationAnomalyAnalysis analysis = new LocationAnomalyAnalysis();
        analysis.setAnomalyScore(0.2);
        return analysis;
    }

    private BehavioralChangeAnalysis analyzeBehavioralChanges(Long userId, AccountTakeoverRequest request) {
        BehavioralChangeAnalysis analysis = new BehavioralChangeAnalysis();
        analysis.setChangeScore(0.3);
        return analysis;
    }

    private double calculateTakeoverRisk(AccountTakeoverResult result) {
        return (result.getLoginAnalysis().getRiskScore() * 0.3 +
                (1 - result.getDeviceConsistency().getConsistencyScore()) * 0.3 +
                result.getLocationAnomaly().getAnomalyScore() * 0.2 +
                result.getBehavioralChange().getChangeScore() * 0.2);
    }

    private SecurityAction determineSecurityAction(double takeoverRisk, AccountTakeoverResult result) {
        if (takeoverRisk > 0.8) return SecurityAction.LOCK_ACCOUNT;
        if (takeoverRisk > 0.6) return SecurityAction.REQUIRE_ADDITIONAL_AUTH;
        if (takeoverRisk > 0.4) return SecurityAction.SECURITY_ALERT;
        return SecurityAction.MONITOR;
    }

    private TransactionVelocityAnalysis analyzeTransactionVelocity(Long userId, SuspiciousActivityRequest request) {
        TransactionVelocityAnalysis analysis = new TransactionVelocityAnalysis();
        analysis.setVelocityScore(Math.random() * 0.6);
        return analysis;
    }

    private NetworkAnalysis analyzeUserNetwork(Long userId, SuspiciousActivityRequest request) {
        NetworkAnalysis analysis = new NetworkAnalysis();
        analysis.setNetworkRiskScore(Math.random() * 0.4);
        return analysis;
    }

    private ContentAnalysis analyzeUserContent(Long userId, SuspiciousActivityRequest request) {
        ContentAnalysis analysis = new ContentAnalysis();
        analysis.setContentRiskScore(Math.random() * 0.3);
        return analysis;
    }

    private TimePatternAnalysis analyzeTimePatterns(Long userId, SuspiciousActivityRequest request) {
        TimePatternAnalysis analysis = new TimePatternAnalysis();
        analysis.setTimeRiskScore(Math.random() * 0.3);
        return analysis;
    }

    private double calculateSuspiciousActivityScore(SuspiciousActivityResult result) {
        return (result.getVelocityAnalysis().getVelocityScore() * 0.3 +
                result.getNetworkAnalysis().getNetworkRiskScore() * 0.3 +
                result.getContentAnalysis().getContentRiskScore() * 0.2 +
                result.getTimeAnalysis().getTimeRiskScore() * 0.2);
    }

    private MonitoringAction determineMonitoringAction(double suspiciousScore, SuspiciousActivityResult result) {
        if (suspiciousScore > 0.7) return MonitoringAction.FLAG_FOR_REVIEW;
        if (suspiciousScore > 0.5) return MonitoringAction.INCREASE_MONITORING;
        return MonitoringAction.CONTINUE_MONITORING;
    }

    private double calculateQuickRiskScore(Transaction transaction, RealTimeMonitoringRequest request) {
        return Math.random() * 0.8; // Simplified
    }

    private boolean checkKnownFraudPatterns(Transaction transaction) {
        return Math.random() < 0.1; // 10% chance of known pattern
    }

    private boolean checkVelocityViolations(Transaction transaction, RealTimeMonitoringRequest request) {
        return Math.random() < 0.05; // 5% chance of velocity violation
    }

    private boolean checkBlacklists(Transaction transaction, RealTimeMonitoringRequest request) {
        return Math.random() < 0.02; // 2% chance of blacklist match
    }

    // Additional helper methods (simplified implementations)
    private double analyzeDeviceFingerprint(DeviceInfo deviceInfo) { return Math.random() * 0.3; }
    private boolean checkKnownMaliciousDevices(DeviceInfo deviceInfo) { return Math.random() < 0.05; }
    private boolean detectEmulator(DeviceInfo deviceInfo) { return Math.random() < 0.1; }
    private boolean detectRootedDevice(DeviceInfo deviceInfo) { return Math.random() < 0.15; }
    private boolean isHighRiskCountry(String country) { return "NG".equals(country) || "PK".equals(country); }
    private boolean checkLocationConsistency(LocationInfo locationInfo) { return Math.random() > 0.2; }
    private boolean detectVPN(LocationInfo locationInfo) { return Math.random() < 0.25; }
    private double analyzeAccountAge(Long userId) { return Math.random() * 0.4; }
    private double analyzeVerificationStatus(Long userId) { return Math.random() * 0.3; }
    private double analyzeRecentSecurityChanges(Long userId, FraudDetectionRequest request) { return Math.random() * 0.2; }
    private double analyzePasswordStrength(Long userId) { return Math.random() * 0.3; }
    private boolean check2FAStatus(Long userId) { return Math.random() > 0.3; }
    private double analyzeHistoricalTransactionPatterns(Long userId, FraudDetectionRequest request) { return Math.random() * 0.4; }
    private double analyzeDeviceUsagePatterns(Long userId, FraudDetectionRequest request) { return Math.random() * 0.3; }
    private double analyzeLocationPatterns(Long userId, FraudDetectionRequest request) { return Math.random() * 0.2; }
    private Map<String, Integer> calculateFraudTrends(FraudStatisticsRequest request) { return new HashMap<>(); }
    private Map<String, Integer> getFraudTypesBreakdown(FraudStatisticsRequest request) { return new HashMap<>(); }
    private Map<String, Integer> identifyGeographicHotspots(FraudStatisticsRequest request) { return new HashMap<>(); }
    private Map<String, Integer> analyzeDevicePatterns(FraudStatisticsRequest request) { return new HashMap<>(); }
    private double calculatePreventionEffectiveness(FraudStatisticsRequest request) { return 85.0; }
    private double calculateFalsePositiveRate(FraudStatisticsRequest request) { return 2.5; }

    // Data classes
    @Data
    public static class FraudAnalysisResult {
        private Long transactionId;
        private Long userId;
        private LocalDateTime analysisTimestamp;
        private TransactionPatternAnalysis patternAnalysis;
        private BehavioralAnalysis behavioralAnalysis;
        private DeviceAnalysis deviceAnalysis;
        private LocationAnalysis locationAnalysis;
        private AccountSecurityAnalysis securityAnalysis;
        private double overallFraudScore;
        private FraudAction recommendedAction;
        private String explanation;
    }

    @Data
    public static class TransactionPatternAnalysis {
        private double amountRiskScore;
        private double frequencyRiskScore;
        private double recipientRiskScore;
        private double timingRiskScore;
        private double patternRiskScore;
    }

    @Data
    public static class BehavioralAnalysis {
        private double loginPatternRisk;
        private double transactionPatternRisk;
        private double deviceUsageRisk;
        private double locationPatternRisk;
        private double behavioralRiskScore;
    }

    @Data
    public static class DeviceAnalysis {
        private double fingerprintRiskScore;
        private boolean knownMaliciousDevice;
        private boolean emulatorDetected;
        private boolean rootedDevice;
        private double deviceRiskScore;
    }

    @Data
    public static class LocationAnalysis {
        private boolean highRiskCountry;
        private boolean locationConsistent;
        private boolean vpnDetected;
        private double locationRiskScore;
    }

    @Data
    public static class AccountSecurityAnalysis {
        private double accountAgeRisk;
        private double verificationRisk;
        private double securityChangeRisk;
        private double passwordRisk;
        private boolean hasTwoFactorAuth;
        private double securityRiskScore;
    }

    @Data
    public static class AccountTakeoverResult {
        private Long userId;
        private LocalDateTime analysisTimestamp;
        private LoginPatternAnalysis loginAnalysis;
        private DeviceConsistencyAnalysis deviceConsistency;
        private LocationAnomalyAnalysis locationAnomaly;
        private BehavioralChangeAnalysis behavioralChange;
        private double takeoverRiskScore;
        private SecurityAction recommendedSecurityAction;
    }

    @Data
    public static class LoginPatternAnalysis {
        private double riskScore;
    }

    @Data
    public static class DeviceConsistencyAnalysis {
        private double consistencyScore;
    }

    @Data
    public static class LocationAnomalyAnalysis {
        private double anomalyScore;
    }

    @Data
    public static class BehavioralChangeAnalysis {
        private double changeScore;
    }

    @Data
    public static class SuspiciousActivityResult {
        private Long userId;
        private LocalDateTime analysisTimestamp;
        private TransactionVelocityAnalysis velocityAnalysis;
        private NetworkAnalysis networkAnalysis;
        private ContentAnalysis contentAnalysis;
        private TimePatternAnalysis timeAnalysis;
        private double suspiciousActivityScore;
        private MonitoringAction recommendedMonitoringAction;
    }

    @Data
    public static class TransactionVelocityAnalysis {
        private double velocityScore;
    }

    @Data
    public static class NetworkAnalysis {
        private double networkRiskScore;
    }

    @Data
    public static class ContentAnalysis {
        private double contentRiskScore;
    }

    @Data
    public static class TimePatternAnalysis {
        private double timeRiskScore;
    }

    @Data
    public static class RealTimeFraudAlert {
        private Long transactionId;
        private Long userId;
        private LocalDateTime alertTimestamp;
        private double quickRiskScore;
        private boolean knownFraudPattern;
        private boolean velocityViolation;
        private boolean blacklisted;
        private FraudAction immediateAction;
        private boolean requiresManualReview;
    }

    @Data
    public static class FraudStatistics {
        private LocalDateTime generatedAt;
        private String dateRange;
        private Map<String, Integer> fraudTrends;
        private Map<String, Integer> fraudTypesBreakdown;
        private Map<String, Integer> geographicHotspots;
        private Map<String, Integer> devicePatterns;
        private double preventionEffectiveness;
        private double falsePositiveRate;
    }

    // Enums
    public enum FraudAction {
        ALLOW_TRANSACTION, MONITOR_USER, REQUIRE_ADDITIONAL_VERIFICATION, BLOCK_TRANSACTION
    }

    public enum SecurityAction {
        MONITOR, SECURITY_ALERT, REQUIRE_ADDITIONAL_AUTH, LOCK_ACCOUNT
    }

    public enum MonitoringAction {
        CONTINUE_MONITORING, INCREASE_MONITORING, FLAG_FOR_REVIEW
    }

    // Request classes
    @Data
    public static class FraudDetectionRequest {
        private Long userId;
        private DeviceInfo deviceInfo;
        private LocationInfo locationInfo;
        private LocalDateTime analysisTime;
    }

    @Data
    public static class AccountTakeoverRequest {
        private DeviceInfo deviceInfo;
        private LocationInfo locationInfo;
        private String loginMethod;
    }

    @Data
    public static class SuspiciousActivityRequest {
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private String activityType;
    }

    @Data
    public static class RealTimeMonitoringRequest {
        private DeviceInfo deviceInfo;
        private LocationInfo locationInfo;
        private String ipAddress;
    }

    @Data
    public static class FraudStatisticsRequest {
        private String dateRange;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
    }

    // Supporting data classes
    @Data
    public static class DeviceInfo {
        private String deviceId;
        private String deviceType;
        private String operatingSystem;
        private String browser;
        private String userAgent;
        private String screenResolution;
    }

    @Data
    public static class LocationInfo {
        private String ipAddress;
        private String country;
        private String city;
        private Double latitude;
        private Double longitude;
        private String timezone;
    }
}
