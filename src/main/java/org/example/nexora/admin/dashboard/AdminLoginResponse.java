package org.example.nexora.admin.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Response class for admin login
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminLoginResponse {
    
    private String token;
    private String adminId;
    private String username;
    private String role;
    private LocalDateTime expiresAt;
    private String message;
    
    public AdminLoginResponse(String token, String adminId, String username) {
        this.token = token;
        this.adminId = adminId;
        this.username = username;
        this.role = "ADMIN";
        this.expiresAt = LocalDateTime.now().plusHours(24);
        this.message = "Login successful";
    }
}
