package com.ngdesk.knowledgebase;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients({"com.ngdesk.commons", "com.ngdesk.knowledgebase"})
@ComponentScan({"com.ngdesk.commons", "com.ngdesk.knowledgebase", "com.ngdesk.repositories"})
public class KnowledgeBaseApplication {

	public static void main(String[] args) {
		SpringApplication.run(KnowledgeBaseApplication.class, args);
	}

}
