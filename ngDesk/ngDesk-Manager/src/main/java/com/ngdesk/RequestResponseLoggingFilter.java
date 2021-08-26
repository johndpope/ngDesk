package com.ngdesk;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class RequestResponseLoggingFilter implements Filter {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	Authentication auth;

	@Autowired
	Environment env;

	private final Logger log = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);

	@Override
	public void init(FilterConfig cfg) throws ServletException {

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;

		res.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
		res.setHeader("Access-Control-Allow-Methods", "GET,POST,DELETE,PUT,OPTIONS");
		res.setHeader("Access-Control-Allow-Headers", "*");
		res.setHeader("Access-Control-Allow-Credentials", "true");
		res.setHeader("Access-Control-Max-Age", "180");

		String requestMethod = req.getMethod();
		String requestUrl = req.getRequestURL().toString();
		boolean isValidRequest = true;
		Map<String, String> queryParams = new HashMap<String, String>();
		Map<String, String[]> queryParamsMap = request.getParameterMap();
		for (String param : queryParamsMap.keySet()) {
			queryParams.put(param, queryParamsMap.get(param)[0]);
		}
		if (queryParams.containsKey("authentication_token")) {
			String uuid = queryParams.get("authentication_token");

			if (auth.isValidUser(uuid)) {
				JSONObject userDetails = auth.getUserDetails(uuid);
				for (String key : userDetails.keySet()) {
					MDC.put(key.toLowerCase(), userDetails.get(key).toString());
				}
			} else {
				isValidRequest = false;
			}
		}

		String url = req.getRequestURL().toString();
		MDC.put("url", url);

		
		String subdomain = req.getHeader("x-forwarded-server");
		if (subdomain == null) {
			subdomain = "dev1";
			if (!url.contains("localhost") && !url.contains(env.getProperty("dev.ip")) && !url.contains("10.2.15.131")) {
				subdomain = url.split("://")[1].split("\\.ngdesk")[0];
			}
		}
		
		request.setAttribute("SUBDOMAIN", subdomain);
		request.setAttribute("LANGUAGE", "en");
		Pattern facebookWebhookPattern = Pattern.compile("/ngdesk/facebook/webhook$");
		Pattern smsWebhookPattern = Pattern.compile("/ngdesk/channels/sms/webhook$");
		Pattern smsWebhookStatusPattern = Pattern.compile("/ngdesk/channels/sms/status$");

		if (facebookWebhookPattern.matcher(requestUrl).find()) {
			if (!requestMethod.equalsIgnoreCase("GET") && !requestMethod.equalsIgnoreCase("POST")) {
				isValidRequest = false;
			}
		}

		if (smsWebhookPattern.matcher(requestUrl).find()) {
			if (!requestMethod.equalsIgnoreCase("GET") && !requestMethod.equalsIgnoreCase("POST")) {
				isValidRequest = false;
			}
		}

		if (smsWebhookStatusPattern.matcher(requestUrl).find()) {
			if (!requestMethod.equalsIgnoreCase("GET") && !requestMethod.equalsIgnoreCase("POST")) {
				isValidRequest = false;
			}
		}
		
		if (isValidRequest) {
			MDC.put("subdomain", subdomain);
			MDC.put("ipAddress", request.getRemoteAddr());
			MDC.put("transactionId", UUID.randomUUID().toString());
			chain.doFilter(request, response);
			MDC.clear();
		}
	}

	@Override
	public void destroy() {

	}

}
