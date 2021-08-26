package com.ngdesk.sam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.ngdesk.commons", "com.ngdesk.repositories", "com.ngdesk.sam"})
public class RepositoryTest {
	public static void main(String[] args) throws Exception {
        SpringApplication.run(RepositoryTest.class, args);
    }
}
