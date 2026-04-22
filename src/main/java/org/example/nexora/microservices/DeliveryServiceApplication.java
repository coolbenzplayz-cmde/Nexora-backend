package org.example.nexora.microservices;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Delivery Service - Handles package and item delivery
 * 
 * Features:
 * - Package tracking
 * - Delivery scheduling
 * - Courier management
 * - Route optimization
 * - Delivery notifications
 * - Proof of delivery
 * - Delivery analytics
 * - Returns management
 */
@SpringBootApplication
@EnableFeignClients
@EnableJpaRepositories
public class DeliveryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeliveryServiceApplication.class, args);
    }
}
