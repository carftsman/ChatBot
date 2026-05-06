package com.dhatvibs.modules.config.auth;


import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Chatbot Help Center API")
                .description("""
                    APIs for Chatbot Help Center.
                    Supports User, Vendor, and Rider login.
                    After login, JWT token contains appId
                    which controls chatbot routing.
                    """)
                .version("v1.0")
                .contact(new Contact()
                    .name("Chatbot Team")
                    .email("support@chatbot.com")))
            // Adds "Authorize" button in Swagger UI
            .addSecurityItem(
                new SecurityRequirement()
                    .addList("Bearer Authentication"))
            .components(new Components()
                .addSecuritySchemes(
                    "Bearer Authentication",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description(
                            "Paste your JWT token here")));
    }
}