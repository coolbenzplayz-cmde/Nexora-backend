package org.example.nexora.transaction;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    // GET FULL USER HISTORY (SENT + RECEIVED)
    public List<TransactionResponse> getUserHistory(Long userId) {

        List<Transaction> transactions =
                transactionRepository.findBySenderIdOrReceiverId(userId, userId);

        return transactions.stream()
                .map(TransactionResponse::new)
                .collect(Collectors.toList());
    }

    // SAVE TRANSACTION (USED BY WALLET SERVICE)
    public Transaction saveTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }
}