package com.ngdesk.commons;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {
    @Bean
    public FeignErrorHandler feignErrorHandler() {
        return new FeignErrorHandler();
    }
}