package com.ngdesk.gateway;

import java.util.Date;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import reactor.core.publisher.Mono;

@Component
public class AuthFilter extends AbstractGatewayFilterFactory<AuthFilter.Config> {
	
	@Autowired
	WhitelistService whitelistService;
	
	private static final Logger logger = LoggerFactory.getLogger(AuthFilter.class);

	public AuthFilter() {
		super(Config.class);
	}

	@Override
	public GatewayFilter apply(Config config) {

		// Custom pre-filter. Suppose we can extract JWT and perform Authentication
		return (exchange, chain) -> {

			try {

				String method = exchange.getRequest().getMethod().name();
				String path = exchange.getRequest().getURI().getPath();
				
				
				if (whitelistService.pathsMap.containsKey(path) && whitelistService.pathsMap.get(path).contains(method)) {
					return chain.filter(exchange).then(Mono.fromRunnable(() -> {
					}));
				}

				if (!exchange.getRequest().getHeaders().containsKey("authentication_token")) {
					exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
					return exchange.getResponse().setComplete();
				}

				String jwt = exchange.getRequest().getHeaders().get("authentication_token").get(0);
				Claims claims = Jwts.parser().setSigningKey(config.getSigningKey()).parseClaimsJws(jwt).getBody();
				Date expirationDate = claims.getExpiration();
				Date now = new Date();

				if (now.after(expirationDate)) {
					exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
					return exchange.getResponse().setComplete();
				}

				return chain.filter(exchange).then(Mono.fromRunnable(() -> {
				}));

			} catch (Exception e) {
				logger.error(ExceptionUtils.getFullStackTrace(e));
			}

			exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
			return exchange.getResponse().setComplete();

		};
	}

	public static class Config {
		private static String signingKey = "Vdu7IxJ5Lvcp0YUJ";

		public static String getSigningKey() {
			return signingKey;
		}

	}
}
