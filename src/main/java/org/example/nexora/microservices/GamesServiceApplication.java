package org.example.nexora.microservices;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Games Service - Handles gaming and entertainment
 * 
 * Features:
 * - Mini-games
 * - Tournaments
 * - Leaderboards
 * - Rewards and achievements
 * - Game statistics
 * - Player profiles
 * - Game matchmaking
 * - Virtual currency
 */
@SpringBootApplication
@EnableFeignClients
@EnableJpaRepositories
public class GamesServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GamesServiceApplication.class, args);
    }
}
