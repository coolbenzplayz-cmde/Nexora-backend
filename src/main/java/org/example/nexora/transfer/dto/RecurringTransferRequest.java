package org.example.nexora.transfer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecurringTransferRequest {

    @NotNull(message = "Receiver ID cannot be null")
    private Long receiverId;

    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    private BigDecimal amount;

    private String description;

    @NotNull(message = "Interval cannot be null")
    @Pattern(regexp = "DAILY|WEEKLY|MONTHLY", message = "Interval must be DAILY, WEEKLY, or MONTHLY")
    private String interval;

    @Min(value = 1, message = "Max count must be at least 1")
    @Max(value = 365, message = "Max count cannot exceed 365")
    private Integer maxCount;
}
