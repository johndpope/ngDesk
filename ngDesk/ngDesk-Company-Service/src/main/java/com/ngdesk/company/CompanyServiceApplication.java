package com.ngdesk.company;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@EnableDiscoveryClient
@EnableFeignClients({"com.ngdesk.commons", "com.ngdesk.company"})
@ComponentScan({"com.ngdesk.commons", "com.ngdesk.company", "com.ngdesk.repositories", "com.ngdesk.workflow.dao"})
@SpringBootApplication
public class CompanyServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CompanyServiceApplication.class, args);
	}

}
 