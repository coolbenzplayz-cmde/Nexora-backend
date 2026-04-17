package org.example.nexora.wallet;

import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/withdraw")
public class WithdrawController {

    private final WithdrawService withdrawService;

    public WithdrawController(WithdrawService withdrawService) {
        this.withdrawService = withdrawService;
    }

    // REQUEST
    @PostMapping
    public String withdraw(@RequestHeader("Authorization") String token,
                           @RequestParam BigDecimal amount) {
        return withdrawService.requestWithdraw(token, amount);
    }

    // HISTORY
    @GetMapping
    public List<WithdrawRequest> history(@RequestHeader("Authorization") String token) {
        return withdrawService.getMyWithdraws(token);
    }
}