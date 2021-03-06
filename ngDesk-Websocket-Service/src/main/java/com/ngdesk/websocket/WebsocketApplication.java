package com.ngdesk.websocket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan({"com.ngdesk.commons", "com.ngdesk.websocket", "com.ngdesk.repositories", "com.ngdesk.data.dao","com.ngdesk.data.elastic"})
@EnableFeignClients({ "com.ngdesk.commons", "com.ngdesk.websocket" })
public class WebsocketApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebsocketApplication.class, args);
	}

}
