package com.ngdesk;

import org.slf4j.Logger; 
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	private final Logger log = LoggerFactory.getLogger(WebSocketConfig.class);

	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		try {
			config.enableSimpleBroker("rest");
			config.setApplicationDestinationPrefixes("ngdesk");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		try {
			registry.addEndpoint("/rest-websocket").setAllowedOrigins("*");
			registry.addEndpoint("/rest-websocket").setAllowedOrigins("*").withSockJS();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	 @Override
     public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
         registration.setMessageSizeLimit(200000); // default : 64 * 1024
         registration.setSendTimeLimit(20 * 10000); // default : 10 * 10000
         registration.setSendBufferSizeLimit(3 * 512 * 1024); // default : 512 * 1024
     }

}