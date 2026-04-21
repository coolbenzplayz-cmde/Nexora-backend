package org.example.nexora.monetization.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ViewEarningsRequest {

    @NotNull(message = "Additional views cannot be null")
    @Min(value = 1, message = "Additional views must be at least 1")
    private Long additionalViews;
}
