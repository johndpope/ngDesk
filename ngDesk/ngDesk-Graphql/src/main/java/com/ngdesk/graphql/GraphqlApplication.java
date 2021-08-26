package com.ngdesk.graphql;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients({ "com.ngdesk.commons", "com.ngdesk.graphql" })
@ComponentScan({ "com.ngdesk.commons", "com.ngdesk.graphql", "com.ngdesk.repositories", "com.ngdesk.sam" })
public class GraphqlApplication {

	public static void main(String[] args) { 
		SpringApplication.run(GraphqlApplication.class, args); 
	}

}
