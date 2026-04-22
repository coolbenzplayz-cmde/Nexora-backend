package org.example.nexora.media.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class MonetizationSettings {
    private Boolean enabled = false;
    private MonetizationType type;
    private BigDecimal price;
    private String currency = "USD";
    private List<String> paymentMethods;
    private Map<String, Object> subscriptionSettings;
    private Map<String, Object> payPerViewSettings;
    private Map<String, Object> licensingSettings;
    private String revenueShareModel;
    private BigDecimal platformFee = BigDecimal.ZERO;
    private Boolean allowTips = false;
    private Boolean allowDonations = false;
    private String payoutMethod;
    private String payoutFrequency;
    
    public enum MonetizationType {
        FREE,
        PREMIUM,
        SUBSCRIPTION,
        PAY_PER_VIEW,
        LICENSING,
        AD_SUPPORTED,
        TIP_BASED,
        DONATION_BASED
    }
}
