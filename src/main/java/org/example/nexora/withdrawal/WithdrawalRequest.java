package org.example.nexora.withdrawal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Withdrawal request entity
 */
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
    
    // Manual getters/setters to fix Lombok issues
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public String getDestinationDetails() { return destinationDetails; }
    public void setDestinationDetails(String destinationDetails) { this.destinationDetails = destinationDetails; }
    
    public String getWithdrawalType() { return withdrawalType; }
    public void setWithdrawalType(String withdrawalType) { this.withdrawalType = withdrawalType; }
    
    public WithdrawalStatus getStatus() { return status; }
    public void setStatus(WithdrawalStatus status) { this.status = status; }
    
    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }
    
    public LocalDateTime getEstimatedProcessingTime() { return estimatedProcessingTime; }
    public void setEstimatedProcessingTime(LocalDateTime estimatedProcessingTime) { this.estimatedProcessingTime = estimatedProcessingTime; }
    
    public BigDecimal getFee() { return fee; }
    public void setFee(BigDecimal fee) { this.fee = fee; }
    
    public BigDecimal getNetAmount() { return netAmount; }
    public void setNetAmount(BigDecimal netAmount) { this.netAmount = netAmount; }
    
    public enum WithdrawalStatus {
        PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED
    }
}
