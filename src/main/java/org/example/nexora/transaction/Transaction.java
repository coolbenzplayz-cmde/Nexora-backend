package org.example.nexora.transaction;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "LedgerTransaction")
@Table(name = "ledger_transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String reference;

    private Long senderId;

    private Long receiverId;

    private BigDecimal amount;

    private String status;

    private LocalDateTime createdAt = LocalDateTime.now();
}
