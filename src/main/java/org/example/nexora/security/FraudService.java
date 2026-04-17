package org.example.nexora.security;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FraudService {

    // -----------------------------
    // TRACKING STORAGE (IN MEMORY)
    // -----------------------------
    private final Map<Long, Integer> transferCountPerMinute = new ConcurrentHashMap<>();
    private final Map<Long, Long> lastTransferTime = new ConcurrentHashMap<>();
    private final Map<Long, BigDecimal> dailyTotal = new ConcurrentHashMap<>();
    private final Map<Long, Integer> failedAttempts = new ConcurrentHashMap<>();
    private final Map<Long, Integer> riskScore = new ConcurrentHashMap<>();

    // -----------------------------
    // LIMITS (TUNE THESE LATER)
    // -----------------------------
    private static final int MAX_TRANSFERS_PER_MINUTE = 5;
    private static final BigDecimal MAX_SINGLE_TRANSFER = new BigDecimal("50000");
    private static final BigDecimal DAILY_LIMIT = new BigDecimal("1000000");;
    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final int MAX_RISK_SCORE = 10;

    // -----------------------------
    // MAIN FRAUD CHECK
    // -----------------------------
    public void checkTransfer(Long userId, BigDecimal amount) {

        long now = System.currentTimeMillis();

        // -----------------------------
        // RESET MINUTE WINDOW
        // -----------------------------
        lastTransferTime.putIfAbsent(userId, now);

        if (now - lastTransferTime.get(userId) > 60000) {
            transferCountPerMinute.put(userId, 0);
        }

        // -----------------------------
        // UPDATE TRANSFER COUNT
        // -----------------------------
        int count = transferCountPerMinute.getOrDefault(userId, 0) + 1;
        transferCountPerMinute.put(userId, count);
        lastTransferTime.put(userId, now);

        // RULE 1: too many transfers
        if (count > MAX_TRANSFERS_PER_MINUTE) {
            increaseRisk(userId);
            throw new RuntimeException("Fraud detected: too many transfers per minute");
        }

        // RULE 2: large transfer
        if (amount.compareTo(MAX_SINGLE_TRANSFER) > 0) {
            increaseRisk(userId);
            throw new RuntimeException("Fraud detected: single transfer too large");
        }

        // -----------------------------
        // DAILY LIMIT CHECK
        // -----------------------------
        BigDecimal total = dailyTotal.getOrDefault(userId, BigDecimal.ZERO);
        total = total.add(amount);
        dailyTotal.put(userId, total);

        if (total.compareTo(DAILY_LIMIT) > 0) {
            increaseRisk(userId);
            throw new RuntimeException("Fraud detected: daily limit exceeded");
        }

        // -----------------------------
        // RISK SCORE CHECK
        // -----------------------------
        int risk = riskScore.getOrDefault(userId, 0);

        if (risk >= MAX_RISK_SCORE) {
            throw new RuntimeException("Account blocked due to high risk score");
        }
    }

    // -----------------------------
    // FAILED TRANSFER TRACKING
    // -----------------------------
    public void registerFailedAttempt(Long userId) {

        int failed = failedAttempts.getOrDefault(userId, 0) + 1;
        failedAttempts.put(userId, failed);

        if (failed >= MAX_FAILED_ATTEMPTS) {
            increaseRisk(userId);
        }
    }

    // -----------------------------
    // RISK ENGINE (SIMPLE)
    // -----------------------------
    private void increaseRisk(Long userId) {
        int risk = riskScore.getOrDefault(userId, 0) + 2;
        riskScore.put(userId, risk);
    }

    // -----------------------------
    // OPTIONAL: RESET (ADMIN USE)
    // -----------------------------
    public void resetUser(Long userId) {
        transferCountPerMinute.remove(userId);
        lastTransferTime.remove(userId);
        dailyTotal.remove(userId);
        failedAttempts.remove(userId);
        riskScore.remove(userId);
    }
}