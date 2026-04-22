package org.example.nexora.microservices;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Ride Service - Handles ride hailing and transportation
 * 
 * Features:
 * - Ride booking and matching
 * - Driver management
 * - Real-time location tracking
 * - Route optimization
 * - Fare calculation
 * - Payment processing
 * - Ride history
 * - Driver ratings and reviews
 */
@SpringBootApplication
@EnableFeignClients
@EnableJpaRepositories
public class RideServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RideServiceApplication.class, args);
    }
}
