package org.example.nexora.wallet;

import org.example.nexora.user.User;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    // CREATE WALLET
    @PostMapping("/create")
    public Wallet createWallet(@RequestBody User user) {
        return walletService.createWallet(user);
    }

    // ADD MONEY
    @PostMapping("/add")
    public Wallet addMoney(@RequestParam Long userId,
                           @RequestParam BigDecimal amount) {
        return walletService.addMoney(userId, amount);
    }

    // TRANSFER MONEY
    @PostMapping("/transfer")
    public String transfer(@RequestBody TransferRequest request) {
        return walletService.transfer(request);
    }
}