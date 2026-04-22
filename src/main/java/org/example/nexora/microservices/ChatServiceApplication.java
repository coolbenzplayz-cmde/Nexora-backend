package org.example.nexora.microservices;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Chat Service - Handles messaging and communication
 * 
 * Features:
 * - Real-time messaging
 * - Voice messages
 * - Video calls
 * - Chat locks and privacy
 * - End-to-end encryption
 * - Message reactions
 * - Group chats
 * - Message history
 */
@SpringBootApplication
@EnableFeignClients
@EnableJpaRepositories
@EnableMongoRepositories
public class ChatServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatServiceApplication.class, args);
    }
}
