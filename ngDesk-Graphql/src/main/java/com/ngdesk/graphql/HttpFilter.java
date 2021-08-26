package com.ngdesk.graphql;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.commons.managers.SessionManager;
import com.ngdesk.graphql.company.dao.Company;
import com.ngdesk.repositories.company.CompanyRepository;
import com.ngdesk.repositories.dns.DnsRecordRepository;

@Component
@Order(1)
public class HttpFilter implements Filter {

	@Autowired
	AuthManager manager;

	@Autowired
	Global global;

	@Autowired
	DnsRecordRepository dnsRecordRepository;

	@Autowired
	SessionManager sessionManager;

	@Autowired
	CompanyRepository companyRepository;

	// TODO: this should be an array of object with path and type of request
	public String[] whitelistedInternalPaths = { "/reports/generate" };

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest httpServletRequest = (HttpServletRequest) request;

		String authToken = httpServletRequest.getHeader("authentication_token");

		boolean noAuthCall = false;
		boolean internalCall = false;

		String requestUrl = httpServletRequest.getRequestURL().toString();

		for (String path : Global.pathsWhitelisted) {
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
			}
		}

		if (!noAuthCall) {
			manager.loadUserDetails(authToken);
		}
		String url = httpServletRequest.getHeader("x-forwarded-server");
		if (url != null) {
			String subDomain = getSubDomain(url);
			Company company = companyRepository.findByCompanySubdomain(subDomain).orElse(null);

			if (company == null) {
				throw new BadRequestException("INVALID_COMPANY", null);
			}
			
			sessionManager.getSessionInfo().put("companyId", company.getCompanyId());
			sessionManager.getSessionInfo().put("subdomain", subDomain);
		}

		chain.doFilter(request, response);
	}

	public String getSubDomain(String requestURL) {
		String subDomain = "";

		if (requestURL.equals("localhost") || requestURL.equals("127.0.0.1")) {
			subDomain = "dev1";
		} else if (!requestURL.endsWith("ngdesk.com")) {

			String cname = requestURL;
			Optional<Map<String, Object>> optionalCname = dnsRecordRepository.findDNSRecordByCname(cname,
					"dns_records");
			if (optionalCname.isPresent()) {
				subDomain = (String) optionalCname.get().get("COMPANY_SUBDOMAIN");
			}
		} else {
			subDomain = requestURL.split("\\.ngdesk\\.com")[0];
		}
		return subDomain;
	}

}
