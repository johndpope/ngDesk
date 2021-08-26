package com.ngdesk.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({ "com.ngdesk.commons" })
public class NgDeskAuthApplicationTests {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(NgDeskAuthApplicationTests.class, args);
	}

}
