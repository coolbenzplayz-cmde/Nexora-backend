package org.example.nexora.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * Search Service - Handles search functionality across the platform
 * 
 * Features:
 * - Full-text search
 * - User search
 * - Content search (videos, media, posts)
 * - Product search (marketplace)
 * - Location-based search
 * - Real-time search suggestions
 * - Search analytics
 * - Search ranking and relevance
 */
@SpringBootApplication
@EnableFeignClients
@EnableElasticsearchRepositories
public class SearchServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SearchServiceApplication.class, args);
    }
}
