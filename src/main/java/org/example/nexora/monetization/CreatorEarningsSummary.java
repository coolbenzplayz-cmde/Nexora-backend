package org.example.nexora.monetization;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
public class CreatorEarningsSummary {

    private Long creatorId;
    private String creatorUsername;
    private BigDecimal totalEarnings;
    private BigDecimal totalAdRevenue;
    private BigDecimal unpaidEarnings;
    private BigDecimal unpaidAdRevenue;
    private BigDecimal totalUnpaid;
    private BigDecimal totalPaid;
    private Long earningsCount;
    private Long adRevenueCount;

    public CreatorEarningsSummary() {
        // Default constructor
    }

    public BigDecimal getTotalRevenue() {
        return totalEarnings.add(totalAdRevenue);
    }

    public BigDecimal getTotalUnpaidRevenue() {
        return unpaidEarnings.add(unpaidAdRevenue);
    }

    public boolean hasUnpaidEarnings() {
        return totalUnpaid.compareTo(BigDecimal.ZERO) > 0;
    }

    public double getUnpaidPercentage() {
        if (getTotalRevenue().compareTo(BigDecimal.ZERO) == 0) return 0.0;
        return getTotalUnpaidRevenue().divide(getTotalRevenue(), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue();
    }
}
