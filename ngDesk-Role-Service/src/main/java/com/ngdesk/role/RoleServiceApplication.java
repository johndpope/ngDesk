package com.ngdesk.role;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@EnableDiscoveryClient
@SpringBootApplication
@EnableFeignClients({"com.ngdesk.commons", "com.ngdesk.role"})
@ComponentScan({"com.ngdesk.commons", "com.ngdesk.role", "com.ngdesk.repositories"})
public class RoleServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(RoleServiceApplication.class, args); 
	}
 
}
