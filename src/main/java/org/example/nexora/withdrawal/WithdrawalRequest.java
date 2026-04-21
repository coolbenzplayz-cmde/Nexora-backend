package org.example.nexora.withdrawal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Withdrawal request entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawalRequest {
    
    private Long id;
    private Long userId;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private String destinationDetails;
    private String withdrawalType;
    private WithdrawalStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime estimatedProcessingTime;
    private BigDecimal fee;
    private BigDecimal netAmount;
    
    public enum WithdrawalStatus {
        PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED
    }
}
