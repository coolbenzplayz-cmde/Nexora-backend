package org.example.nexora.security;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class FraudLimitService {

    public FraudLimits getLimitsForUser(int riskScore) {

        // 🟢 LOW RISK (BIG USERS)
        if (riskScore < 3) {
            return new FraudLimits(
                    new BigDecimal("1000000"),  // daily
                    new BigDecimal("200000"),   // per transaction
                    10                          // per minute
            );
        }

        // 🟡 MEDIUM RISK
        if (riskScore < 7) {
            return new FraudLimits(
                    new BigDecimal("500000"),
                    new BigDecimal("100000"),
                    5
            );
        }

        // 🔴 HIGH RISK
        return new FraudLimits(
                new BigDecimal("200000"),
                new BigDecimal("50000"),
                3
        );
    }
}