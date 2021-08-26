package com.ngdesk.notifications;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@EnableDiscoveryClient
@EnableFeignClients({ "com.ngdesk.commons", "com.ngdesk.notifications" })
@ComponentScan({ "com.ngdesk.commons", "com.ngdesk.repositories", "com.ngdesk.notifications" })
@SpringBootApplication
public class NotificationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotificationServiceApplication.class, args);

	}

}
