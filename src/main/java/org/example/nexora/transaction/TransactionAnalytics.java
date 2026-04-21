package org.example.nexora.transaction;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Analytics data for transactions
 */
@Data
public class TransactionAnalytics {
    
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private BigDecimal totalAmount;
    private BigDecimal averageAmount;
    private Long transactionCount;
    private BigDecimal successRate;
    private String topCategory;
    private Map<String, BigDecimal> categoryBreakdown;
    
    public TransactionAnalytics() {
        this.totalAmount = BigDecimal.ZERO;
        this.averageAmount = BigDecimal.ZERO;
        this.transactionCount = 0L;
        this.successRate = BigDecimal.ZERO;
    }
}
