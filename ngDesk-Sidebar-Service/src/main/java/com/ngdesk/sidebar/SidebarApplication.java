package com.ngdesk.sidebar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients({"com.ngdesk.commons", "com.ngdesk.sidebar"})
@ComponentScan({"com.ngdesk.commons", "com.ngdesk.sidebar", "com.ngdesk.repositories"})
public class SidebarApplication {

	public static void main(String[] args) {
		SpringApplication.run(SidebarApplication.class, args);
	}

}
