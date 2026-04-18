package org.example.nexora.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {
    private Long id;
    private String orderId;
    private BigDecimal amount;
    private String paymentMethod;
    private String status;
    private String transactionId;
}
