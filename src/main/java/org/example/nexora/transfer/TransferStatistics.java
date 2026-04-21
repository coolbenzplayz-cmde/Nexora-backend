package org.example.nexora.transfer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
public class TransferStatistics {

    private BigDecimal totalSent;
    private BigDecimal totalReceived;
    private BigDecimal netTransferred;
    private Long completedTransfersSent;
    private Long completedTransfersReceived;
    private Long pendingTransfers;

    public TransferStatistics() {
        // Default constructor
    }

    public Long getTotalCompletedTransfers() {
        return completedTransfersSent + completedTransfersReceived;
    }

    public BigDecimal getAverageSentAmount() {
        if (completedTransfersSent == 0) return BigDecimal.ZERO;
        return totalSent.divide(BigDecimal.valueOf(completedTransfersSent), 2, BigDecimal.ROUND_HALF_UP);
    }

    public BigDecimal getAverageReceivedAmount() {
        if (completedTransfersReceived == 0) return BigDecimal.ZERO;
        return totalReceived.divide(BigDecimal.valueOf(completedTransfersReceived), 2, BigDecimal.ROUND_HALF_UP);
    }

    public boolean hasNetPositive() {
        return netTransferred.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean hasNetNegative() {
        return netTransferred.compareTo(BigDecimal.ZERO) < 0;
    }

    public boolean isNetNeutral() {
        return netTransferred.compareTo(BigDecimal.ZERO) == 0;
    }

    public double getSentToReceivedRatio() {
        if (totalReceived.compareTo(BigDecimal.ZERO) == 0) return 0.0;
        return totalSent.divide(totalReceived, 4, BigDecimal.ROUND_HALF_UP).doubleValue();
    }
}
