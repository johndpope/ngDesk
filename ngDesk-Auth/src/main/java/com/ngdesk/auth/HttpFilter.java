package com.ngdesk.auth;

import java.io.IOException;

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

		String requestUrl = httpServletRequest.getRequestURL().toString();
		System.out.println(requestUrl);

		chain.doFilter(request, response);
	}

}
