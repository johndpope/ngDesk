package com.ngdesk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

@Component
@Controller
public class Ping {
	
	private final Logger log = LoggerFactory.getLogger(Ping.class);
	
	@MessageMapping("/ping")
	public void ping(String message) {
		log.trace("ping() message: " + message);
	}

}
