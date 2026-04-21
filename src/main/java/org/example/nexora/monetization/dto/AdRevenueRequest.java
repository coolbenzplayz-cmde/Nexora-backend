package org.example.nexora.monetization.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.nexora.monetization.AdRevenue;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdRevenueRequest {

    @NotNull(message = "Ad type cannot be null")
    private AdRevenue.AdType adType;

    @NotNull(message = "Ad position cannot be null")
    private AdRevenue.AdPosition adPosition;

    @NotNull(message = "Impressions cannot be null")
    @Min(value = 0, message = "Impressions cannot be negative")
    private Long impressions = 0L;

    @NotNull(message = "Clicks cannot be null")
    @Min(value = 0, message = "Clicks cannot be negative")
    private Long clicks = 0L;
}
