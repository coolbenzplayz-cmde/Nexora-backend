package org.example.nexora.admin;

import lombok.Data;
import java.util.List;

/**
 * Simple validation result without Lombok
 */
@Data
public class ValidationResultSimple {
    
    private boolean valid;
    private List<String> errors;
    
    public ValidationResultSimple() {
        this.valid = true;
        this.errors = new java.util.ArrayList<>();
    }
    
    public ValidationResultSimple(boolean valid, List<String> errors) {
        this.valid = valid;
        this.errors = errors != null ? errors : new java.util.ArrayList<>();
    }
    
    public boolean isValid() {
        return valid;
    }
    
    public List<String> getErrors() {
        return errors;
    }
    
    public void setValid(boolean valid) {
        this.valid = valid;
    }
    
    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
    
    public void addError(String error) {
        this.errors.add(error);
        this.valid = false;
    }
    
    public static ValidationResultSimple success() {
        return new ValidationResultSimple(true, new java.util.ArrayList<>());
    }
    
    public static ValidationResultSimple failure(List<String> errors) {
        return new ValidationResultSimple(false, errors);
    }
}
