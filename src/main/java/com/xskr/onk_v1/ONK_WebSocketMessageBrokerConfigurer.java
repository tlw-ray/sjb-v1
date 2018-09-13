package com.xskr.onk_v1;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
@Configuration
@EnableWebSocketMessageBroker
public class ONK_WebSocketMessageBrokerConfigurer implements WebSocketMessageBrokerConfigurer {
    public static final String QUEUE = "/queue";
    public static final String TOPIC = "/topic";
    public static final String ONK_END_POINT = "/endpoint";
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker(TOPIC, QUEUE);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(ONK_END_POINT).setAllowedOrigins("*").withSockJS();
    }

//    @Override
//    public void registerStompEndpoints(StompEndpointRegistry stompEndpointRegistry) {
//        //endpoint的名称可以换，而且多种消息可以通过一个endpoint来连接，为什么需要多个EndPoint有待于研究
//        stompEndpointRegistry.addEndpoint("/endpoint").withSockJS();
//    }
//
//    public void configureMessageBroker(MessageBrokerRegistry registry){
//        //点对点消息，必须名为/queue
//        //广播消息，必须名为/topic
//        registry.enableSimpleBroker("/queue", "/topic");
//    }
}
