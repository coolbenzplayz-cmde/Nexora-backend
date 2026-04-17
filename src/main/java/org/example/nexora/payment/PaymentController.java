package org.example.nexora.payment;

import org.example.nexora.common.ApiResponse;
import org.example.nexora.common.PaginationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/pay")
    public ResponseEntity<ApiResponse<Transaction>> processPayment(
            @RequestParam Long userId,
            @RequestParam BigDecimal amount,
            @RequestParam String type,
            @RequestParam(required = false) String description) {
        Transaction transaction = paymentService.processPayment(userId, amount, type, description);
        return ResponseEntity.ok(ApiResponse.success(transaction));
    }

    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse<Transaction>> processDeposit(
            @RequestParam Long userId,
            @RequestParam BigDecimal amount,
            @RequestParam String method,
            @RequestParam(required = false) String reference) {
        Transaction transaction = paymentService.processDeposit(userId, amount, method, reference);
        return ResponseEntity.ok(ApiResponse.success(transaction));
    }

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<Transaction>> processTransfer(
            @RequestParam Long fromUserId,
            @RequestParam Long toUserId,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String description) {
        Transaction transaction = paymentService.processTransfer(fromUserId, toUserId, amount, description);
        return ResponseEntity.ok(ApiResponse.success(transaction));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<PaginationResponse<Transaction>>> getUserTransactions(
            @PathVariable Long userId,
            Pageable pageable) {
        Page<Transaction> transactions = paymentService.getUserTransactions(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(new PaginationResponse<>(transactions)));
    }

    @GetMapping("/users/{userId}/recent")
    public ResponseEntity<ApiResponse<List<Transaction>>> getRecentTransactions(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getRecentTransactions(userId, limit)));
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<ApiResponse<Transaction>> getTransaction(@PathVariable Long transactionId) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getTransactionById(transactionId)));
    }

    @GetMapping("/users/{userId}/balance")
    public ResponseEntity<ApiResponse<BigDecimal>> getUserBalance(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getUserBalance(userId)));
    }
}
