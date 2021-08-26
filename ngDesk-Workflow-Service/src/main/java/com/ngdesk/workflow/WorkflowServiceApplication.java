package com.ngdesk.workflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients({"com.ngdesk.commons", "com.ngdesk.workflow"})
@ComponentScan({"com.ngdesk.commons", "com.ngdesk.workflow", "com.ngdesk.repositories", "com.ngdesk.data.dao"})
public class WorkflowServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(WorkflowServiceApplication.class, args);
	}

}
