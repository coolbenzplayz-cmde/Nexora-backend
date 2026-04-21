package org.example.nexora.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for API documentation.
 * Configures API metadata, security schemes, and server information.
 */
@Configuration
public class OpenApiConfig {

    private static final Logger log = LoggerFactory.getLogger(OpenApiConfig.class);

    @Value("${spring.application.name:Nexora API}")
    private String appName;

    @Value("${spring.application.version:1.0.0}")
    private String appVersion;

    @Value("${spring.application.description:Comprehensive platform for social networking, marketplace, and more}")
    private String appDescription;

    @Value("${app.server.url:http://localhost:8080}")
    private String serverUrl;

    @Value("${app.server.description:Development server}")
    private String serverDescription;

    @Value("${app.contact.email:support@nexora.app}")
    private String contactEmail;

    @Value("${app.contact.name:Nexora Support}")
    private String contactName;

    @Value("${app.license.name:MIT}")
    private String licenseName;

    @Value("${app.license.url:https://opensource.org/licenses/MIT}")
    private String licenseUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        log.info("Configuring OpenAPI for {}", appName);

        return new OpenAPI()
                .info(new Info()
                        .title(appName)
                        .version(appVersion)
                        .description(appDescription)
                        .contact(new Contact()
                                .name(contactName)
                                .email(contactEmail))
                        .license(new License()
                                .name(licenseName)
                                .url(licenseUrl)))
                .servers(List.of(
                        new Server()
                                .url(serverUrl)
                                .description(serverDescription)
                ))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .bearerFormat("JWT")
                                        .scheme("bearer")
                                        .description("Please provide a valid JWT token")));
    }
}
