package com.xskr.onk_v1;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
@Configuration
@EnableWebSocketMessageBroker
public class ONK_WebSocketMessageBrokerConfigurer implements WebSocketMessageBrokerConfigurer {

    public static final String ONK_PUBLIC = "/topic";
    public static final String ONK_END_POINT = "/onk/endpoint";
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/user", "/queue");
//        registry.enableStompBrokerRelay("/topic", "/user", "/queue");
//        registry.setApplicationDestinationPrefixes("/onk");
//        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(ONK_END_POINT).setAllowedOrigins("*").withSockJS();
    }

}
