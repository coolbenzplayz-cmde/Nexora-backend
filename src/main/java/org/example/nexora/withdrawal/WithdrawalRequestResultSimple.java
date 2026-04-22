package org.example.nexora.withdrawal;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Simplified Withdrawal request result without Lombok
 */
public class WithdrawalRequestResultSimple {
    
    private boolean success;
    private String message;
    private WithdrawalRequestSimple withdrawalRequest;
    private LocalDateTime estimatedProcessingTime;
    private List<String> errors;
    private String transactionId;
    
    // Getters and setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public WithdrawalRequestSimple getWithdrawalRequest() { return withdrawalRequest; }
    public void setWithdrawalRequest(WithdrawalRequestSimple withdrawalRequest) { this.withdrawalRequest = withdrawalRequest; }
    
    public LocalDateTime getEstimatedProcessingTime() { return estimatedProcessingTime; }
    public void setEstimatedProcessingTime(LocalDateTime estimatedProcessingTime) { this.estimatedProcessingTime = estimatedProcessingTime; }
    
    public List<String> getErrors() { return errors; }
    public void setErrors(List<String> errors) { this.errors = errors; }
    
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    
    // Static factory methods
    public static WithdrawalRequestResultSimple success(WithdrawalRequestSimple request) {
        WithdrawalRequestResultSimple result = new WithdrawalRequestResultSimple();
        result.setSuccess(true);
        result.setWithdrawalRequest(request);
        result.setMessage("Withdrawal request created successfully");
        result.setTransactionId("TXN_" + System.currentTimeMillis());
        return result;
    }
    
    public static WithdrawalRequestResultSimple failure(String message) {
        WithdrawalRequestResultSimple result = new WithdrawalRequestResultSimple();
        result.setSuccess(false);
        result.setMessage(message);
        return result;
    }
    
    public static WithdrawalRequestResultSimple failure(List<String> errors) {
        WithdrawalRequestResultSimple result = new WithdrawalRequestResultSimple();
        result.setSuccess(false);
        result.setErrors(errors);
        result.setMessage("Validation failed");
        return result;
    }
}
