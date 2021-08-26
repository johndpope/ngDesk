package com.ngdesk.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;

import reactor.core.publisher.Mono;

@SpringBootApplication
@EnableDiscoveryClient
public class GatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication.class, args);
	}

	@Bean
	KeyResolver ipAddressResolver() {
	    return (exchange)-> {
	    	if (exchange.getRequest().getHeaders().containsKey("X-Forwarded-For") && exchange.getRequest().getHeaders().get("X-Forwarded-For") != null) {
	    		return Mono.just(exchange.getRequest().getHeaders().get("X-Forwarded-For").get(0));
	    	} 
	    	return Mono.just("1");
	    };
	}

}
