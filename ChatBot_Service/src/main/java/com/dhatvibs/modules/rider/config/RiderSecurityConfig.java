/*
 * package com.dhatvibs.modules.rider.config;
 * 
 * import com.dhatvibs.modules.rider.filter.RiderAuthFilter; import
 * lombok.RequiredArgsConstructor; import
 * org.springframework.context.annotation.Bean; import
 * org.springframework.context.annotation.Configuration; import
 * org.springframework.core.annotation.Order; import
 * org.springframework.security.config.annotation.web.builders.HttpSecurity;
 * import org.springframework.security.config.http.SessionCreationPolicy; import
 * org.springframework.security.web.SecurityFilterChain; import
 * org.springframework.security.web.authentication
 * .UsernamePasswordAuthenticationFilter; import
 * org.springframework.web.cors.CorsConfiguration; import
 * org.springframework.web.cors.CorsConfigurationSource; import
 * org.springframework.web.cors.UrlBasedCorsConfigurationSource;
 * 
 * import java.util.List;
 * 
 * @Configuration
 * 
 * @RequiredArgsConstructor
 * 
 * @Order(1) public class RiderSecurityConfig {
 * 
 * private final RiderAuthFilter riderAuthFilter;
 * 
 * // ← Method name changed to riderCorsConfig
 * 
 * @Bean(name = "riderCorsConfigurationSource") public CorsConfigurationSource
 * riderCorsConfig() { CorsConfiguration config = new CorsConfiguration();
 * config.setAllowedOriginPatterns(List.of("*")); config.setAllowedMethods(
 * List.of("GET","POST","PUT", "DELETE","OPTIONS"));
 * config.setAllowedHeaders(List.of("*")); config.setAllowCredentials(true);
 * 
 * UrlBasedCorsConfigurationSource source = new
 * UrlBasedCorsConfigurationSource(); source.registerCorsConfiguration( "/**",
 * config); return source; }
 * 
 * // ← Method name changed to riderFilterChain
 * 
 * @Bean(name = "riderFilterChain") public SecurityFilterChain riderFilterChain(
 * HttpSecurity http) throws Exception { return http .cors(c ->
 * c.configurationSource( riderCorsConfig())) .csrf(c -> c.disable())
 * .sessionManagement(s -> s .sessionCreationPolicy(
 * SessionCreationPolicy.STATELESS)) .authorizeHttpRequests(auth -> auth
 * .requestMatchers( "/auth/send-otp", "/auth/verify-otp", "/api/admin/**",
 * "/swagger-ui/**", "/v3/api-docs/**", "/webjars/**", "/ws/rider/**"
 * ).permitAll() .anyRequest().authenticated()) .addFilterBefore(
 * riderAuthFilter, UsernamePasswordAuthenticationFilter .class) .build(); } }
 */