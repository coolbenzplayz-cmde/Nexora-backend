package org.example.nexora.wallet;

import org.example.nexora.security.JwtService;
import org.example.nexora.security.RiskEngine;
import org.example.nexora.user.User;
import org.example.nexora.user.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class WithdrawService {

    private final WithdrawRepository withdrawRepository;
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RiskEngine riskEngine;

    public WithdrawService(WithdrawRepository withdrawRepository,
                           WalletRepository walletRepository,
                           UserRepository userRepository,
                           JwtService jwtService,
                           RiskEngine riskEngine) {

        this.withdrawRepository = withdrawRepository;
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.riskEngine = riskEngine;
    }

    // 💳 WITHDRAW REQUEST (AUTO DECISION SYSTEM)
    public String requestWithdraw(String token, BigDecimal amount) {

        String email = jwtService.extractEmail(token.replace("Bearer ", ""));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Wallet wallet = walletRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        // 🧠 RISK SCORE
        int riskScore = riskEngine.calculateRisk(user.getId(), amount, 0);

        String status;

        if (riskScore <= 30) {
            status = "APPROVED";
        } else if (riskScore <= 70) {
            status = "PENDING_REVIEW";
        } else {
            status = "REJECTED";
        }

        // 💰 deduct only if not rejected
        if (!status.equals("REJECTED")) {
            wallet.setBalance(wallet.getBalance().subtract(amount));
            walletRepository.save(wallet);
        }

        WithdrawRequest request = new WithdrawRequest(
                user.getId(),
                amount,
                status
        );

        withdrawRepository.save(request);

        return "Withdraw status: " + status + " (risk score: " + riskScore + ")";
    }

    // HISTORY
    public List<WithdrawRequest> getMyWithdraws(String token) {
        String email = jwtService.extractEmail(token.replace("Bearer ", ""));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return withdrawRepository.findByUser_Id(user.getId());
    }
}