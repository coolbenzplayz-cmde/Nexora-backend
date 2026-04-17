package org.example.nexora.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI (Swagger) configuration for API documentation.
 * Generates interactive API docs with JWT authentication support.
 */
@Slf4j
@Configuration
public class OpenApiConfig {

    @Value("${springdoc.api-docs.path:/api-docs}")
    private String apiDocsPath;

    @Value("${springdoc.swagger-ui.path:/swagger-ui.html}")
    private String swaggerUiPath;

    @Value("${app.name:Nexora}")
    private String appName;

    @Value("${app.description:Nexora super-app API — social, commerce, mobility, wallet, games, and media}")
    private String appDescription;

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @Value("${appTermsOfServiceUrl:https://nexora.app/terms}")
    private String termsOfServiceUrl;

    @Value("${appContactName:API Support}")
    private String contactName;

    @Value("${appContactEmail:support@nexora.app}")
    private String contactEmail;

    @Value("${appContactUrl:https://nexora.app/support}")
    private String contactUrl;

    @Value("${serverUrl:http://localhost:8080}")
    private String serverUrl;

    @Value("${serverDescription:Development Server}")
    private String serverDescription;

    /**
     * Configures the OpenAPI specification.
     */
    @Bean
    public OpenAPI openAPI() {
        log.info("Configuring OpenAPI for {}", appName);

        return new OpenAPI()
                .info(new Info()
                        .title(appName)
                        .description(appDescription)
                        .version(appVersion)
                        .termsOfService(termsOfServiceUrl)
                        .contact(new Contact()
                                .name(contactName)
                                .email(contactEmail)
                                .url(contactUrl))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://nexora.app/license")))
                .servers(List.of(
                        new Server()
                                .url(serverUrl)
                                .description(serverDescription)))
                .addSecurityItem(new SecurityRequirement()
                        .addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT token to access protected endpoints")));
    }
}