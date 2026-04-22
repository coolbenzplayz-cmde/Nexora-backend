package org.example.nexora.withdrawal;

import java.util.List;

/**
 * Simplified Validation result without Lombok
 */
public class ValidationResultSimple {
    
    private boolean valid;
    private List<String> errors;
    
    public ValidationResultSimple(boolean valid, List<String> errors) {
        this.valid = valid;
        this.errors = errors;
    }
    
    // Getters and methods
    public boolean isValid() {
        return valid;
    }
    
    public List<String> getErrors() {
        return errors;
    }
    
    // Static factory methods
    public static ValidationResultSimple success() {
        return new ValidationResultSimple(true, List.of());
    }
    
    public static ValidationResultSimple failure(List<String> errors) {
        return new ValidationResultSimple(false, errors);
    }
}
