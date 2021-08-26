package com.ngdesk.tesseract;

import java.io.IOException;
import java.util.HashMap;
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

	// TODO: this should be an array of object with path and type of request
	public String[] whitelistedInternalPaths = { "modules/[a-zA-Z0-9%]+/data$", "/attachments",
			"modules/[a-zA-Z0-9%]+/probes/data$" };

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		String authToken = httpServletRequest.getHeader("authentication_token");

		boolean noAuthCall = false;
		boolean internalCall = false;
		String requestMethod = httpServletRequest.getMethod();

		String requestUrl = httpServletRequest.getRequestURL().toString();

		for (String path : global.pathsWhitelisted) {
			Pattern pattern = Pattern.compile(path);
			if (pattern.matcher(requestUrl).find()) {
				noAuthCall = true;
			}
		}

		if (authToken == null) {
			for (String path : whitelistedInternalPaths) {
				Pattern pattern = Pattern.compile(path);
				if (pattern.matcher(requestUrl).find()) {
					internalCall = true;
					noAuthCall = true;
				}
			}
		}

		if (internalCall) {
			Map<String, String> queryParams = new HashMap<String, String>();
			Map<String, String[]> queryParamsMap = httpServletRequest.getParameterMap();
			for (String param : queryParamsMap.keySet()) {
				queryParams.put(param, queryParamsMap.get(param)[0]);
			}

			if (queryParams.containsKey("user_uuid") && queryParams.containsKey("company_id")) {
				manager.loadUserDetailsForInternalCalls(queryParams.get("user_uuid").toString(),
						queryParams.get("company_id").toString());
			} else {
				String subdomain = "dev1";
				String url = httpServletRequest.getHeader("x-forwarded-server");
				if (!url.contains("localhost")) {
					subdomain = url.split("\\.ngdesk\\.com")[0];
				}
				request.setAttribute("SUBDOMAIN", subdomain);
			}
		}

		if (httpServletRequest.getHeader("internal_call") != null
				&& httpServletRequest.getHeader("internal_call").equals("y")) {
			noAuthCall = true;
		}

		if (!noAuthCall) {
			manager.loadUserDetails(authToken);
		}
		chain.doFilter(request, response);
	}

}
