
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
    
    
    @Bean
    public GroupedOpenApi consumerApis() {
        return GroupedOpenApi.builder()
            .group("3-consumer-module")
            .displayName("Consumer Chatbot Module")
            .packagesToScan(
                "com.dhatvibs.modules.consumer"
                + ".controller")
            .pathsToMatch("/consumer/**")
            .build();
    }
    
 // Add Group 4 — Vendor Module
    @Bean
    public GroupedOpenApi vendorApis() {
        return GroupedOpenApi.builder()
            .group("4-vendor-module")
            .displayName("Vendor Chatbot Module")
            .packagesToScan(
                "com.dhatvibs.modules.vendor.controller")
            .pathsToMatch("/vendor/**")
            .build();
    }
    
    
    @Bean
    public GroupedOpenApi riderChatbotApis() {
        return GroupedOpenApi.builder()
            .group("5-riderchatbot-module")
            .displayName("Rider Chatbot Module")
            .packagesToScan(
                "com.dhatvibs.modules.riderchatbot"
                + ".controller")
            .pathsToMatch("/riderchatbot/**")
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