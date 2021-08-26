package com.ngdesk.sidebar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.ngdesk.commons"})
public class ApplicationTest {
    public static void main(String[] args) throws Exception {
        SpringApplication.run(ApplicationTest.class, args);
    }
}
