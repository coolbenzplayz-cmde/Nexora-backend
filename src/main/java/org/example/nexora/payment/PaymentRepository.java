package org.example.nexora.payment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Transaction, Long> {
    Page<Transaction> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Page<Transaction> findByWalletIdOrderByCreatedAtDesc(Long walletId, Pageable pageable);
    List<Transaction> findByWalletIdAndType(Long walletId, Transaction.TransactionType type);
    long countByWalletIdAndStatus(Long walletId, Transaction.TransactionStatus status);
    List<Transaction> findTop10ByWalletIdOrderByCreatedAtDesc(Long walletId);
}
