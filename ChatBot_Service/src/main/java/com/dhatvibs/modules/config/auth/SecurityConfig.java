package com.dhatvibs.modules.config.auth;

import com.dhatvibs.modules.consumer.filter
        .ConsumerAuthFilter;
import com.dhatvibs.modules.rider.filter.RiderAuthFilter;
import com.dhatvibs.modules.riderchatbot.filter.RiderChatbotAuthFilter;
import com.dhatvibs.modules.vendor.filter.VendorAuthFilter;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation
        .Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation
        .web.builders.HttpSecurity;
import org.springframework.security.config.annotation
        .web.configuration.EnableWebSecurity;
import org.springframework.security.config.http
        .SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt
        .BCryptPasswordEncoder;
import org.springframework.security.crypto.password
        .PasswordEncoder;
import org.springframework.security.web
        .SecurityFilterChain;
import org.springframework.security.web.authentication
        .UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors
        .CorsConfiguration;
import org.springframework.web.cors
        .CorsConfigurationSource;
import org.springframework.web.cors
        .UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final HeaderAuthFilter   headerAuthFilter;
    private final ConsumerAuthFilter consumerAuthFilter;
    private final VendorAuthFilter   vendorAuthFilter;
    private final RiderAuthFilter    riderAuthFilter;
    private final AdminApiKeyFilter  adminApiKeyFilter;
    private final RiderChatbotAuthFilter riderChatbotAuthFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public CorsConfigurationSource
            corsConfigurationSource() {
        CorsConfiguration config =
            new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(
            List.of("GET","POST","PUT",
                    "DELETE","OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(
            "/**", config);
        return source;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain riderFilterChain(
            HttpSecurity http) throws Exception {
        return http
            .securityMatcher("/rider/**")
            .cors(c -> c.configurationSource(
                corsConfigurationSource()))
            .csrf(c -> c.disable())
            .sessionManagement(s -> s
                .sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/rider/auth/**",
                    "/ws/rider/**"
                ).permitAll()
                .requestMatchers(
                    "/rider/admin/**"
                ).hasRole("ADMIN")
                .anyRequest().authenticated())
            .addFilterBefore(
                adminApiKeyFilter,
                UsernamePasswordAuthenticationFilter
                    .class)
            .addFilterBefore(
                riderAuthFilter,
                UsernamePasswordAuthenticationFilter
                    .class)
            .build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain consumerFilterChain(
            HttpSecurity http) throws Exception {
        return http
            .securityMatcher("/consumer/**")
            .cors(c -> c.configurationSource(
                corsConfigurationSource()))
            .csrf(c -> c.disable())
            .sessionManagement(s -> s
                .sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/consumer/auth/**",
                    "/ws/consumer/**"
                ).permitAll()
                .requestMatchers(
                    "/consumer/admin/**"
                ).hasRole("ADMIN")
                .anyRequest().authenticated())
            .addFilterBefore(
                adminApiKeyFilter,
                UsernamePasswordAuthenticationFilter
                    .class)
            .addFilterBefore(
                consumerAuthFilter,
                UsernamePasswordAuthenticationFilter
                    .class)
            .build();
    }

    @Bean
    @Order(3)
    public SecurityFilterChain vendorFilterChain(
            HttpSecurity http) throws Exception {
        return http
            .securityMatcher("/vendor/**")
            .cors(c -> c.configurationSource(
                corsConfigurationSource()))
            .csrf(c -> c.disable())
            .sessionManagement(s -> s
                .sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/vendor/auth/**",
                    "/ws/vendor/**"
                ).permitAll()
                .requestMatchers(
                    "/vendor/admin/**"
                ).hasRole("ADMIN")
                .anyRequest().authenticated())
            .addFilterBefore(
                adminApiKeyFilter,
                UsernamePasswordAuthenticationFilter
                    .class)
            .addFilterBefore(
                vendorAuthFilter,
                UsernamePasswordAuthenticationFilter
                    .class)
            .build();
    }
    
    
    @Bean
    @Order(4)
    public SecurityFilterChain riderChatbotFilterChain(
            HttpSecurity http) throws Exception {
        return http
            .securityMatcher("/riderchatbot/**")
            .cors(c -> c.configurationSource(
                corsConfigurationSource()))
            .csrf(c -> c.disable())
            .sessionManagement(s -> s
                .sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/riderchatbot/auth/**",
                    "/riderchatbot/admin/**",
                    "/ws/riderchatbot/**"
                ).permitAll()
                .anyRequest().authenticated())
            .addFilterBefore(
                riderChatbotAuthFilter,
                UsernamePasswordAuthenticationFilter
                    .class)
            .build();
    }


    @Bean
    @Order(5)
    public SecurityFilterChain filterChain(
            HttpSecurity http) throws Exception {
        return http
            .cors(c -> c.configurationSource(
                corsConfigurationSource()))
            .csrf(c -> c.disable())
            .sessionManagement(s -> s
                .sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/auth/login",
                    "/auth/logout",
                    "/swagger-ui/**",
                    "/swagger-ui/index.html",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/v3/api-docs",
                    "/webjars/**",
                    "/ws/chat/**",
                    "/ws/rider/**",
                    "/ws/consumer/**",
                    "/ws/vendor/**"
                ).permitAll()
                .anyRequest().authenticated())
            .addFilterBefore(
                headerAuthFilter,
                UsernamePasswordAuthenticationFilter
                    .class)
            .build();
    }
}
