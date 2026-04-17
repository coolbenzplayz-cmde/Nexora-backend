package org.example.nexora.wallet;

import jakarta.persistence.*;
import org.example.nexora.user.User;

import java.math.BigDecimal;

@Entity
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal balance;

    @OneToOne
    private User user;

    public Wallet() {}

    public Wallet(User user) {
        this.user = user;
        this.balance = BigDecimal.ZERO;
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}