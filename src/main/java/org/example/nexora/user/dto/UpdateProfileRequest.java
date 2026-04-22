package org.example.nexora.user.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Update profile request
 */
@Data
public class UpdateProfileRequest {
    
    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
    private String lastName;
    
    private String bio;
    private String avatarUrl;
    private String phoneNumber;
    private String dateOfBirth;
    private String gender;
    private String location;
    private String website;
    private String[] interests;
    private String[] socialLinks;
    
    public UpdateProfileRequest() {
        this.interests = new String[0];
        this.socialLinks = new String[0];
    }
}
