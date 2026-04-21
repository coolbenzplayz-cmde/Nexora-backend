package org.example.nexora.withdrawal;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Result class for withdrawal request operations
 */
@Data
public class WithdrawalRequestResult {
    
    private boolean success;
    private String message;
    private WithdrawalRequest withdrawalRequest;
    private LocalDateTime estimatedProcessingTime;
    private List<String> errors;
    private String transactionId;
    
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
