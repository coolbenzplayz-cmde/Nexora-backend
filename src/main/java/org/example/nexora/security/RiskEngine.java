package org.example.nexora.security;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class RiskEngine {

    public int calculateRisk(Long userId, BigDecimal amount, int pastWithdraws) {

        int risk = 0;

        // 💰 high amount risk
        if (amount.compareTo(new BigDecimal("100000")) > 0) {
            risk += 40;
        }

        if (amount.compareTo(new BigDecimal("50000")) > 0) {
            risk += 20;
        }

        // 🔁 too many withdrawals
        if (pastWithdraws > 5) {
            risk += 25;
        }

        // 🧠 simple new user risk logic
        if (userId == null || userId < 10) {
            risk += 15;
        }

        return Math.min(risk, 100);
    }
}