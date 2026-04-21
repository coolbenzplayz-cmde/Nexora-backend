package org.example.nexora.wallet;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.example.nexora.common.BaseEntity;
import org.example.nexora.user.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "wallets", indexes = {
        @Index(name = "idx_wallets_user_id", columnList = "user.id"),
        @Index(name = "idx_wallets_status", columnList = "status"),
        @Index(name = "idx_wallets_created_at", columnList = "createdAt")
})
public class Wallet extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "available_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal availableBalance = BigDecimal.ZERO;

    @Column(name = "frozen_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal frozenBalance = BigDecimal.ZERO;

    @Column(name = "total_earned", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalEarned = BigDecimal.ZERO;

    @Column(name = "total_withdrawn", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalWithdrawn = BigDecimal.ZERO;

    @Column(name = "total_deposited", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalDeposited = BigDecimal.ZERO;

    @Column(name = "total_transferred_in", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalTransferredIn = BigDecimal.ZERO;

    @Column(name = "total_transferred_out", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalTransferredOut = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private WalletStatus status = WalletStatus.ACTIVE;

    @Column(name = "currency", nullable = false)
    private String currency = "USD";

    @Column(name = "daily_limit", precision = 19, scale = 4)
    private BigDecimal dailyLimit;

    @Column(name = "monthly_limit", precision = 19, scale = 4)
    private BigDecimal monthlyLimit;

    @Column(name = "daily_spent", nullable = false, precision = 19, scale = 4)
    private BigDecimal dailySpent = BigDecimal.ZERO;

    @Column(name = "monthly_spent", nullable = false, precision = 19, scale = 4)
    private BigDecimal monthlySpent = BigDecimal.ZERO;

    @Column(name = "last_daily_reset", nullable = false)
    private LocalDateTime lastDailyReset = LocalDateTime.now();

    @Column(name = "last_monthly_reset", nullable = false)
    private LocalDateTime lastMonthlyReset = LocalDateTime.now();

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;

    @Column(name = "verification_level")
    private Integer verificationLevel = 0;

    @Column(name = "wallet_address")
    private String walletAddress;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum WalletStatus {
        ACTIVE,
        FROZEN,
        SUSPENDED,
        CLOSED,
        PENDING_VERIFICATION
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (availableBalance == null) {
            availableBalance = balance;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        resetDailyMonthlyLimitsIfNeeded();
    }

    public void addBalance(BigDecimal amount) {
        this.balance = this.balance.add(amount);
        this.availableBalance = this.availableBalance.add(amount);
        this.totalDeposited = this.totalDeposited.add(amount);
    }

    public void subtractBalance(BigDecimal amount) {
        if (availableBalance.compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient available balance");
        }
        this.balance = this.balance.subtract(amount);
        this.availableBalance = this.availableBalance.subtract(amount);
        this.dailySpent = this.dailySpent.add(amount);
        this.monthlySpent = this.monthlySpent.add(amount);
    }

    public void freezeAmount(BigDecimal amount) {
        if (availableBalance.compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance to freeze");
        }
        this.availableBalance = this.availableBalance.subtract(amount);
        this.frozenBalance = this.frozenBalance.add(amount);
    }

    public void unfreezeAmount(BigDecimal amount) {
        if (frozenBalance.compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient frozen balance to unfreeze");
        }
        this.frozenBalance = this.frozenBalance.subtract(amount);
        this.availableBalance = this.availableBalance.add(amount);
    }

    public void addEarnings(BigDecimal amount) {
        this.balance = this.balance.add(amount);
        this.availableBalance = this.availableBalance.add(amount);
        this.totalEarned = this.totalEarned.add(amount);
    }

    public void recordWithdrawal(BigDecimal amount) {
        this.totalWithdrawn = this.totalWithdrawn.add(amount);
    }

    public void recordTransferIn(BigDecimal amount) {
        this.totalTransferredIn = this.totalTransferredIn.add(amount);
    }

    public void recordTransferOut(BigDecimal amount) {
        this.totalTransferredOut = this.totalTransferredOut.add(amount);
    }

    public boolean hasSufficientBalance(BigDecimal amount) {
        return availableBalance.compareTo(amount) >= 0;
    }

    public boolean isWithinDailyLimit(BigDecimal amount) {
        if (dailyLimit == null) return true;
        return dailySpent.add(amount).compareTo(dailyLimit) <= 0;
    }

    public boolean isWithinMonthlyLimit(BigDecimal amount) {
        if (monthlyLimit == null) return true;
        return monthlySpent.add(amount).compareTo(monthlyLimit) <= 0;
    }

    public boolean canTransfer(BigDecimal amount) {
        return status == WalletStatus.ACTIVE && 
               hasSufficientBalance(amount) && 
               isWithinDailyLimit(amount) && 
               isWithinMonthlyLimit(amount);
    }

    public void freeze() {
        this.status = WalletStatus.FROZEN;
    }

    public void unfreeze() {
        this.status = WalletStatus.ACTIVE;
    }

    public void suspend() {
        this.status = WalletStatus.SUSPENDED;
    }

    public void close() {
        this.status = WalletStatus.CLOSED;
    }

    public void verify(int level) {
        this.isVerified = true;
        this.verificationLevel = level;
        if (this.status == WalletStatus.PENDING_VERIFICATION) {
            this.status = WalletStatus.ACTIVE;
        }
    }

    private void resetDailyMonthlyLimitsIfNeeded() {
        LocalDateTime now = LocalDateTime.now();
        
        // Reset daily limit if it's a new day
        if (now.toLocalDate().isAfter(lastDailyReset.toLocalDate())) {
            dailySpent = BigDecimal.ZERO;
            lastDailyReset = now;
        }
        
        // Reset monthly limit if it's a new month
        if (now.getYear() > lastMonthlyReset.getYear() || 
            (now.getYear() == lastMonthlyReset.getYear() && now.getMonthValue() > lastMonthlyReset.getMonthValue())) {
            monthlySpent = BigDecimal.ZERO;
            lastMonthlyReset = now;
        }
    }

    public BigDecimal getTotalTransactions() {
        return totalDeposited.add(totalTransferredIn).add(totalEarned);
    }

    public BigDecimal getTotalOutflow() {
        return totalWithdrawn.add(totalTransferredOut);
    }

    public BigDecimal getNetBalance() {
        return getTotalTransactions().subtract(getTotalOutflow());
    }
}