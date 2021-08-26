package com.ngdesk.gateway;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import reactor.core.publisher.Mono;

@Component
public class JsonExceptionHandler implements ErrorWebExceptionHandler {

	@Autowired
	Prometheus prometheus;

	private static final Logger log = LoggerFactory.getLogger(JsonExceptionHandler.class);
	/**
	 * MessageReader
	 */
	private List<HttpMessageReader<?>> messageReaders = Collections.emptyList();

	/**
	 * MessageWriter
	 */
	private List<HttpMessageWriter<?>> messageWriters = Collections.emptyList();

	/**
	 * ViewResolvers
	 */
	private List<ViewResolver> viewResolvers = Collections.emptyList();

	/**
	 * Store information after processing exceptions
	 */
	private ThreadLocal<Map<String, Object>> exceptionHandlerResult = new ThreadLocal<>();

	/**
	 * Reference AbstractErrorWebExceptionHandler
	 */
	public void setMessageReaders(List<HttpMessageReader<?>> messageReaders) {
		Assert.notNull(messageReaders, "'messageReaders' must not be null");
		this.messageReaders = messageReaders;
	}

	/**
	 * Reference AbstractErrorWebExceptionHandler
	 */
	public void setViewResolvers(List<ViewResolver> viewResolvers) {
		this.viewResolvers = viewResolvers;
	}

	/**
	 * Reference AbstractErrorWebExceptionHandler
	 */
	public void setMessageWriters(List<HttpMessageWriter<?>> messageWriters) {
		Assert.notNull(messageWriters, "'messageWriters' must not be null");
		this.messageWriters = messageWriters;
	}

	@Override
	public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
		// According to the type of exception processing
		log.debug("inside handle");
		try {
			HttpStatus httpStatus;
			String body;
			if (ex instanceof NotFoundException) {
				String path = exchange.getRequest().getPath().toString();
				String serviceName = "";
				try {
					serviceName = path.split("/")[2];
					prometheus.increment(serviceName);
				} catch (Exception e) {
					e.printStackTrace();
				}
				httpStatus = HttpStatus.SERVICE_UNAVAILABLE;
				body = serviceName + " Unavailable";
			} else if (ex instanceof ResponseStatusException) {
				ResponseStatusException responseStatusException = (ResponseStatusException) ex;
				httpStatus = responseStatusException.getStatus();
				body = responseStatusException.getMessage();
				ex.printStackTrace();
			} else {
				httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
				body = "Internal Server Error";
			}
			// Encapsulate the response body, this body can be modified to its own jsonBody
			Map<String, Object> result = new HashMap<>(2, 1);
			result.put("httpStatus", httpStatus);

			String msg = "{\"code\":\"" + httpStatus + "\",\"message\": \"" + body + "\"}";
			result.put("body", msg);
			// error record
			ServerHttpRequest request = exchange.getRequest();

			String emailAddress = "";
			String subdomain = "";
			if (request.getHeaders().containsKey("authentication_token")) {
				String jwt = exchange.getRequest().getHeaders().get("authentication_token").get(0);
				Claims claims = Jwts.parser().setSigningKey(AuthFilter.Config.getSigningKey()).parseClaimsJws(jwt)
						.getBody();
				subdomain = claims.get("SUBDOMAIN").toString();
				Map<String, Object> userMap = new ObjectMapper().readValue(claims.get("USER").toString(), Map.class);
				emailAddress = userMap.get("EMAIL_ADDRESS").toString();

			}
			String ip = "";
			if (request.getHeaders().containsKey("X-Forwarded-For")) {
				ip = request.getHeaders().get("X-Forwarded-For").get(0);
			}
			String userAgent = "";
			if (request.getHeaders().containsKey("user-agent")) {
				userAgent = request.getHeaders().get("user-agent").get(0);
			}

			log.error(
					"[Global Exception Handling] IP: {}, Browser: {}, Subdomain: {}, Email: {}, exception request path: {}, record exception information: {}",
					ip, userAgent, subdomain, emailAddress, request.getPath(), ExceptionUtils.getFullStackTrace(ex));
			// Reference AbstractErrorWebExceptionHandler
			if (exchange.getResponse().isCommitted()) {
				return Mono.error(ex);
			}
			exceptionHandlerResult.set(result);
			ServerRequest newRequest = ServerRequest.create(exchange, this.messageReaders);
			return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse).route(newRequest)
					.switchIfEmpty(Mono.error(ex)).flatMap((handler) -> handler.handle(newRequest))
					.flatMap((response) -> write(exchange, response));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Mono.error(ex);

	}

	/**
	 * Refer to DefaultErrorWebExceptionHandler
	 */
	protected Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
		Map<String, Object> result = exceptionHandlerResult.get();
		return ServerResponse.status((HttpStatus) result.get("httpStatus")).contentType(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromValue(result.get("body")));
	}

	/**
	 * Reference AbstractErrorWebExceptionHandler
	 */
	private Mono<? extends Void> write(ServerWebExchange exchange, ServerResponse response) {
		exchange.getResponse().getHeaders().setContentType(response.headers().getContentType());
		return response.writeTo(exchange, new ResponseContext());
	}

	/**
	 * Reference AbstractErrorWebExceptionHandler
	 */
	private class ResponseContext implements ServerResponse.Context {

		@Override
		public List<HttpMessageWriter<?>> messageWriters() {
			return JsonExceptionHandler.this.messageWriters;
		}

		@Override
		public List<ViewResolver> viewResolvers() {
			return JsonExceptionHandler.this.viewResolvers;
		}
	}
}
