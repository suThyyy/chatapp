package com.oqnsuthyz.chatapp.config;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebsocketConfiguration implements WebSocketMessageBrokerConfigurer {

    private final WebsocketHandshake websocketHandshake;
    private final ClientInboundAuthentication clientInboundAuthentication;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Configure STOMP endpoint: ws://localhost:8080/ws
        // Allow frontend origin và add handshake interceptor
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:3000")
                .addInterceptors(websocketHandshake);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable in-memory message broker
        // /topic: broadcast messages (1-N), /queue: point-to-point (1-1)
        config.enableSimpleBroker("/topic", "/queue");

        // Prefix cho messages từ client → server
        // Client gửi đến /app/xxx → @MessageMapping("/xxx") xử lý
        config.setApplicationDestinationPrefixes("/app");

        // Prefix cho user-specific destinations
        // /user/{username}/queue/xxx → Spring convert thành session-specific
        // destination
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(@NonNull ChannelRegistration registration) {
        // Register ChannelInterceptor để authenticate STOMP CONNECT frames
        registration.interceptors(clientInboundAuthentication);
    }
}