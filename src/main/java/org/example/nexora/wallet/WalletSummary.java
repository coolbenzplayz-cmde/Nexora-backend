package org.example.nexora.wallet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class WalletSummary {

    private Long walletId;
    private Long userId;
    private BigDecimal balance;
    private BigDecimal availableBalance;
    private BigDecimal frozenBalance;
    private BigDecimal totalEarned;
    private BigDecimal totalWithdrawn;
    private BigDecimal totalDeposited;
    private BigDecimal totalTransferredIn;
    private BigDecimal totalTransferredOut;
    private Wallet.WalletStatus status;
    private String currency;
    private Boolean isVerified;
    private Integer verificationLevel;
    private String walletAddress;
    private BigDecimal dailyLimit;
    private BigDecimal monthlyLimit;
    private BigDecimal dailySpent;
    private BigDecimal monthlySpent;
    private LocalDateTime createdAt;

    public WalletSummary() {
        // Default constructor
    }

    public BigDecimal getTotalInflow() {
        return totalDeposited.add(totalTransferredIn).add(totalEarned);
    }

    public BigDecimal getTotalOutflow() {
        return totalWithdrawn.add(totalTransferredOut);
    }

    public BigDecimal getNetBalance() {
        return getTotalInflow().subtract(getTotalOutflow());
    }

    public BigDecimal getRemainingDailyLimit() {
        if (dailyLimit == null) return null;
        return dailyLimit.subtract(dailySpent);
    }

    public BigDecimal getRemainingMonthlyLimit() {
        if (monthlyLimit == null) return null;
        return monthlyLimit.subtract(monthlySpent);
    }

    public double getDailyUtilizationPercentage() {
        if (dailyLimit == null) return 0.0;
        return dailySpent.divide(dailyLimit, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue();
    }

    public double getMonthlyUtilizationPercentage() {
        if (monthlyLimit == null) return 0.0;
        return monthlySpent.divide(monthlyLimit, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue();
    }

    public boolean hasFunds() {
        return balance.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean hasFrozenFunds() {
        return frozenBalance.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isActive() {
        return status == Wallet.WalletStatus.ACTIVE;
    }

    public boolean isFrozen() {
        return status == Wallet.WalletStatus.FROZEN;
    }

    public boolean isSuspended() {
        return status == Wallet.WalletStatus.SUSPENDED;
    }

    public boolean isClosed() {
        return status == Wallet.WalletStatus.CLOSED;
    }
}
