/*
 * package com.dhatvibs.modules.config.auth;
 * 
 * 
 * import io.swagger.v3.oas.models.*; import io.swagger.v3.oas.models.info.*;
 * import io.swagger.v3.oas.models.security.*; import
 * org.springframework.context.annotation.Bean; import
 * org.springframework.context.annotation.Configuration;
 * 
 * @Configuration public class SwaggerConfig {
 * 
 * @Bean public OpenAPI openAPI() { return new OpenAPI() .info(new Info()
 * .title("Chatbot Help Center API") .description(""" APIs for Chatbot Help
 * Center. Supports User, Vendor, and Rider login. After login, JWT token
 * contains appId which controls chatbot routing. """) .version("v1.0")
 * .contact(new Contact() .name("Chatbot Team") .email("support@chatbot.com")))
 * // Adds "Authorize" button in Swagger UI .addSecurityItem( new
 * SecurityRequirement() .addList("Bearer Authentication")) .components(new
 * Components() .addSecuritySchemes( "Bearer Authentication", new
 * SecurityScheme() .type(SecurityScheme.Type.HTTP) .scheme("bearer")
 * .bearerFormat("JWT") .description( "Paste your JWT token here"))); } }
 */ 


package com.dhatvibs.modules.config.auth;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.*;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    // ── Group 1 — Chatbot Service APIs ──────────
    @Bean
    public GroupedOpenApi chatbotApis() {
        return GroupedOpenApi.builder()
            .group("1-chatbot-service")
            .displayName("Chatbot Service")
            .packagesToScan(
                "com.dhatvibs.modules.controller")
            .build();
    }

    // ── Group 2 — Rider Chatbot APIs ────────────
    @Bean
    public GroupedOpenApi riderApis() {
        return GroupedOpenApi.builder()
            .group("2-rider-chatbot")
            .displayName("Rider Chatbot")
            .packagesToScan(
                "com.dhatvibs.modules.rider.controller")
            .pathsToMatch("/rider/**")
            .build();
    }

    // ── Main OpenAPI info ────────────────────────
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Help Center APIs")
                .description(
                    "Chatbot + Rider Help Center")
                .version("v1.0"))
            .addSecurityItem(
                new SecurityRequirement()
                    .addList("Bearer Auth"))
            .components(new Components()
                .addSecuritySchemes(
                    "Bearer Auth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}