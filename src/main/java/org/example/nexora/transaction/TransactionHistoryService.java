package org.example.nexora.transaction;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.nexora.common.BusinessException;
import org.example.nexora.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Comprehensive Transaction History tracking system providing:
 * - Complete transaction audit trail
 * - Advanced filtering and search
 * - Financial analytics and reporting
 * - Export capabilities
 * - Transaction categorization
 * - Risk assessment integration
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionHistoryService {

    private final TransactionRepository transactionRepository;
    private final TransactionAnalyticsService analyticsService;

    /**
     * Get comprehensive transaction history for a user
     */
    public TransactionHistory getUserTransactionHistory(Long userId, TransactionHistoryRequest request) {
        log.info("Getting transaction history for user {}", userId);

        TransactionHistory history = new TransactionHistory();
        history.setUserId(userId);
        history.setGeneratedAt(LocalDateTime.now());

        // Get transactions with filtering
        Page<Transaction> transactions = getFilteredTransactions(userId, request);
        history.setTransactions(transactions.getContent());

        // Calculate summary statistics
        TransactionSummary summary = calculateTransactionSummary(userId, transactions.getContent());
        history.setSummary(summary);

        // Categorize transactions
        Map<String, List<Transaction>> categorizedTransactions = categorizeTransactions(transactions.getContent());
        history.setCategorizedTransactions(categorizedTransactions);

        // Calculate analytics
        TransactionAnalytics analytics = analyticsService.calculateUserAnalytics(userId, transactions.getContent());
        history.setAnalytics(analytics);

        return history;
    }

    /**
     * Get detailed transaction information
     */
    public TransactionDetail getTransactionDetail(Long transactionId, Long userId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new BusinessException("Transaction not found"));

        // Verify user can access this transaction
        if (!transaction.getFromUserId().equals(userId) && !transaction.getToUserId().equals(userId)) {
            throw new BusinessException("Not authorized to view this transaction");
        }

        TransactionDetail detail = new TransactionDetail();
        detail.setTransaction(transaction);

        // Get related information
        detail.setFromUser(getUserById(transaction.getFromUserId()));
        detail.setToUser(getUserById(transaction.getToUserId()));

        // Calculate fees and breakdown
        TransactionBreakdown breakdown = calculateTransactionBreakdown(transaction);
        detail.setBreakdown(breakdown);

        // Get transaction history (related transactions)
        List<Transaction> relatedTransactions = getRelatedTransactions(transaction, userId);
        detail.setRelatedTransactions(relatedTransactions);

        // Risk assessment
        TransactionRiskAssessment riskAssessment = assessTransactionRisk(transaction);
        detail.setRiskAssessment(riskAssessment);

        return detail;
    }

    /**
     * Search transactions with advanced filters
     */
    public Page<Transaction> searchTransactions(TransactionSearchRequest request, Pageable pageable) {
        return transactionRepository.searchTransactions(
                request.getUserId(),
                request.getTransactionType(),
                request.getStatus(),
                request.getMinAmount(),
                request.getMaxAmount(),
                request.getStartDate(),
                request.getEndDate(),
                request.getKeyword(),
                pageable
        );
    }

    /**
     * Get transaction statistics for a user
     */
    public TransactionStatistics getUserTransactionStatistics(Long userId, StatisticsRequest request) {
        List<Transaction> transactions = getTransactionsForPeriod(userId, request.getStartDate(), request.getEndDate());

        TransactionStatistics statistics = new TransactionStatistics();
        statistics.setUserId(userId);
        statistics.setPeriod(request.getPeriod());

        // Basic counts
        statistics.setTotalTransactions(transactions.size());
        statistics.setSuccessfulTransactions(transactions.stream().filter(t -> "COMPLETED".equals(t.getStatus())).count());
        statistics.setFailedTransactions(transactions.stream().filter(t -> "FAILED".equals(t.getStatus())).count());

        // Financial totals
        BigDecimal totalSent = transactions.stream()
                .filter(t -> t.getFromUserId().equals(userId) && "COMPLETED".equals(t.getStatus()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalReceived = transactions.stream()
                .filter(t -> t.getToUserId().equals(userId) && "COMPLETED".equals(t.getStatus()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        statistics.setTotalSent(totalSent);
        statistics.setTotalReceived(totalReceived);
        statistics.setNetAmount(totalReceived.subtract(totalSent));

        // Average amounts
        long successfulCount = statistics.getSuccessfulTransactions();
        if (successfulCount > 0) {
            statistics.setAverageTransactionAmount(
                    transactions.stream()
                            .filter(t -> "COMPLETED".equals(t.getStatus()))
                            .map(Transaction::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .divide(BigDecimal.valueOf(successfulCount), 2, RoundingMode.HALF_UP)
            );
        }

        // Transaction type breakdown
        Map<String, Long> typeBreakdown = transactions.stream()
                .collect(Collectors.groupingBy(Transaction::getType, Collectors.counting()));
        statistics.setTypeBreakdown(typeBreakdown);

        // Monthly trends
        Map<String, BigDecimal> monthlyTrends = calculateMonthlyTrends(transactions);
        statistics.setMonthlyTrends(monthlyTrends);

        return statistics;
    }

    /**
     * Export transaction history
     */
    public TransactionExport exportTransactionHistory(Long userId, ExportRequest request) {
        List<Transaction> transactions = getTransactionsForPeriod(userId, request.getStartDate(), request.getEndDate());

        TransactionExport export = new TransactionExport();
        export.setUserId(userId);
        export.setExportFormat(request.getFormat());
        export.setExportedAt(LocalDateTime.now());
        export.setTotalTransactions(transactions.size());

        switch (request.getFormat().toUpperCase()) {
            case "CSV":
                export.setExportData(generateCsvExport(transactions));
                break;
            case "PDF":
                export.setExportData(generatePdfExport(transactions));
                break;
            case "EXCEL":
                export.setExportData(generateExcelExport(transactions));
                break;
            default:
                throw new BusinessException("Unsupported export format: " + request.getFormat());
        }

        return export;
    }

    /**
     * Get transaction categories for a user
     */
    public List<TransactionCategory> getUserTransactionCategories(Long userId) {
        List<Transaction> transactions = transactionRepository.findByFromUserIdOrToUserId(userId, userId);

        Map<String, TransactionCategory> categories = new HashMap<>();

        for (Transaction transaction : transactions) {
            String category = categorizeTransaction(transaction);
            TransactionCategory categoryObj = categories.computeIfAbsent(category, TransactionCategory::new);
            categoryObj.addTransaction(transaction);
        }

        return new ArrayList<>(categories.values());
    }

    /**
     * Get recurring transactions
     */
    public List<RecurringTransaction> getRecurringTransactions(Long userId) {
        List<Transaction> transactions = transactionRepository.findByFromUserId(userId);

        // Identify patterns for recurring transactions
        Map<String, List<Transaction>> patterns = identifyRecurringPatterns(transactions);

        List<RecurringTransaction> recurringTransactions = new ArrayList<>();

        for (Map.Entry<String, List<Transaction>> entry : patterns.entrySet()) {
            if (entry.getValue().size() >= 2) { // At least 2 occurrences to be considered recurring
                RecurringTransaction recurring = new RecurringTransaction();
                recurring.setPattern(entry.getKey());
                recurring.setTransactions(entry.getValue());
                recurring.setFrequency(calculateFrequency(entry.getValue()));
                recurring.setAverageAmount(calculateAverageAmount(entry.getValue()));
                recurring.setNextExpectedDate(calculateNextExpectedDate(entry.getValue()));
                recurringTransactions.add(recurring);
            }
        }

        return recurringTransactions;
    }

    /**
     * Get transaction insights and recommendations
     */
    public TransactionInsights getTransactionInsights(Long userId) {
        List<Transaction> transactions = transactionRepository.findByFromUserIdOrToUserId(userId, userId);

        TransactionInsights insights = new TransactionInsights();

        // Spending patterns
        insights.setTopSpendingCategories(getTopSpendingCategories(transactions));
        insights.setSpendingTrends(calculateSpendingTrends(transactions));
        insights.setUnusualTransactions(identifyUnusualTransactions(transactions));

        // Income analysis
        insights.setIncomeSources(getIncomeSources(transactions));
        insights.setIncomeTrends(calculateIncomeTrends(transactions));

        // Recommendations
        insights.setRecommendations(generateTransactionRecommendations(transactions));

        // Financial health score
        insights.setFinancialHealthScore(calculateFinancialHealthScore(transactions));

        return insights;
    }

    // Private helper methods
    private Page<Transaction> getFilteredTransactions(Long userId, TransactionHistoryRequest request) {
        return transactionRepository.searchTransactions(
                userId,
                request.getTransactionType(),
                request.getStatus(),
                request.getMinAmount(),
                request.getMaxAmount(),
                request.getStartDate(),
                request.getEndDate(),
                request.getKeyword(),
                request.getPageable()
        );
    }

    private TransactionSummary calculateTransactionSummary(Long userId, List<Transaction> transactions) {
        TransactionSummary summary = new TransactionSummary();

        summary.setTotalTransactions(transactions.size());
        summary.setSuccessfulTransactions(transactions.stream().filter(t -> "COMPLETED".equals(t.getStatus())).count());

        BigDecimal totalSent = transactions.stream()
                .filter(t -> t.getFromUserId().equals(userId) && "COMPLETED".equals(t.getStatus()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalReceived = transactions.stream()
                .filter(t -> t.getToUserId().equals(userId) && "COMPLETED".equals(t.getStatus()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        summary.setTotalSent(totalSent);
        summary.setTotalReceived(totalReceived);
        summary.setNetAmount(totalReceived.subtract(totalSent));

        return summary;
    }

    private Map<String, List<Transaction>> categorizeTransactions(List<Transaction> transactions) {
        return transactions.stream()
                .collect(Collectors.groupingBy(this::categorizeTransaction));
    }

    private String categorizeTransaction(Transaction transaction) {
        // Categorize based on transaction type and description
        if (transaction.getDescription() != null) {
            String desc = transaction.getDescription().toLowerCase();
            if (desc.contains("transfer") || desc.contains("send")) return "Peer-to-Peer Transfer";
            if (desc.contains("withdraw") || desc.contains("cash out")) return "Withdrawal";
            if (desc.contains("deposit") || desc.contains("add money")) return "Deposit";
            if (desc.contains("earning") || desc.contains("revenue")) return "Earnings";
            if (desc.contains("fee") || desc.contains("charge")) return "Fees";
            if (desc.contains("refund") || desc.contains("return")) return "Refund";
        }

        return transaction.getType();
    }

    private TransactionBreakdown calculateTransactionBreakdown(Transaction transaction) {
        TransactionBreakdown breakdown = new TransactionBreakdown();
        breakdown.setAmount(transaction.getAmount());
        breakdown.setFee(transaction.getFee());
        breakdown.setTotal(transaction.getAmount().add(transaction.getFee()));
        breakdown.setNetAmount(transaction.getAmount());
        return breakdown;
    }

    private List<Transaction> getRelatedTransactions(Transaction transaction, Long userId) {
        // Get transactions with same users
        return transactionRepository.findByFromUserIdOrToUserIdAndIdNot(
                transaction.getFromUserId(),
                transaction.getToUserId(),
                transaction.getId()
        ).stream()
                .filter(t -> t.getFromUserId().equals(userId) || t.getToUserId().equals(userId))
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(10)
                .collect(Collectors.toList());
    }

    private TransactionRiskAssessment assessTransactionRisk(Transaction transaction) {
        TransactionRiskAssessment assessment = new TransactionRiskAssessment();
        
        // Simple risk assessment based on amount and frequency
        double riskScore = 0.0;
        
        if (transaction.getAmount().compareTo(BigDecimal.valueOf(10000)) > 0) {
            riskScore += 0.3;
        }
        if (transaction.getAmount().compareTo(BigDecimal.valueOf(50000)) > 0) {
            riskScore += 0.4;
        }
        
        assessment.setRiskScore(Math.min(riskScore, 1.0));
        assessment.setRiskLevel(riskScore > 0.7 ? "HIGH" : riskScore > 0.3 ? "MEDIUM" : "LOW");
        
        return assessment;
    }

    private List<Transaction> getTransactionsForPeriod(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.findByFromUserIdOrToUserIdAndCreatedAtBetween(userId, userId, startDate, endDate);
    }

    private Map<String, BigDecimal> calculateMonthlyTrends(List<Transaction> transactions) {
        return transactions.stream()
                .filter(t -> "COMPLETED".equals(t.getStatus()))
                .collect(Collectors.groupingBy(
                        t -> t.getCreatedAt().getYear() + "-" + String.format("%02d", t.getCreatedAt().getMonthValue()),
                        Collectors.mapping(Transaction::getAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));
    }

    private byte[] generateCsvExport(List<Transaction> transactions) {
        // Placeholder for CSV generation
        return "CSV export data".getBytes();
    }

    private byte[] generatePdfExport(List<Transaction> transactions) {
        // Placeholder for PDF generation
        return "PDF export data".getBytes();
    }

    private byte[] generateExcelExport(List<Transaction> transactions) {
        // Placeholder for Excel generation
        return "Excel export data".getBytes();
    }

    private Map<String, List<Transaction>> identifyRecurringPatterns(List<Transaction> transactions) {
        // Simplified pattern identification
        Map<String, List<Transaction>> patterns = new HashMap<>();
        
        // Group by recipient and amount
        transactions.stream()
                .filter(t -> "COMPLETED".equals(t.getStatus()))
                .forEach(t -> {
                    String pattern = t.getToUserId() + "-" + t.getAmount();
                    patterns.computeIfAbsent(pattern, k -> new ArrayList<>()).add(t);
                });
        
        return patterns;
    }

    private String calculateFrequency(List<Transaction> transactions) {
        if (transactions.size() < 2) return "UNKNOWN";
        
        // Simple frequency calculation
        long daysBetween = java.time.Duration.between(
                transactions.get(transactions.size() - 1).getCreatedAt(),
                transactions.get(0).getCreatedAt()
        ).toDays();
        
        if (daysBetween < 7) return "DAILY";
        if (daysBetween < 30) return "WEEKLY";
        if (daysBetween < 90) return "MONTHLY";
        return "IRREGULAR";
    }

    private BigDecimal calculateAverageAmount(List<Transaction> transactions) {
        return transactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(transactions.size()), 2, RoundingMode.HALF_UP);
    }

    private LocalDateTime calculateNextExpectedDate(List<Transaction> transactions) {
        // Simple calculation based on last transaction
        return transactions.get(transactions.size() - 1).getCreatedAt().plusDays(30);
    }

    private List<String> getTopSpendingCategories(List<Transaction> transactions) {
        return transactions.stream()
                .filter(t -> t.getFromUserId().equals(transactions.get(0).getFromUserId()))
                .collect(Collectors.groupingBy(this::categorizeTransaction, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private Map<String, BigDecimal> calculateSpendingTrends(List<Transaction> transactions) {
        // Placeholder for spending trends calculation
        return new HashMap<>();
    }

    private List<Transaction> identifyUnusualTransactions(List<Transaction> transactions) {
        // Simple unusual transaction detection based on amount
        BigDecimal averageAmount = calculateAverageAmount(transactions);
        BigDecimal threshold = averageAmount.multiply(BigDecimal.valueOf(3));
        
        return transactions.stream()
                .filter(t -> t.getAmount().compareTo(threshold) > 0)
                .collect(Collectors.toList());
    }

    private Map<String, BigDecimal> getIncomeSources(List<Transaction> transactions) {
        return transactions.stream()
                .filter(t -> t.getToUserId().equals(transactions.get(0).getToUserId()) && "COMPLETED".equals(t.getStatus()))
                .collect(Collectors.groupingBy(
                        t -> t.getFromUserId().toString(), // Simplified - would use user names
                        Collectors.mapping(Transaction::getAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));
    }

    private Map<String, BigDecimal> calculateIncomeTrends(List<Transaction> transactions) {
        // Placeholder for income trends calculation
        return new HashMap<>();
    }

    private List<String> generateTransactionRecommendations(List<Transaction> transactions) {
        List<String> recommendations = new ArrayList<>();
        
        // Simple recommendations based on transaction patterns
        long failedTransactions = transactions.stream().filter(t -> "FAILED".equals(t.getStatus())).count();
        if (failedTransactions > transactions.size() * 0.1) {
            recommendations.add("Consider reviewing failed transactions and payment methods");
        }
        
        BigDecimal totalAmount = transactions.stream()
                .filter(t -> "COMPLETED".equals(t.getStatus()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (totalAmount.compareTo(BigDecimal.valueOf(100000)) > 0) {
            recommendations.add("Consider setting up automatic savings for large transactions");
        }
        
        return recommendations;
    }

    private double calculateFinancialHealthScore(List<Transaction> transactions) {
        // Simple health score calculation
        long successfulTransactions = transactions.stream().filter(t -> "COMPLETED".equals(t.getStatus())).count();
        double successRate = transactions.size() > 0 ? (double) successfulTransactions / transactions.size() : 0.0;
        
        return successRate * 100; // Simple score based on success rate
    }

    // Placeholder methods for user retrieval
    private User getUserById(Long userId) {
        // Would use UserRepository
        return null;
    }

    // Data classes
    @Data
    public static class TransactionHistory {
        private Long userId;
        private LocalDateTime generatedAt;
        private List<Transaction> transactions;
        private TransactionSummary summary;
        private Map<String, List<Transaction>> categorizedTransactions;
        private TransactionAnalytics analytics;
    }

    @Data
    public static class TransactionSummary {
        private int totalTransactions;
        private long successfulTransactions;
        private BigDecimal totalSent;
        private BigDecimal totalReceived;
        private BigDecimal netAmount;
    }

    @Data
    public static class TransactionDetail {
        private Transaction transaction;
        private User fromUser;
        private User toUser;
        private TransactionBreakdown breakdown;
        private List<Transaction> relatedTransactions;
        private TransactionRiskAssessment riskAssessment;
    }

    @Data
    public static class TransactionBreakdown {
        private BigDecimal amount;
        private BigDecimal fee;
        private BigDecimal total;
        private BigDecimal netAmount;
    }

    @Data
    public static class TransactionRiskAssessment {
        private double riskScore;
        private String riskLevel;
        private List<String> riskFactors;
    }

    @Data
    public static class TransactionStatistics {
        private Long userId;
        private String period;
        private int totalTransactions;
        private long successfulTransactions;
        private long failedTransactions;
        private BigDecimal totalSent;
        private BigDecimal totalReceived;
        private BigDecimal netAmount;
        private BigDecimal averageTransactionAmount;
        private Map<String, Long> typeBreakdown;
        private Map<String, BigDecimal> monthlyTrends;
    }

    @Data
    public static class TransactionExport {
        private Long userId;
        private String exportFormat;
        private LocalDateTime exportedAt;
        private int totalTransactions;
        private byte[] exportData;
    }

    @Data
    public static class TransactionCategory {
        private String name;
        private List<Transaction> transactions;
        private BigDecimal totalAmount;
        private int transactionCount;

        public TransactionCategory(String name) {
            this.name = name;
            this.transactions = new ArrayList<>();
        }

        public void addTransaction(Transaction transaction) {
            this.transactions.add(transaction);
            this.transactionCount = transactions.size();
            this.totalAmount = transactions.stream()
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }

    @Data
    public static class RecurringTransaction {
        private String pattern;
        private List<Transaction> transactions;
        private String frequency;
        private BigDecimal averageAmount;
        private LocalDateTime nextExpectedDate;
    }

    @Data
    public static class TransactionInsights {
        private List<String> topSpendingCategories;
        private Map<String, BigDecimal> spendingTrends;
        private List<Transaction> unusualTransactions;
        private Map<String, BigDecimal> incomeSources;
        private Map<String, BigDecimal> incomeTrends;
        private List<String> recommendations;
        private double financialHealthScore;
    }

    // Request classes
    @Data
    public static class TransactionHistoryRequest {
        private String transactionType;
        private String status;
        private BigDecimal minAmount;
        private BigDecimal maxAmount;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private String keyword;
        private Pageable pageable;
    }

    @Data
    public static class TransactionSearchRequest {
        private Long userId;
        private String transactionType;
        private String status;
        private BigDecimal minAmount;
        private BigDecimal maxAmount;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private String keyword;
    }

    @Data
    public static class StatisticsRequest {
        private String period;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
    }

    @Data
    public static class ExportRequest {
        private String format;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
    }
}
