package com.ngdesk.module;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
@EnableFeignClients({ "com.ngdesk.commons", "com.ngdesk.module" })
@ComponentScan({ "com.ngdesk.commons", "com.ngdesk.module", "com.ngdesk.repositories", "com.ngdesk.workflow.dao", "com.ngdesk.data" })
public class ModuleApplication {

	public static void main(String[] args) {
		SpringApplication.run(ModuleApplication.class, args);
	}

}
