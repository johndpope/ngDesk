package com.ngdesk;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Configuration
public class ListenerConfig {
	@Bean
	@Description("Tracks user presence (join / leave) and broacasts it to all connected users")
	public WebSocketListener webSocketListener(SimpMessagingTemplate messagingTemplate) {
		WebSocketListener listener = new WebSocketListener(messagingTemplate);
		return listener;
	}
}
