package org.example.nexora.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

/**
 * Auth Service - Handles authentication and authorization
 * 
 * Features:
 * - User registration and login
 * - JWT token generation and validation
 * - Password management
 * - Multi-factor authentication
 * - OAuth2 integration
 * - Role-based access control
 * - Session management
 * - Security audit logging
 */
@SpringBootApplication
@EnableWebSecurity
@EnableFeignClients
@EnableJpaRepositories
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
