package org.example.nexora.microservices;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Food Service - Handles food ordering and delivery
 * 
 * Features:
 * - Restaurant management
 * - Menu management
 * - Food ordering
 * - Order tracking
 * - Delivery management
 * - Payment processing
 * - Food reviews and ratings
 * - Promotions and discounts
 */
@SpringBootApplication
@EnableFeignClients
@EnableJpaRepositories
public class FoodServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FoodServiceApplication.class, args);
    }
}
