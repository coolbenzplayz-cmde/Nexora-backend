package org.example.nexora.microservices;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Wallet Service - Handles financial transactions and payments
 * 
 * Features:
 * - Wallet management
 * - Payment processing
 * - Transaction history
 * - Balance management
 * - Currency conversion
 * - Fraud detection
 * - Withdrawal and deposits
 * - Payment methods management
 */
@SpringBootApplication
@EnableFeignClients
@EnableJpaRepositories
public class WalletServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(WalletServiceApplication.class, args);
    }
}
