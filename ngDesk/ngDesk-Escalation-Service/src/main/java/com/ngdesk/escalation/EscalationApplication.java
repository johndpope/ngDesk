package com.ngdesk.escalation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients({"com.ngdesk.commons", "com.ngdesk.escalation"})
@ComponentScan({"com.ngdesk.commons", "com.ngdesk.escalation", "com.ngdesk.repositories"})
public class EscalationApplication {

	public static void main(String[] args) {
		SpringApplication.run(EscalationApplication.class, args);
	}

	
}
