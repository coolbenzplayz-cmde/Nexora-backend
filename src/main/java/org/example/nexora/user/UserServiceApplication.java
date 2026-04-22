package org.example.nexora.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * User Service - Manages user profiles and data
 * 
 * Features:
 * - User profile management
 * - User preferences and settings
 * - User search and discovery
 * - User relationships (friends, followers)
 * - User activity tracking
 * - User analytics
 * - User privacy controls
 * - User content management
 */
@SpringBootApplication
@EnableFeignClients
@EnableJpaRepositories
@EnableMongoRepositories
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
