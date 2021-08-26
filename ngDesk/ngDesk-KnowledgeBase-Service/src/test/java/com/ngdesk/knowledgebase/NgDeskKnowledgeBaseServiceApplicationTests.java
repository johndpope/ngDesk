package com.ngdesk.knowledgebase;

import org.springframework.boot.SpringApplication;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({ "com.ngdesk.commons" })
public class NgDeskKnowledgeBaseServiceApplicationTests {
	public static void main(String[] args) throws Exception {
		SpringApplication.run(NgDeskKnowledgeBaseServiceApplicationTests.class, args);
	}

}
