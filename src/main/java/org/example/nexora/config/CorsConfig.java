package org.example.nexora.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

/**
 * CORS (Cross-Origin Resource Sharing) configuration.
 * Configures allowed origins, methods, and headers.
 */
@Slf4j
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${cors.allowed-origins:*}")
    private String allowedOrigins;

    @Value("${cors.allowed-methods:GET,POST,PUT,DELETE,PATCH,OPTIONS}")
    private String allowedMethods;

    @Value("${cors.allowed-headers:*}")
    private String allowedHeaders;

    @Value("${cors.exposed-headers:X-Total-Count,X-Page-Number,X-Page-Size}")
    private String exposedHeaders;

    @Value("${cors.allow-credentials:false}")
    private boolean allowCredentials;

    @Value("${cors.max-age:3600}")
    private long maxAge;

    /**
     * Register CORS mappings for the application.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        log.info("Configuring CORS: allowedOrigins={}, allowedMethods={}", allowedOrigins, allowedMethods);

        registry.addMapping("/api/**")
                .allowedOrigins(parseOrigins(allowedOrigins))
                .allowedMethods(parseMethods(allowedMethods))
                .allowedHeaders(parseHeaders(allowedHeaders))
                .exposedHeaders(parseHeaders(exposedHeaders))
                .allowCredentials(allowCredentials)
                .maxAge(maxAge);

        registry.addMapping("/ws/**")
                .allowedOrigins(parseOrigins(allowedOrigins))
                .allowedMethods(parseMethods("GET,POST,OPTIONS"))
                .allowedHeaders(parseHeaders("*"))
                .allowCredentials(true)
                .maxAge(maxAge);
    }

    /**
     * CORS Configuration source bean.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(parseOrigins(allowedOrigins));
        configuration.setAllowedMethods(parseMethods(allowedMethods));
        configuration.setAllowedHeaders(parseHeaders(allowedHeaders));
        configuration.setExposedHeaders(parseHeaders(exposedHeaders));
        configuration.setAllowCredentials(allowCredentials);
        configuration.setMaxAge(maxAge);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        source.registerCorsConfiguration("/ws/**", configuration);

        return source;
    }

    /**
     * CORS Filter bean.
     */
    @Bean
    public CorsFilter corsFilter(CorsConfigurationSource corsConfigurationSource) {
        return new CorsFilter(corsConfigurationSource);
    }

    // Helper methods

    private List<String> parseOrigins(String origins) {
        if (origins == null || origins.isEmpty() || "*".equals(origins)) {
            return Arrays.asList("*");
        }
        return Arrays.asList(origins.split(","));
    }

    private List<String> parseMethods(String methods) {
        if (methods == null || methods.isEmpty()) {
            return Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS");
        }
        return Arrays.asList(methods.split(","));
    }

    private List<String> parseHeaders(String headers) {
        if (headers == null || headers.isEmpty()) {
            return Arrays.asList("*");
        }
        return Arrays.asList(headers.split(","));
    }
}