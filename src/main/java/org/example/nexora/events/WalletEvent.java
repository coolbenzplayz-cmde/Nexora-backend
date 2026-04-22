package org.example.nexora.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

/**
 * Wallet domain event
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WalletEvent extends DomainEvent {
    
    private Long userId;
    private Long walletId;
    private String action; // DEPOSIT, WITHDRAWAL, TRANSFER, BALANCE_UPDATED
    private BigDecimal amount;
    private String currency;
    private String transactionId;
    private String status;
    
    public WalletEvent() {
        super("WALLET_EVENT", "WALLET_SERVICE");
    }
    
    public WalletEvent(Long userId, Long walletId, String action, BigDecimal amount) {
        this();
        this.userId = userId;
        this.walletId = walletId;
        this.action = action;
        this.amount = amount;
        this.currency = "USD";
    }
}
