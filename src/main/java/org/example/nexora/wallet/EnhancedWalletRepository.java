package org.example.nexora.wallet;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface EnhancedWalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByUserId(Long userId);

    Optional<Wallet> findByWalletAddress(String walletAddress);

    @Query("SELECT w FROM Wallet w WHERE w.status = :status")
    List<Wallet> findByStatus(@Param("status") Wallet.WalletStatus status);

    @Query("SELECT COUNT(w) FROM Wallet w WHERE w.status = :status")
    long countByStatus(@Param("status") Wallet.WalletStatus status);

    @Query("SELECT COUNT(w) FROM Wallet w WHERE w.isVerified = true")
    long countByIsVerifiedTrue();

    @Query("SELECT SUM(w.balance) FROM Wallet w")
    BigDecimal sumTotalBalance();

    @Query("SELECT SUM(w.availableBalance) FROM Wallet w WHERE w.status = 'ACTIVE'")
    BigDecimal sumTotalAvailableBalance();

    @Query("SELECT SUM(w.frozenBalance) FROM Wallet w")
    BigDecimal sumTotalFrozenBalance();

    @Query("SELECT SUM(w.totalEarned) FROM Wallet w")
    BigDecimal sumTotalEarned();

    @Query("SELECT SUM(w.totalWithdrawn) FROM Wallet w")
    BigDecimal sumTotalWithdrawn();

    @Query("SELECT SUM(w.totalDeposited) FROM Wallet w")
    BigDecimal sumTotalDeposited();

    @Query("SELECT w FROM Wallet w WHERE w.balance > :minBalance ORDER BY w.balance DESC")
    List<Wallet> findWalletsWithBalanceGreaterThan(@Param("minBalance") BigDecimal minBalance);

    @Query("SELECT w FROM Wallet w WHERE w.frozenBalance > :minFrozenBalance ORDER BY w.frozenBalance DESC")
    List<Wallet> findWalletsWithFrozenBalanceGreaterThan(@Param("minFrozenBalance") BigDecimal minFrozenBalance);

    @Query("SELECT w FROM Wallet w WHERE w.isVerified = true ORDER BY w.totalEarned DESC")
    List<Wallet> findTopEarnersByVerification();

    @Query("SELECT w FROM Wallet w ORDER BY w.totalEarned DESC")
    List<Wallet> findTopEarners();

    @Query("SELECT w FROM Wallet w WHERE w.dailyLimit IS NOT NULL OR w.monthlyLimit IS NOT NULL")
    List<Wallet> findWalletsWithLimits();

    @Query("SELECT w FROM Wallet w WHERE w.status = 'FROZEN' OR w.status = 'SUSPENDED'")
    List<Wallet> findRestrictedWallets();

    @Query("SELECT w FROM Wallet w WHERE w.verificationLevel >= :minLevel ORDER BY w.verificationLevel DESC")
    List<Wallet> findWalletsByVerificationLevel(@Param("minLevel") Integer minLevel);

    @Query("SELECT COUNT(w) FROM Wallet w WHERE w.createdAt >= :since")
    long countWalletsCreatedSince(@Param("since") java.time.LocalDateTime since);

    @Query("SELECT w FROM Wallet w WHERE w.user.id IN :userIds")
    List<Wallet> findByUserIds(@Param("userIds") List<Long> userIds);
}
