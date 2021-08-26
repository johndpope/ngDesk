package com.ngdesk.data;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication 
@EnableDiscoveryClient
@EnableFeignClients({ "com.ngdesk.commons", "com.ngdesk.data" }) 
@ComponentScan({ "com.ngdesk.commons", "com.ngdesk.data", "com.ngdesk.repositories" })
@EnableScheduling
@EnableAsync
public class DataServiceApplication { 

	public static void main(String[] args) { 
		SpringApplication.run(DataServiceApplication.class, args);
	}

}
