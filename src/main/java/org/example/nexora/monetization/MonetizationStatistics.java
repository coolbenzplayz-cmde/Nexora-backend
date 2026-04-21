package org.example.nexora.monetization;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
public class MonetizationStatistics {

    private Long totalCreators;
    private BigDecimal totalEarnings;
    private BigDecimal totalAdRevenue;
    private BigDecimal totalRevenue;

    public MonetizationStatistics() {
        // Default constructor
    }

    public BigDecimal getAverageEarningsPerCreator() {
        if (totalCreators == 0) return BigDecimal.ZERO;
        return totalEarnings.divide(BigDecimal.valueOf(totalCreators), 2, BigDecimal.ROUND_HALF_UP);
    }

    public BigDecimal getAverageAdRevenuePerCreator() {
        if (totalCreators == 0) return BigDecimal.ZERO;
        return totalAdRevenue.divide(BigDecimal.valueOf(totalCreators), 2, BigDecimal.ROUND_HALF_UP);
    }

    public BigDecimal getAverageTotalRevenuePerCreator() {
        if (totalCreators == 0) return BigDecimal.ZERO;
        return totalRevenue.divide(BigDecimal.valueOf(totalCreators), 2, BigDecimal.ROUND_HALF_UP);
    }

    public double getAdRevenuePercentage() {
        if (totalRevenue.compareTo(BigDecimal.ZERO) == 0) return 0.0;
        return totalAdRevenue.divide(totalRevenue, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue();
    }
}
