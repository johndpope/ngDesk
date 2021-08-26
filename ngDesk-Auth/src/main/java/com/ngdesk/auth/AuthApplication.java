package com.ngdesk.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients({"com.ngdesk.commons", "com.ngdesk.auth"})
@ComponentScan({"com.ngdesk.commons", "com.ngdesk.auth", "com.ngdesk.repositories"})
@EnableScheduling
public class AuthApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(AuthApplication.class, args);
	}

}
