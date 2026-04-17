package org.example.nexora.transaction;

import org.example.nexora.security.JwtService;
import org.example.nexora.user.User;
import org.example.nexora.user.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public TransactionController(TransactionService transactionService,
                                 JwtService jwtService,
                                 UserRepository userRepository) {
        this.transactionService = transactionService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @GetMapping("/history")
    public List<TransactionResponse> getMyHistory(@RequestHeader("Authorization") String token) {

        String email = jwtService.extractEmail(token.replace("Bearer ", ""));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return transactionService.getUserHistory(user.getId());
    }
}