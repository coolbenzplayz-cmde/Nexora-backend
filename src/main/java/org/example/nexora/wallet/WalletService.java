package org.example.nexora.wallet;

import org.example.nexora.transaction.Transaction;
import org.example.nexora.transaction.TransactionService;
import org.example.nexora.user.User;
import org.example.nexora.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final TransactionService transactionService;

    public WalletService(WalletRepository walletRepository,
                         UserRepository userRepository,
                         TransactionService transactionService) {
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
        this.transactionService = transactionService;
    }

    @Transactional
    public Wallet createWallet(User user) {
        if (user.getId() == null) {
            user = userRepository.save(user);
        }
        Long userId = user.getId();
        return walletRepository.findByUser_Id(userId)
                .orElseGet(() -> {
                    Wallet wallet = new Wallet();
                    wallet.setUser(user);
                    wallet.setBalance(BigDecimal.ZERO);
                    return walletRepository.save(wallet);
                });
    }

    @Transactional
    public Wallet addMoney(Long userId, BigDecimal amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Wallet wallet = walletRepository.findByUser_Id(userId)
                .orElseGet(() -> {
                    Wallet w = new Wallet();
                    w.setUser(user);
                    w.setBalance(BigDecimal.ZERO);
                    return walletRepository.save(w);
                });
        wallet.setBalance(wallet.getBalance().add(amount));
        Wallet saved = walletRepository.save(wallet);
        transactionService.saveTransaction(new Transaction(
                "ADD-" + System.currentTimeMillis(),
                null,
                userId,
                amount,
                "COMPLETED"));
        return saved;
    }

    @Transactional
    public String transfer(TransferRequest request) {
        Long senderId = request.getSenderId();
        Long receiverId = request.getReceiverId();
        BigDecimal amount = request.getAmount();

        Wallet from = walletRepository.findByUser_Id(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Sender wallet not found"));
        Wallet to = walletRepository.findByUser_Id(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("Receiver wallet not found"));

        if (from.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));
        walletRepository.save(from);
        walletRepository.save(to);

        String ref = "TRF-" + System.currentTimeMillis();
        transactionService.saveTransaction(new Transaction(ref, senderId, receiverId, amount, "COMPLETED"));

        return "Transfer successful: " + ref;
    }
}
