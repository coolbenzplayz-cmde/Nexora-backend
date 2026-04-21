package org.example.nexora.transport;

import lombok.Data;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

/**
 * Service for handling ride payments
 */
@Service
public class PaymentService {
    
    /**
     * Process payment for a ride
     */
    public PaymentResult processPayment(Long rideId, BigDecimal amount, String paymentMethod) {
        PaymentResult result = new PaymentResult();
        result.setSuccess(true);
        result.setPaymentId("PAY_" + System.currentTimeMillis());
        result.setAmount(amount);
        result.setPaymentMethod(paymentMethod);
        return result;
    }
    
    /**
     * Refund payment
     */
    public PaymentResult refundPayment(String paymentId, BigDecimal amount) {
        PaymentResult result = new PaymentResult();
        result.setSuccess(true);
        result.setPaymentId(paymentId);
        result.setAmount(amount.negate());
        result.setPaymentMethod("REFUND");
        return result;
    }
    
    @Data
    public static class PaymentResult {
        private boolean success;
        private String paymentId;
        private BigDecimal amount;
        private String paymentMethod;
        private String message;
    }
}
