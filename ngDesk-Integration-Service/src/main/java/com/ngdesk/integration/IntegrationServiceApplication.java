package com.ngdesk.integration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({ "com.ngdesk.commons", "com.ngdesk.integration", "com.ngdesk.repositories", "com.ngdesk.data" })
@EnableFeignClients({ "com.ngdesk.commons", "com.ngdesk.integration" })
@EnableDiscoveryClient
public class IntegrationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(IntegrationServiceApplication.class, args);
	}

}
