package com.ngdesk.gateway;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class GlobalPreFilter implements GlobalFilter {

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		if (!exchange.getRequest().getURI().toString().contains("outlook")
				&& !exchange.getRequest().getURI().toString().contains("forms-widget") &&
				!exchange.getRequest().getURI().toString().contains("chat-widget")) {
			exchange.getResponse().getHeaders().add("X-Frame-Options", "DENY");
		}
		return chain.filter(exchange);
	}
}