package org.example.nexora.payment;

import org.example.nexora.common.BusinessException;
import org.example.nexora.common.PaginationResponse;
import org.example.nexora.user.User;
import org.example.nexora.user.UserRepository;
import org.example.nexora.wallet.Wallet;
import org.example.nexora.wallet.WalletRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;

    public PaymentService(PaymentRepository paymentRepository,
                          WalletRepository walletRepository,
                          UserRepository userRepository) {
        this.paymentRepository = paymentRepository;
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
    }

    public Transaction processPayment(Long userId, BigDecimal amount, String type, String description) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));

        Wallet wallet = walletRepository.findByUser_Id(userId)
                .orElseGet(() -> createWallet(user));

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new BusinessException("Insufficient balance");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);

        Transaction transaction = new Transaction();
        transaction.setWalletId(wallet.getId());
        transaction.setUserId(userId);
        transaction.setAmount(amount);
        transaction.setType(parseType(type));
        transaction.setDescription(description);
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);

        return paymentRepository.save(transaction);
    }

    public Transaction processDeposit(Long userId, BigDecimal amount, String method, String reference) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));

        Wallet wallet = walletRepository.findByUser_Id(userId)
                .orElseGet(() -> createWallet(user));

        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);

        Transaction transaction = new Transaction();
        transaction.setWalletId(wallet.getId());
        transaction.setUserId(userId);
        transaction.setAmount(amount);
        transaction.setType(Transaction.TransactionType.DEPOSIT);
        transaction.setDescription("Deposit via " + method);
        transaction.setReferenceCode(reference);
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);

        return paymentRepository.save(transaction);
    }

    @Transactional
    public Transaction processTransfer(Long fromUserId, Long toUserId, BigDecimal amount, String description) {
        if (fromUserId.equals(toUserId)) {
            throw new BusinessException("Cannot transfer to yourself");
        }

        Wallet fromWallet = walletRepository.findByUser_Id(fromUserId)
                .orElseThrow(() -> new BusinessException("Sender wallet not found"));

        if (fromWallet.getBalance().compareTo(amount) < 0) {
            throw new BusinessException("Insufficient balance");
        }

        Wallet toWallet = walletRepository.findByUser_Id(toUserId)
                .orElseThrow(() -> new BusinessException("Recipient wallet not found"));

        fromWallet.setBalance(fromWallet.getBalance().subtract(amount));
        toWallet.setBalance(toWallet.getBalance().add(amount));

        walletRepository.save(fromWallet);
        walletRepository.save(toWallet);

        String ref = "TX-" + System.currentTimeMillis();

        Transaction debitTransaction = new Transaction();
        debitTransaction.setWalletId(fromWallet.getId());
        debitTransaction.setUserId(fromUserId);
        debitTransaction.setAmount(amount);
        debitTransaction.setType(Transaction.TransactionType.TRANSFER);
        debitTransaction.setDescription(description);
        debitTransaction.setReferenceCode(ref);
        debitTransaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        paymentRepository.save(debitTransaction);

        Transaction creditTransaction = new Transaction();
        creditTransaction.setWalletId(toWallet.getId());
        creditTransaction.setUserId(toUserId);
        creditTransaction.setAmount(amount);
        creditTransaction.setType(Transaction.TransactionType.TRANSFER_RECEIVED);
        creditTransaction.setDescription(description);
        creditTransaction.setReferenceCode(ref);
        creditTransaction.setStatus(Transaction.TransactionStatus.COMPLETED);

        return paymentRepository.save(creditTransaction);
    }

    public Page<Transaction> getUserTransactions(Long userId, Pageable pageable) {
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public List<Transaction> getRecentTransactions(Long userId, int limit) {
        Wallet wallet = walletRepository.findByUser_Id(userId)
                .orElseThrow(() -> new BusinessException("Wallet not found"));
        return paymentRepository.findTop10ByWalletIdOrderByCreatedAtDesc(wallet.getId());
    }

    public Transaction getTransactionById(Long transactionId) {
        return paymentRepository.findById(transactionId)
                .orElseThrow(() -> new BusinessException("Transaction not found"));
    }

    private Wallet createWallet(User user) {
        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setBalance(BigDecimal.ZERO);
        return walletRepository.save(wallet);
    }

    public BigDecimal getUserBalance(Long userId) {
        Wallet wallet = walletRepository.findByUser_Id(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new BusinessException("User not found"));
                    return createWallet(user);
                });
        return wallet.getBalance();
    }

    private static Transaction.TransactionType parseType(String type) {
        if (type == null || type.isBlank()) {
            return Transaction.TransactionType.PAYMENT;
        }
        try {
            return Transaction.TransactionType.valueOf(type.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return Transaction.TransactionType.PAYMENT;
        }
    }
}
