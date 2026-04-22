package org.example.nexora.microservices;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * AI Service - Handles AI-powered features and assistance
 * 
 * Features:
 * - AI chat assistant
 * - Content recommendations
 * - Natural language processing
 * - Image recognition
 * - Voice recognition
 * - Machine learning models
 * - Predictive analytics
 * - Automated moderation
 */
@SpringBootApplication
@EnableFeignClients
@EnableJpaRepositories
public class AIServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AIServiceApplication.class, args);
    }
}
