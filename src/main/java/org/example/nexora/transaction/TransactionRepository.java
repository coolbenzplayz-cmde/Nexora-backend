package org.example.nexora.transaction;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findBySenderId(Long senderId);

    List<Transaction> findByReceiverId(Long receiverId);

    List<Transaction> findBySenderIdOrReceiverId(Long senderId, Long receiverId);
}