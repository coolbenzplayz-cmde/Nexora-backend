package org.example.nexora.withdrawal;

import lombok.Data;
import java.util.List;

/**
 * Validation result for withdrawal requests
 */
@Data
public class ValidationResult {
    
    private boolean valid;
    private List<String> errors;
    
    public ValidationResult(boolean valid, List<String> errors) {
        this.valid = valid;
        this.errors = errors;
    }
    
    public boolean isValid() {
        return valid;
    }
    
    public List<String> getErrors() {
        return errors;
    }
    
    public static ValidationResult success() {
        return new ValidationResult(true, List.of());
    }
    
    public static ValidationResult failure(List<String> errors) {
        return new ValidationResult(false, errors);
    }
}
