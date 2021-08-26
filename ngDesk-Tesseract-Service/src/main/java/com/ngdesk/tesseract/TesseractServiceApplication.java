package com.ngdesk.tesseract;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication 
@EnableDiscoveryClient
@EnableFeignClients({ "com.ngdesk.commons", "com.ngdesk.tesseract" }) 
@ComponentScan({ "com.ngdesk.commons", "com.ngdesk.tesseract", "com.ngdesk.repositories" })
@EnableScheduling
@EnableAsync
public class TesseractServiceApplication { 

	public static void main(String[] args) {
		SpringApplication.run(TesseractServiceApplication.class, args);
	}

}
