package org.example.nexora.transfer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleTransferRequest {

    @NotNull(message = "Receiver ID cannot be null")
    private Long receiverId;

    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    private BigDecimal amount;

    private String description;

    @NotNull(message = "Scheduled time cannot be null")
    @Future(message = "Scheduled time must be in the future")
    private LocalDateTime scheduledAt;
}
