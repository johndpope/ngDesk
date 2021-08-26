package com.ngdesk.module;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({ "com.ngdesk.commons" })
public class NgDeskModulesServiceApplicationTests {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(NgDeskModulesServiceApplicationTests.class, args);
	}
}
