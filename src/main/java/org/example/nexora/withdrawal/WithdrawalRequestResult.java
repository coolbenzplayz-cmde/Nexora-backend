package org.example.nexora.withdrawal;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Result class for withdrawal request operations
 */
public class WithdrawalRequestResult {
    
    private boolean success;
    private String message;
    private WithdrawalRequest withdrawalRequest;
    private LocalDateTime estimatedProcessingTime;
    private List<String> errors;
    private String transactionId;
    
    // Manual getters/setters to fix Lombok issues
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public WithdrawalRequest getWithdrawalRequest() { return withdrawalRequest; }
    public void setWithdrawalRequest(WithdrawalRequest withdrawalRequest) { this.withdrawalRequest = withdrawalRequest; }
    
    public LocalDateTime getEstimatedProcessingTime() { return estimatedProcessingTime; }
    public void setEstimatedProcessingTime(LocalDateTime estimatedProcessingTime) { this.estimatedProcessingTime = estimatedProcessingTime; }
    
    public List<String> getErrors() { return errors; }
    public void setErrors(List<String> errors) { this.errors = errors; }
    
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    
    public static WithdrawalRequestResult success(WithdrawalRequest request) {
        WithdrawalRequestResult result = new WithdrawalRequestResult();
        result.setSuccess(true);
        result.setWithdrawalRequest(request);
        result.setMessage("Withdrawal request created successfully");
        result.setTransactionId("TXN_" + System.currentTimeMillis());
        return result;
    }
    
    public static WithdrawalRequestResult failure(String message) {
        WithdrawalRequestResult result = new WithdrawalRequestResult();
        result.setSuccess(false);
        result.setMessage(message);
        return result;
    }
    
    public static WithdrawalRequestResult failure(List<String> errors) {
        WithdrawalRequestResult result = new WithdrawalRequestResult();
        result.setSuccess(false);
        result.setErrors(errors);
        result.setMessage("Validation failed");
        return result;
    }
}
