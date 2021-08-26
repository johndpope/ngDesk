package com.ngdesk;

import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class WebConfig implements WebMvcConfigurer {

//	@Override
//    public void addCorsMappings(CorsRegistry registry) {
//		registry.addMapping("/**")
//        .allowedOrigins("/*")
//        .allowCredentials(true)
//        .maxAge(3600)
//        .allowedHeaders("Accept", "Content-Type", "Origin", 
//        		"Authorization", "X-Auth-Token", "Access-Control-Allow-Origin")
//        .exposedHeaders("X-Auth-Token", "Authorization")
//        .allowedMethods("*")
//        .allowedMethods("POST", "GET", "DELETE", "PUT", "OPTIONS")
//        ;
//    }

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/*").allowedOrigins("*");
	}
}
