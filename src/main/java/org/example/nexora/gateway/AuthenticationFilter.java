package org.example.nexora.gateway;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Authentication Filter for API Gateway
 * 
 * Features:
 * - JWT token validation
 * - User authentication
 * - Rate limiting per user
 * - Request logging
 * - Security headers injection
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationFilter implements GatewayFilter {

    private final WebClient.Builder webClientBuilder;
    private final RateLimitService rateLimitService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        // Skip authentication for public endpoints
        if (isPublicEndpoint(request.getPath().value())) {
            return chain.filter(exchange);
        }

        // Extract token from request
        String token = extractToken(request);
        if (token == null) {
            return handleUnauthorized(response, "Missing authentication token");
        }

        // Validate token
        return validateToken(token)
                .flatMap(userDetails -> {
                    // Check rate limiting
                    if (rateLimitService.isRateLimited(userDetails.getUserId(), request.getPath().value())) {
                        return handleTooManyRequests(response, "Rate limit exceeded");
                    }

                    // Add user context to request
                    ServerHttpRequest modifiedRequest = addUserContext(request, userDetails);
                    
                    // Log request
                    logRequest(userDetails, request);

                    return chain.filter(exchange.mutate().request(modifiedRequest).build());
                })
                .onErrorResume(throwable -> handleUnauthorized(response, "Invalid token"));
    }

    private boolean isPublicEndpoint(String path) {
        return path.startsWith("/api/v1/auth/login") ||
               path.startsWith("/api/v1/auth/register") ||
               path.startsWith("/api/v1/auth/refresh") ||
               path.startsWith("/actuator/") ||
               path.startsWith("/health") ||
               path.startsWith("/ws/");
    }

    private String extractToken(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private Mono<UserDetails> validateToken(String token) {
        return webClientBuilder.build()
                .post()
                .uri("http://auth-service/api/v1/auth/validate")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(UserDetails.class);
    }

    private Mono<Void> handleUnauthorized(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");
        String body = String.format("{\"error\": \"%s\", \"status\": 401}", message);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    private Mono<Void> handleTooManyRequests(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().add("Content-Type", "application/json");
        String body = String.format("{\"error\": \"%s\", \"status\": 429}", message);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    private ServerHttpRequest addUserContext(ServerHttpRequest request, UserDetails userDetails) {
        return request.mutate()
                .header("X-User-ID", userDetails.getUserId().toString())
                .header("X-User-Email", userDetails.getEmail())
                .header("X-User-Role", userDetails.getRole())
                .header("X-User-Permissions", String.join(",", userDetails.getPermissions()))
                .build();
    }

    private void logRequest(UserDetails userDetails, ServerHttpRequest request) {
        log.info("Request: {} {} by user {} ({})", 
                request.getMethod(), 
                request.getPath(), 
                userDetails.getUserId(), 
                userDetails.getEmail());
    }

    @Data
    public static class UserDetails {
        private Long userId;
        private String email;
        private String role;
        private java.util.List<String> permissions;
        private java.time.LocalDateTime lastLogin;
    }
}

/**
 * Rate Limiting Service
 */
@Component
@RequiredArgsConstructor
class RateLimitService {
    
    private final RedisTemplate<String, String> redisTemplate;
    
    public boolean isRateLimited(Long userId, String endpoint) {
        String key = "rate_limit:" + userId + ":" + endpoint;
        
        // Get current count
        String countStr = redisTemplate.opsForValue().get(key);
        int count = countStr != null ? Integer.parseInt(countStr) : 0;
        
        // Define rate limits per endpoint
        int limit = getRateLimit(endpoint);
        
        if (count >= limit) {
            return true;
        }
        
        // Increment counter with expiration
        redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, java.time.Duration.ofMinutes(1));
        
        return false;
    }
    
    private int getRateLimit(String endpoint) {
        if (endpoint.contains("/chat/")) return 1000; // High limit for chat
        if (endpoint.contains("/media/")) return 500;  // Medium limit for media
        if (endpoint.contains("/wallet/")) return 200; // Lower limit for wallet
        return 100; // Default limit
    }
}

/**
 * Redis Template for Rate Limiting
 */
@Component
class RedisTemplate {
    // Simplified Redis operations
    public void opsForValue() {}
    public void increment(String key) {}
    public void expire(String key, java.time.Duration duration) {}
    public String get(String key) { return null; }
}
