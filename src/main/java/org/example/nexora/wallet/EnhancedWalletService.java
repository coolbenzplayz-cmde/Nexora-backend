package org.example.nexora.wallet;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.nexora.common.BusinessException;
import org.example.nexora.transaction.Transaction;
import org.example.nexora.transaction.TransactionService;
import org.example.nexora.user.User;
import org.example.nexora.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnhancedWalletService {

    private final EnhancedWalletRepository walletRepository;
    private final UserRepository userRepository;
    private final TransactionService transactionService;

    @Transactional
    public Wallet createWallet(User user) {
        if (user.getId() == null) {
            user = userRepository.save(user);
        }
        
        return walletRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Wallet wallet = new Wallet();
                    wallet.setUser(user);
                    wallet.setBalance(BigDecimal.ZERO);
                    wallet.setAvailableBalance(BigDecimal.ZERO);
                    wallet.setFrozenBalance(BigDecimal.ZERO);
                    wallet.setCurrency("USD");
                    wallet.setStatus(Wallet.WalletStatus.ACTIVE);
                    wallet.setWalletAddress(generateWalletAddress());
                    return walletRepository.save(wallet);
                });
    }

    @Transactional
    public Wallet addMoney(Long userId, BigDecimal amount, String description) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Amount must be positive");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));
        
        Wallet wallet = getOrCreateWallet(userId);
        wallet.addBalance(amount);
        
        Wallet saved = walletRepository.save(wallet);
        
        // Record transaction
        Transaction transaction = new Transaction();
        transaction.setReference("DEPOSIT-" + System.currentTimeMillis());
        transaction.setFromUserId(null);
        transaction.setToUserId(userId);
        transaction.setAmount(amount);
        transaction.setStatus("COMPLETED");
        transaction.setDescription(description != null ? description : "Wallet deposit");
        transaction.setCreatedAt(LocalDateTime.now());
        transactionService.saveTransaction(transaction);
        
        log.info("Added {} to wallet for user {}. New balance: {}", amount, userId, saved.getBalance());
        return saved;
    }

    @Transactional
    public String transfer(TransferRequest request) {
        Long senderId = request.getSenderId();
        Long receiverId = request.getReceiverId();
        BigDecimal amount = request.getAmount();

        if (senderId.equals(receiverId)) {
            throw new BusinessException("Cannot transfer to yourself");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Transfer amount must be positive");
        }

        Wallet from = getWallet(senderId);
        Wallet to = getWallet(receiverId);

        // Check if sender can transfer
        if (!from.canTransfer(amount)) {
            throw new BusinessException("Transfer not allowed: insufficient balance or limits exceeded");
        }

        // Perform transfer
        from.subtractBalance(amount);
        from.recordTransferOut(amount);
        
        to.addBalance(amount);
        to.recordTransferIn(amount);

        walletRepository.save(from);
        walletRepository.save(to);

        String reference = "TRF-" + System.currentTimeMillis();
        
        // Record transaction
        Transaction transaction = new Transaction();
        transaction.setReference(reference);
        transaction.setFromUserId(senderId);
        transaction.setToUserId(receiverId);
        transaction.setAmount(amount);
        transaction.setStatus("COMPLETED");
        transaction.setDescription("Peer-to-peer transfer");
        transaction.setCreatedAt(LocalDateTime.now());
        transactionService.saveTransaction(transaction);

        log.info("Transferred {} from user {} to user {}. Reference: {}", amount, senderId, receiverId, reference);
        return "Transfer successful: " + reference;
    }

    @Transactional
    public void freezeAmount(Long userId, BigDecimal amount, String reason) {
        Wallet wallet = getWallet(userId);
        
        if (!wallet.hasSufficientBalance(amount)) {
            throw new BusinessException("Insufficient balance to freeze");
        }

        wallet.freezeAmount(amount);
        walletRepository.save(wallet);

        log.info("Froze {} in wallet for user {}. Reason: {}", amount, userId, reason);
    }

    @Transactional
    public void unfreezeAmount(Long userId, BigDecimal amount, String reason) {
        Wallet wallet = getWallet(userId);
        
        if (wallet.getFrozenBalance().compareTo(amount) < 0) {
            throw new BusinessException("Insufficient frozen balance to unfreeze");
        }

        wallet.unfreezeAmount(amount);
        walletRepository.save(wallet);

        log.info("Unfroze {} in wallet for user {}. Reason: {}", amount, userId, reason);
    }

    @Transactional
    public void addEarnings(Long userId, BigDecimal amount, String description) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Earnings amount must be positive");
        }

        Wallet wallet = getOrCreateWallet(userId);
        wallet.addEarnings(amount);
        
        Wallet saved = walletRepository.save(wallet);

        // Record transaction
        Transaction transaction = new Transaction();
        transaction.setReference("EARN-" + System.currentTimeMillis());
        transaction.setFromUserId(null);
        transaction.setToUserId(userId);
        transaction.setAmount(amount);
        transaction.setStatus("COMPLETED");
        transaction.setDescription(description != null ? description : "Creator earnings");
        transaction.setCreatedAt(LocalDateTime.now());
        transactionService.saveTransaction(transaction);

        log.info("Added {} earnings to wallet for user {}. New balance: {}", amount, userId, saved.getBalance());
    }

    @Transactional
    public void setWalletLimits(Long userId, BigDecimal dailyLimit, BigDecimal monthlyLimit) {
        Wallet wallet = getWallet(userId);
        wallet.setDailyLimit(dailyLimit);
        wallet.setMonthlyLimit(monthlyLimit);
        walletRepository.save(wallet);

        log.info("Set wallet limits for user {}: daily={}, monthly={}", userId, dailyLimit, monthlyLimit);
    }

    @Transactional
    public void freezeWallet(Long userId, String reason) {
        Wallet wallet = getWallet(userId);
        wallet.freeze();
        walletRepository.save(wallet);

        log.info("Froze wallet for user {}. Reason: {}", userId, reason);
    }

    @Transactional
    public void unfreezeWallet(Long userId, String reason) {
        Wallet wallet = getWallet(userId);
        wallet.unfreeze();
        walletRepository.save(wallet);

        log.info("Unfroze wallet for user {}. Reason: {}", userId, reason);
    }

    @Transactional
    public void suspendWallet(Long userId, String reason) {
        Wallet wallet = getWallet(userId);
        wallet.suspend();
        walletRepository.save(wallet);

        log.info("Suspended wallet for user {}. Reason: {}", userId, reason);
    }

    @Transactional
    public void verifyWallet(Long userId, int verificationLevel) {
        Wallet wallet = getWallet(userId);
        wallet.verify(verificationLevel);
        walletRepository.save(wallet);

        log.info("Verified wallet for user {} at level {}", userId, verificationLevel);
    }

    public Wallet getWallet(Long userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("Wallet not found for user: " + userId));
    }

    public Wallet getOrCreateWallet(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found: " + userId));
        
        return walletRepository.findByUserId(userId)
                .orElseGet(() -> createWallet(user));
    }

    public Wallet getWalletByAddress(String walletAddress) {
        return walletRepository.findByWalletAddress(walletAddress)
                .orElseThrow(() -> new BusinessException("Wallet not found with address: " + walletAddress));
    }

    public boolean hasSufficientBalance(Long userId, BigDecimal amount) {
        Wallet wallet = getWallet(userId);
        return wallet.hasSufficientBalance(amount);
    }

    public boolean canTransfer(Long userId, BigDecimal amount) {
        Wallet wallet = getWallet(userId);
        return wallet.canTransfer(amount);
    }

    public WalletSummary getWalletSummary(Long userId) {
        Wallet wallet = getWallet(userId);
        
        return WalletSummary.builder()
                .walletId(wallet.getId())
                .userId(userId)
                .balance(wallet.getBalance())
                .availableBalance(wallet.getAvailableBalance())
                .frozenBalance(wallet.getFrozenBalance())
                .totalEarned(wallet.getTotalEarned())
                .totalWithdrawn(wallet.getTotalWithdrawn())
                .totalDeposited(wallet.getTotalDeposited())
                .totalTransferredIn(wallet.getTotalTransferredIn())
                .totalTransferredOut(wallet.getTotalTransferredOut())
                .status(wallet.getStatus())
                .currency(wallet.getCurrency())
                .isVerified(wallet.getIsVerified())
                .verificationLevel(wallet.getVerificationLevel())
                .walletAddress(wallet.getWalletAddress())
                .dailyLimit(wallet.getDailyLimit())
                .monthlyLimit(wallet.getMonthlyLimit())
                .dailySpent(wallet.getDailySpent())
                .monthlySpent(wallet.getMonthlySpent())
                .createdAt(wallet.getCreatedAt())
                .build();
    }

    private String generateWalletAddress() {
        return "NX" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }

    public BigDecimal getTotalPlatformBalance() {
        return walletRepository.sumTotalBalance();
    }

    public long getTotalWalletsCount() {
        return walletRepository.count();
    }

    public long getActiveWalletsCount() {
        return walletRepository.countByStatus(Wallet.WalletStatus.ACTIVE);
    }

    public BigDecimal getTotalVerifiedWalletsCount() {
        return BigDecimal.valueOf(walletRepository.countByIsVerifiedTrue());
    }

    public Wallet save(Wallet wallet) {
        return walletRepository.save(wallet);
    }
}
