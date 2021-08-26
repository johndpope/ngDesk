package com.ngdesk.sam;

import java.io.IOException;
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
@Order(1)
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

		String requestUrl = httpServletRequest.getRequestURL().toString();

		for (String path : Global.pathsWhitelisted) {
			Pattern pattern = Pattern.compile(path);
			if (pattern.matcher(requestUrl).find()) {
				noAuthCall = true;
			}
		}

		if (!noAuthCall) {
			manager.loadUserDetails(authToken);
		}

		chain.doFilter(request, response);
	}

}
