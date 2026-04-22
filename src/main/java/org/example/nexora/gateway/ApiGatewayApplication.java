package org.example.nexora.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * API Gateway Application - Entry point for all microservices
 * 
 * Features:
 * - Request routing to microservices
 * - Authentication and authorization
 * - Rate limiting
 * - Load balancing
 * - Request/response transformation
 * - Circuit breaker patterns
 * - Request logging and monitoring
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Auth Service Routes
                .route("auth-service", r -> r.path("/api/v1/auth/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config.setName("auth-circuit-breaker"))
                                .retry(retryConfig -> retryConfig.setRetries(3))
                                .requestRateLimiter(config -> config.setRateLimiter(redisRateLimiter()))
                        )
                        .uri("lb://auth-service"))

                // User Service Routes
                .route("user-service", r -> r.path("/api/v1/users/**")
                        .filters(f -> f
                                .filter(authenticationFilter())
                                .circuitBreaker(config -> config.setName("user-circuit-breaker"))
                                .requestRateLimiter(config -> config.setRateLimiter(redisRateLimiter()))
                        )
                        .uri("lb://user-service"))

                // Search Service Routes
                .route("search-service", r -> r.path("/api/v1/search/**")
                        .filters(f -> f
                                .filter(authenticationFilter())
                                .circuitBreaker(config -> config.setName("search-circuit-breaker"))
                                .requestRateLimiter(config -> config.setRateLimiter(redisRateLimiter()))
                        )
                        .uri("lb://search-service"))

                // Ride Service Routes
                .route("ride-service", r -> r.path("/api/v1/rides/**")
                        .filters(f -> f
                                .filter(authenticationFilter())
                                .circuitBreaker(config -> config.setName("ride-circuit-breaker"))
                                .requestRateLimiter(config -> config.setRateLimiter(redisRateLimiter()))
                        )
                        .uri("lb://ride-service"))

                // Food Service Routes
                .route("food-service", r -> r.path("/api/v1/food/**")
                        .filters(f -> f
                                .filter(authenticationFilter())
                                .circuitBreaker(config -> config.setName("food-circuit-breaker"))
                                .requestRateLimiter(config -> config.setRateLimiter(redisRateLimiter()))
                        )
                        .uri("lb://food-service"))

                // Wallet Service Routes
                .route("wallet-service", r -> r.path("/api/v1/wallet/**")
                        .filters(f -> f
                                .filter(authenticationFilter())
                                .circuitBreaker(config -> config.setName("wallet-circuit-breaker"))
                                .requestRateLimiter(config -> config.setRateLimiter(redisRateLimiter()))
                        )
                        .uri("lb://wallet-service"))

                // Chat Service Routes
                .route("chat-service", r -> r.path("/api/v1/chat/**")
                        .filters(f -> f
                                .filter(authenticationFilter())
                                .circuitBreaker(config -> config.setName("chat-circuit-breaker"))
                                .requestRateLimiter(config -> config.setRateLimiter(redisRateLimiter()))
                        )
                        .uri("lb://chat-service"))

                // Games Service Routes
                .route("games-service", r -> r.path("/api/v1/games/**")
                        .filters(f -> f
                                .filter(authenticationFilter())
                                .circuitBreaker(config -> config.setName("games-circuit-breaker"))
                                .requestRateLimiter(config -> config.setRateLimiter(redisRateLimiter()))
                        )
                        .uri("lb://games-service"))

                // Media Service Routes
                .route("media-service", r -> r.path("/api/v1/media/**")
                        .filters(f -> f
                                .filter(authenticationFilter())
                                .circuitBreaker(config -> config.setName("media-circuit-breaker"))
                                .requestRateLimiter(config -> config.setRateLimiter(redisRateLimiter()))
                        )
                        .uri("lb://media-service"))

                // Delivery Service Routes
                .route("delivery-service", r -> r.path("/api/v1/delivery/**")
                        .filters(f -> f
                                .filter(authenticationFilter())
                                .circuitBreaker(config -> config.setName("delivery-circuit-breaker"))
                                .requestRateLimiter(config -> config.setRateLimiter(redisRateLimiter()))
                        )
                        .uri("lb://delivery-service"))

                // AI Assistant Service Routes
                .route("ai-service", r -> r.path("/api/v1/ai/**")
                        .filters(f -> f
                                .filter(authenticationFilter())
                                .circuitBreaker(config -> config.setName("ai-circuit-breaker"))
                                .requestRateLimiter(config -> config.setRateLimiter(redisRateLimiter()))
                        )
                        .uri("lb://ai-service"))

                // Shopping Service Routes
                .route("shopping-service", r -> r.path("/api/v1/shopping/**")
                        .filters(f -> f
                                .filter(authenticationFilter())
                                .circuitBreaker(config -> config.setName("shopping-circuit-breaker"))
                                .requestRateLimiter(config -> config.setRateLimiter(redisRateLimiter()))
                        )
                        .uri("lb://shopping-service"))

                // WebSocket Routes
                .route("websocket-service", r -> r.path("/ws/**")
                        .uri("lb://websocket-service"))

                .build();
    }

    @Bean
    public AuthenticationFilter authenticationFilter() {
        return new AuthenticationFilter();
    }

    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(100, 200, 1); // 100 requests per second, burst of 200
    }

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
