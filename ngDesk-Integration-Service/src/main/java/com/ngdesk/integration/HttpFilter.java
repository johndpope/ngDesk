package com.ngdesk.integration;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.Global;
import com.ngdesk.commons.managers.AuthManager;

@Component
@Order(2)
public class HttpFilter implements Filter {

	@Autowired
	AuthManager manager;

	@Autowired
	Global global;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		String authToken = httpServletRequest.getHeader("authentication_token");

		boolean noAuthCall = false;

		Map<String, List<String>> whiteListedPath = new HashMap<String, List<String>>();
		whiteListedPath.put("/zoom/authorized", Arrays.asList("GET"));
		whiteListedPath.put("/zoom/uninstall", Arrays.asList("POST"));
		whiteListedPath.put("/amazon/aws", Arrays.asList("POST"));
		whiteListedPath.put("/conference/xml", Arrays.asList("GET"));
		whiteListedPath.put("/microsoft_teams/ticket_status", Arrays.asList("POST"));
		whiteListedPath.put("/microsoft_team", Arrays.asList("POST", "GET"));
		whiteListedPath.put("/signature_document", Arrays.asList("GET", "PUT"));
		
		String requestUrl = httpServletRequest.getRequestURL().toString();
		for (String path : global.pathsWhitelisted) {
			Pattern pattern = Pattern.compile(path);
			if (pattern.matcher(requestUrl).find()) {
				noAuthCall = true;
			}
		}

		String method = httpServletRequest.getMethod();
		String path = httpServletRequest.getServletPath();

		if (whiteListedPath.containsKey(path) && whiteListedPath.get(path).contains(method)) {
			noAuthCall = true;
		}

		if (!noAuthCall) {
			manager.loadUserDetails(authToken);
		}

		chain.doFilter(request, response);
	}

}
