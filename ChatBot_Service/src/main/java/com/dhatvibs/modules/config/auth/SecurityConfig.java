package com.dhatvibs.modules.config.auth;

/*
 * import org.springframework.context.annotation.Bean; import
 * org.springframework.context.annotation.Configuration; import
 * org.springframework.security.config.annotation .web.builders.HttpSecurity;
 * import org.springframework.security.config.annotation
 * .web.configuration.EnableWebSecurity; import
 * org.springframework.security.config.http .SessionCreationPolicy; import
 * org.springframework.security.crypto.bcrypt .BCryptPasswordEncoder; import
 * org.springframework.security.crypto.password .PasswordEncoder; import
 * org.springframework.security.web.SecurityFilterChain;
 * 
 * @Configuration
 * 
 * @EnableWebSecurity public class SecurityConfig {
 * 
 * @Bean public PasswordEncoder passwordEncoder() { return new
 * BCryptPasswordEncoder(10); }
 * 
 * 
 * @Bean public SecurityFilterChain filterChain(HttpSecurity http) throws
 * Exception { return http .csrf(csrf -> csrf.disable())
 * .sessionManagement(session -> session .sessionCreationPolicy(
 * SessionCreationPolicy.STATELESS)) .authorizeHttpRequests(auth -> auth //
 * Public endpoints .requestMatchers( "/auth/login", "/auth/logout", // Swagger
 * UI URLs "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**",
 * "/v3/api-docs" ).permitAll() .anyRequest().authenticated()) .build(); }
 * 
 * @Bean public SecurityFilterChain filterChain(HttpSecurity http) throws
 * Exception { return http .csrf(csrf -> csrf.disable())
 * .sessionManagement(session -> session
 * .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
 * .authorizeHttpRequests(auth -> auth .requestMatchers( "/auth/login",
 * "/auth/logout", "/api/chat/**", "/v3/api-docs", "/v3/api-docs/**",
 * "/v3/api-docs.yaml", "/swagger-ui/**", "/swagger-ui/index.html",
 * "/swagger-ui.html", "/webjars/**" ).permitAll()
 * .anyRequest().authenticated()) .build(); }
 * 
 * }
 */ 

/*
 * import org.springframework.context.annotation.Bean; import
 * org.springframework.context.annotation.Configuration; import
 * org.springframework.security.config.annotation.web.builders.HttpSecurity;
 * import org.springframework.security.config.annotation.web.configuration.
 * EnableWebSecurity; import
 * org.springframework.security.config.http.SessionCreationPolicy; import
 * org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; import
 * org.springframework.security.crypto.password.PasswordEncoder; import
 * org.springframework.security.web.SecurityFilterChain; import
 * org.springframework.security.web.authentication.
 * UsernamePasswordAuthenticationFilter;
 * 
 * @Configuration
 * 
 * @EnableWebSecurity public class SecurityConfig {
 * 
 * private final HeaderAuthFilter headerAuthFilter;
 * 
 * public SecurityConfig(HeaderAuthFilter headerAuthFilter) {
 * this.headerAuthFilter = headerAuthFilter; }
 * 
 * @Bean public PasswordEncoder passwordEncoder() { return new
 * BCryptPasswordEncoder(10); }
 * 
 * @Bean public SecurityFilterChain filterChain(HttpSecurity http) throws
 * Exception { return http .csrf(csrf -> csrf.disable())
 * .sessionManagement(session -> session .sessionCreationPolicy(
 * SessionCreationPolicy.STATELESS)) .authorizeHttpRequests(auth -> auth //
 * Public endpoints — no token needed .requestMatchers( "/auth/login",
 * "/auth/logout", "/swagger-ui/**", "/swagger-ui/index.html",
 * "/swagger-ui.html", "/v3/api-docs/**", "/v3/api-docs", "/webjars/**"
 * ).permitAll() // All /api/** endpoints — validated by HeaderAuthFilter
 * .anyRequest().authenticated() ) // Add header filter BEFORE Spring Security's
 * default filter // This reads X-User-Id and X-App-Id injected by gateway
 * .addFilterBefore( headerAuthFilter,
 * UsernamePasswordAuthenticationFilter.class) .build(); } }
 */  


/*
 * import org.springframework.context.annotation.Bean; import
 * org.springframework.context.annotation.Configuration; import
 * org.springframework.security.config.annotation.web.builders.HttpSecurity;
 * import org.springframework.security.config.annotation.web.configuration.
 * EnableWebSecurity; import
 * org.springframework.security.config.http.SessionCreationPolicy; import
 * org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; import
 * org.springframework.security.crypto.password.PasswordEncoder; import
 * org.springframework.security.web.SecurityFilterChain; import
 * org.springframework.security.web.authentication.
 * UsernamePasswordAuthenticationFilter; import
 * org.springframework.web.cors.CorsConfiguration; import
 * org.springframework.web.cors.CorsConfigurationSource; import
 * org.springframework.web.cors.UrlBasedCorsConfigurationSource;
 * 
 * import java.util.List;
 * 
 * @Configuration
 * 
 * @EnableWebSecurity public class SecurityConfig {
 * 
 * private final HeaderAuthFilter headerAuthFilter;
 * 
 * public SecurityConfig(HeaderAuthFilter headerAuthFilter) {
 * this.headerAuthFilter = headerAuthFilter; }
 * 
 * @Bean public PasswordEncoder passwordEncoder() { return new
 * BCryptPasswordEncoder(10); }
 * 
 * @Bean public CorsConfigurationSource corsConfigurationSource() {
 * CorsConfiguration config = new CorsConfiguration();
 * 
 * // Allow all origins — change to specific // frontend URL in production
 * config.setAllowedOriginPatterns(List.of("*")); config.setAllowedMethods(
 * List.of("GET","POST","PUT", "DELETE","OPTIONS"));
 * config.setAllowedHeaders(List.of("*")); config.setAllowCredentials(true);
 * config.setExposedHeaders( List.of("Authorization", "X-User-Id","X-App-Id"));
 * 
 * UrlBasedCorsConfigurationSource source = new
 * UrlBasedCorsConfigurationSource(); // Apply CORS to all endpoints
 * source.registerCorsConfiguration("/**", config); return source; }
 * 
 * @Bean public SecurityFilterChain filterChain( HttpSecurity http) throws
 * Exception { return http // Enable CORS with our config .cors(cors -> cors
 * .configurationSource( corsConfigurationSource())) .csrf(csrf ->
 * csrf.disable()) .sessionManagement(session -> session .sessionCreationPolicy(
 * SessionCreationPolicy.STATELESS)) .authorizeHttpRequests(auth -> auth
 * .requestMatchers( "/auth/login", "/auth/logout", "/ws/chat/**",
 * "/swagger-ui/**", "/swagger-ui/index.html", "/swagger-ui.html",
 * "/v3/api-docs/**", "/v3/api-docs", "/webjars/**" ).permitAll()
 * .anyRequest().authenticated()) .addFilterBefore( headerAuthFilter,
 * UsernamePasswordAuthenticationFilter.class) .build(); } }
 */ 


import com.dhatvibs.modules.rider.filter.RiderAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors
        .CorsConfigurationSource;
import org.springframework.web.cors
        .UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final HeaderAuthFilter headerAuthFilter;
    private final RiderAuthFilter  riderAuthFilter;

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
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(
            "/**", config);
        return source;
    }

    // ── Chain 1 — Rider paths (order=1, runs first)
    @Bean
    @Order(1)
    public SecurityFilterChain riderFilterChain(
            HttpSecurity http) throws Exception {

        return http
            // Apply only to rider paths
            .securityMatcher(
            		"/rider/**")
            .cors(c -> c.configurationSource(
                corsConfigurationSource()))
            .csrf(c -> c.disable())
            .sessionManagement(s -> s
                .sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // these rider paths are public
                .requestMatchers(
                		"/rider/auth/**",       // public
                        "/rider/admin/**",      // public
                        "/ws/rider/**" 
                ).permitAll()
                .anyRequest().authenticated())
            .addFilterBefore(
                riderAuthFilter,
                UsernamePasswordAuthenticationFilter
                    .class)
            .build();
    }

    // ── Chain 2 — All other paths (order=2, runs second)
    @Bean
    @Order(2)
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
                    "/ws/chat/**"
                ).permitAll()
                .anyRequest().authenticated())
            .addFilterBefore(
                headerAuthFilter,
                UsernamePasswordAuthenticationFilter
                    .class)
            .build();
    }
}