package org.example.nexora.microservices;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Shopping Service - Handles e-commerce and marketplace
 * 
 * Features:
 * - Product catalog
 * - Shopping cart
 * - Order management
 * - Payment processing
 * - Product search
 * - Vendor management
 * - Customer reviews
 * - Inventory management
 */
@SpringBootApplication
@EnableFeignClients
@EnableJpaRepositories
public class ShoppingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShoppingServiceApplication.class, args);
    }
}
