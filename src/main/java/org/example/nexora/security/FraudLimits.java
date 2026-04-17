package org.example.nexora.security;

import java.math.BigDecimal;

public class FraudLimits {

    private final BigDecimal dailyLimit;
    private final BigDecimal singleTransferLimit;
    private final int maxTransfersPerMinute;

    public FraudLimits(BigDecimal dailyLimit,
                       BigDecimal singleTransferLimit,
                       int maxTransfersPerMinute) {
        this.dailyLimit = dailyLimit;
        this.singleTransferLimit = singleTransferLimit;
        this.maxTransfersPerMinute = maxTransfersPerMinute;
    }

    public BigDecimal getDailyLimit() {
        return dailyLimit;
    }

    public BigDecimal getSingleTransferLimit() {
        return singleTransferLimit;
    }

    public int getMaxTransfersPerMinute() {
        return maxTransfersPerMinute;
    }
}