package com.dhatvibs.modules.config;


/*
 * import org.springframework.context.annotation.Configuration; import
 * org.springframework.messaging.simp.config.MessageBrokerRegistry; import
 * org.springframework.web.socket.config.annotation.*;
 * 
 * @Configuration
 * 
 * @EnableWebSocketMessageBroker public class WebSocketConfig implements
 * WebSocketMessageBrokerConfigurer {
 * 
 * @Override public void registerStompEndpoints( StompEndpointRegistry registry)
 * { // Frontend connects to this endpoint // ws://localhost:8082/ws/chat
 * registry.addEndpoint("/ws/chat") .setAllowedOriginPatterns("*")
 * .withSockJS(); // fallback for browsers }
 * 
 * @Override public void configureMessageBroker( MessageBrokerRegistry registry)
 * { // Frontend subscribes to /topic/session/{sessionId}
 * registry.enableSimpleBroker("/topic"); // Frontend sends messages to
 * /app/chat.message registry.setApplicationDestinationPrefixes("/app"); } }
 */  


import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements
        WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(
            StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/chat")
            // Allow all origins for CORS
            .setAllowedOriginPatterns("*")
            .withSockJS();
    }

    @Override
    public void configureMessageBroker(
            MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }
}