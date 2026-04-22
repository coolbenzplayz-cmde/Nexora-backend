package org.example.nexora.microservices;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Media Service - Handles media content and streaming
 * 
 * Features:
 * - Video upload and processing
 * - Image processing
 * - Media streaming
 * - Content transcoding
 * - CDN integration
 * - Media analytics
 * - Content moderation
 * - Media storage management
 */
@SpringBootApplication
@EnableFeignClients
@EnableJpaRepositories
public class MediaServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MediaServiceApplication.class, args);
    }
}
