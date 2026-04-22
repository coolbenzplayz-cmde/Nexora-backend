package org.example.nexora.withdrawal;

/**
 * Simplified Compliance check result without Lombok
 */
public class ComplianceCheckResultSimple {
    
    private boolean approved;
    private String reason;
    private String complianceLevel;
    
    public ComplianceCheckResultSimple(boolean approved, String reason) {
        this.approved = approved;
        this.reason = reason;
        this.complianceLevel = approved ? "PASSED" : "FAILED";
    }
    
    // Getters and methods
    public boolean isApproved() {
        return approved;
    }
    
    public String getReason() {
        return reason;
    }
    
    public String getComplianceLevel() {
        return complianceLevel;
    }
    
    // Static factory methods
    public static ComplianceCheckResultSimple approved() {
        return new ComplianceCheckResultSimple(true, "Compliance check passed");
    }
    
    public static ComplianceCheckResultSimple rejected(String reason) {
        return new ComplianceCheckResultSimple(false, reason);
    }
}
