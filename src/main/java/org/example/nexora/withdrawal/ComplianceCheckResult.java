package org.example.nexora.withdrawal;

import lombok.Data;

/**
 * Compliance check result for withdrawal requests
 */
@Data
public class ComplianceCheckResult {
    
    private boolean approved;
    private String reason;
    private String complianceLevel;
    
    public ComplianceCheckResult(boolean approved, String reason) {
        this.approved = approved;
        this.reason = reason;
        this.complianceLevel = approved ? "PASSED" : "FAILED";
    }
    
    public boolean isApproved() {
        return approved;
    }
    
    public String getReason() {
        return reason;
    }
    
    public static ComplianceCheckResult approved() {
        return new ComplianceCheckResult(true, "Compliance check passed");
    }
    
    public static ComplianceCheckResult rejected(String reason) {
        return new ComplianceCheckResult(false, reason);
    }
}
