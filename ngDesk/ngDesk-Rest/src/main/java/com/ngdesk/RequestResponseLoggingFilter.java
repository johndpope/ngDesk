package com.ngdesk;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Enumeration;
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
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.InternalErrorException;

@Component
@Order(1)
public class RequestResponseLoggingFilter implements Filter {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Authentication auth;

	@Autowired
	private Global global;

	@Value("${env}")
	private String envProp;

	@Value("${email.host}")
	private String emailHostProp;

	private final Logger log = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);

	@Override
	public void init(FilterConfig cfg) throws ServletException {

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		try {

			MDC.put("transactionId", UUID.randomUUID().toString());
			log.trace("Enter RequestResponseLoggingFilter.doFilter()");
			// res.setHeader("Access-Control-Allow-Origin", "*");
			res.setHeader("Access-Control-Allow-Methods", "GET,POST,DELETE,PUT,OPTIONS");
			res.setHeader("Access-Control-Allow-Headers", "*");
			res.setHeader("Access-Control-Allow-Credentials", "true");
			res.setHeader("Access-Control-Max-Age", "180");

			// REQUEST
			JSONObject requestObj = new JSONObject();
			JSONObject responseObj = new JSONObject();
			String requestMethod = req.getMethod();
			String requestUrl = req.getRequestURL().toString();
			boolean isValidRequest = false;
			JSONObject headers = getHeaders(req);
			JSONObject cookies = setCookies(req);
			String companyId = null;

			log.info(requestMethod + " => " + requestUrl);

			Pattern companiesPattern = Pattern.compile("ngdesk/companies$");
			Pattern companiesPatternV1 = Pattern.compile("ngdesk/companies/v1$");
			Pattern companiesSubdomainPattern = Pattern.compile("ngdesk/companies/subdomain$");
			Pattern loginPattern = Pattern.compile("ngdesk/users/login$");
			Pattern modulesPattern = Pattern.compile("ngdesk/modules/[a-zA-Z0-9%]+/data$");
			Pattern dataPattern = Pattern.compile("ngdesk/modules/[a-zA-Z0-9%]+/data/[0-9a-zA-Z]+$");
			Pattern themesPattern = Pattern.compile("ngdesk/companies/themes$");
			Pattern spencerPattern = Pattern.compile("ngdesk/spencer$");
			Pattern customerSignupPattern = Pattern.compile("ngdesk/companies/customers$");
			Pattern forgotPassword = Pattern.compile("ngdesk/companies/users/forgot_password$");
			Pattern resetPassword = Pattern.compile("ngdesk/companies/users/reset_password$");
			Pattern verifyEmail = Pattern.compile("ngdesk/channels/email/forwarding/verify$");
			Pattern parseEmail = Pattern.compile("ngdesk/emails/parse$");
			Pattern userTokens = Pattern.compile("ngdesk/users/tokens$");
			Pattern userEmailVerify = Pattern.compile("ngdesk/users/email/verify$");
			Pattern userAttempts = Pattern.compile("ngdesk/users/reset_attempts$");
			Pattern landingPattern1 = Pattern.compile("ngdesk/marketing/conversions$");
			Pattern enrollment = Pattern.compile("/ngdesk/companies/enrollment$");
			Pattern twilioCall = Pattern.compile("/ngdesk/TwilioCall$");
			Pattern actuator = Pattern.compile("/ngdesk/actuator$");
			Pattern actuators = Pattern.compile("/ngdesk/actuator/.*");
			Pattern logPattern = Pattern.compile("/ngdesk/log$");
			Pattern attachmentPattern = Pattern.compile("/ngdesk/attachments$");
			Pattern swaggerPattern = Pattern.compile("/ngdesk/swagger-ui.html$");
			Pattern webjarsPattern = Pattern.compile("/ngdesk/webjars(.*)$");
			Pattern swaggerResources = Pattern.compile("/ngdesk/swagger-resources(.*)$");
			Pattern apiDocs = Pattern.compile("/ngdesk/v2(.*)$");
			Pattern cnameChallengePattern = Pattern.compile("/ngdesk/companies/dns/cname/challenge/(.*)$");
			Pattern cnameCertPattern = Pattern.compile("/ngdesk/companies/dns/cname/certificate$");
			Pattern cnameCsrPattern = Pattern.compile("/ngdesk/companies/dns/cname/private_key$");
			Pattern categoriesPattern = Pattern.compile("/ngdesk/categories(.*)$");
			Pattern sectionsPattern = Pattern.compile("/ngdesk/sections(.*)$");
			Pattern articlesPattern = Pattern.compile("/ngdesk/articles(.*)$");
			Pattern knowledgeBaseGeneralPattern = Pattern.compile("/ngdesk/companies/knowledgebase/general$");
			Pattern reportDownloadPattern = Pattern.compile("/ngdesk/reports/schedules/download(.*)$");
			Pattern chatChannelPattern = Pattern.compile("/ngdesk/channels/chat/(.*)$");
			Pattern chatCountryPattern = Pattern.compile("/ngdesk/companies/ip_to_location$");
			Pattern dnsInsertPattern = Pattern.compile("/ngdesk/companies/dns$");
			Pattern facebookLoginPattern = Pattern.compile("/ngdesk/facebook/login$");
			Pattern googleLoginPattern = Pattern.compile("/ngdesk/google/login$");
			Pattern twitterLoginPattern = Pattern.compile("/ngdesk/twitter/login$");
			Pattern microsoftLoginPattern = Pattern.compile("/ngdesk/microsoft/login$");
			Pattern facebookWebhookPattern = Pattern.compile("/ngdesk/facebook/webhook$");
			Pattern facebookUsersLoginRedirectPattern = Pattern.compile("/ngdesk/users/login/facebook/redirect");
			Pattern facebookUsersLoginPattern = Pattern.compile("/ngdesk/users/login/facebook");
			Pattern googleUsersLoginRedirectPattern = Pattern.compile("/ngdesk/users/login/google/redirect");
			Pattern googleUsersLoginPattern = Pattern.compile("/ngdesk/users/login/google");
			Pattern twitterUsersLoginRedirectPattern = Pattern.compile("/ngdesk/users/login/twitter/redirect");
			Pattern twitterUsersLoginPattern = Pattern.compile("/ngdesk/users/login/twitter");
			Pattern microsoftUsersLoginRedirectPattern = Pattern.compile("/ngdesk/users/login/microsoft/redirect");
			Pattern microsoftUsersLoginPattern = Pattern.compile("/ngdesk/users/login/microsoft");
			Pattern signupPattern = Pattern.compile("/company/signup/[a-f0-9-]{36}$");
			Pattern controllerLogsPattern = Pattern.compile("/companies/controllers/[a-f0-9-]{36}/logs");

			Pattern whatsAppRequestUrl = Pattern.compile("/ngdesk/modules/(.*)/channels/sms/(.*)/request/verify");
			Pattern socialSignInPattern = Pattern.compile("/ngdesk/companies/security/social_sign_in");
			Pattern chatwidgetPattern = Pattern.compile("/ngdesk/channels/chat_widget/[a-f0-9]{24}$");
			Pattern unsubscriptionToMarketingEmail = Pattern
					.compile("/ngdesk/companies/users/marketing/emails/unsubscribe");
			Pattern userEvents = Pattern.compile("/ngdesk/companies/track/user/event");
			Pattern campaignImagePattern = Pattern.compile("/ngdesk/companies/gallery/image/(.*)$");
			Pattern campaignTrackingPattern = Pattern.compile("/ngdesk/companies/campaign/tracking");
			Pattern campaignButtonTrackingPattern = Pattern
					.compile("/ngdesk/companies/campaign/(.*)/tracking/button/(.*)$");

			Pattern samInstallerPattern = Pattern.compile("/ngdesk/sam/installer/download$");
			Pattern controllerPostPattern = Pattern.compile("/controllers$");

			Pattern haloocomSsoPattern = Pattern.compile("/users/sso$");
			Pattern haloocomLeadGenPattern = Pattern.compile("/lead$");

			Map<String, String> queryParams = new HashMap<String, String>();
			Map<String, String[]> queryParamsMap = request.getParameterMap();
			for (String param : queryParamsMap.keySet()) {
				queryParams.put(param, queryParamsMap.get(param)[0]);
			}

//			Enumeration<String> headerNames = req.getHeaderNames();
//	        while (headerNames.hasMoreElements()) {
//	            String key = (String) headerNames.nextElement();
//	            String value = req.getHeader(key);
//	            System.out.println(key + ":" + value);
//	        }

//			String url = req.getRequestURL().toString();
			String url = req.getHeader("x-forwarded-server");

			String subdomain = "dev1";
			if (url != null) {

				if (!url.contains("localhost") && !url.contains("10.2.15.85") && !url.contains("10.2.15.131")) {
//				if (!url.contains("localhost") && !url.contains("10.2.15.85") && !url.contains("rest-service")) {
					subdomain = url.split("\\.ngdesk\\.com")[0];
				}

//				String domain = url.split("://")[1].split("/")[0];
//				if (!domain.endsWith(".ngdesk.com")) {
				if (!url.endsWith(".ngdesk.com")) {
					MongoCollection<Document> dnsRecordsCollection = mongoTemplate.getCollection("dns_records");
					Document dnsRecord = dnsRecordsCollection.find(Filters.eq("CNAME", url)).first();
					if (dnsRecord != null) {
						subdomain = dnsRecord.getString("COMPANY_SUBDOMAIN");
					}
				}
				request.setAttribute("SUBDOMAIN", subdomain);
			} else {
				String reqUrl = req.getRequestURL().toString();
				if (reqUrl != null && (reqUrl.contains("localhost") || reqUrl.contains("10.2.15.85"))) {
					request.setAttribute("SUBDOMAIN", subdomain);
				}
			}

			String environment = emailHostProp.split("\\.")[0];
			request.setAttribute("LANGUAGE", "en");

			if (requestMethod.equalsIgnoreCase("OPTIONS")) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("POST") && companiesPattern.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("PUT") && companiesPattern.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("POST") && companiesPatternV1.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("GET") && companiesPattern.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("GET") && whatsAppRequestUrl.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("GET")
					&& knowledgeBaseGeneralPattern.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("GET") && categoriesPattern.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("GET") && sectionsPattern.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("GET") && chatChannelPattern.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("GET") && chatCountryPattern.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("GET") && articlesPattern.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("GET") && haloocomLeadGenPattern.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("GET") && haloocomSsoPattern.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("GET") && requestUrl.contains("/form_data/")) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("POST") && requestUrl.contains("/forms/data")) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("PUT") && signupPattern.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("GET") && signupPattern.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("GET") && cnameChallengePattern.matcher(requestUrl).find()) {
				request.setAttribute("CNAME", url);
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("GET") && socialSignInPattern.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("GET") && cnameCertPattern.matcher(requestUrl).find()) {
//				String env = url.split("://")[1].split("/")[0];
				isValidRequest = true;
//				if (!env.equals("localhost:9080")) {
//					isValidRequest = false;
//				}
			} else if (requestMethod.equalsIgnoreCase("GET") && cnameCsrPattern.matcher(requestUrl).find()) {
//				String env = url.split("://")[1].split("/")[0];
				isValidRequest = true;
//				if (!env.equals("localhost:9080")) {
//					isValidRequest = false;
//				}
			} else if (requestMethod.equalsIgnoreCase("GET") && swaggerPattern.matcher(requestUrl).find()
					&& !environment.equals("prd")) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("GET") && webjarsPattern.matcher(requestUrl).find()
					&& !environment.equals("prd")) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("GET") && swaggerResources.matcher(requestUrl).find()
					&& !environment.equals("prd")) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("GET") && apiDocs.matcher(requestUrl).find()
					&& !environment.equals("prd")) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("GET") && companiesPattern.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("GET")
					&& facebookUsersLoginRedirectPattern.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("GET")
					&& googleUsersLoginRedirectPattern.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("GET")
					&& twitterUsersLoginRedirectPattern.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("GET")
					&& microsoftUsersLoginRedirectPattern.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("GET") && facebookUsersLoginPattern.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("GET") && googleUsersLoginPattern.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("GET") && twitterUsersLoginPattern.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("GET") && microsoftUsersLoginPattern.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("GET") && companiesSubdomainPattern.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("POST") && loginPattern.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("POST") && modulesPattern.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("GET") && modulesPattern.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("PUT") && dataPattern.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("DELETE") && modulesPattern.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("GET") && themesPattern.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("GET") && spencerPattern.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("GET") && actuator.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if ((requestMethod.equalsIgnoreCase("GET") || requestMethod.equalsIgnoreCase("POST"))
					&& actuators.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("POST") && customerSignupPattern.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("POST") && forgotPassword.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("POST") && resetPassword.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("GET") && requestUrl.contains("health")) {
				isValidRequest = true;
			} else if ((requestMethod.equalsIgnoreCase("GET") || requestMethod.equalsIgnoreCase("POST"))
					&& attachmentPattern.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("GET") && requestUrl.contains("kamailio/auth")) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("POST") && verifyEmail.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("POST") && parseEmail.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("POST") && userTokens.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("POST") && userEmailVerify.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("GET") && landingPattern1.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("POST") && landingPattern1.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("GET") && enrollment.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("PUT") && userAttempts.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("GET") && twilioCall.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("POST") && logPattern.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("GET") && reportDownloadPattern.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("GET") && requestUrl.contains("/companies/language")) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("GET") && facebookLoginPattern.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("GET") && googleLoginPattern.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("GET") && twitterLoginPattern.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("GET") && microsoftLoginPattern.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("GET") && campaignImagePattern.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("PUT") && campaignTrackingPattern.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("PUT") && controllerLogsPattern.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("PUT")
					&& campaignButtonTrackingPattern.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if ((requestMethod.equalsIgnoreCase("GET") || requestMethod.equalsIgnoreCase("POST"))
					&& facebookWebhookPattern.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("POST")
					&& unsubscriptionToMarketingEmail.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("GET") && requestUrl.contains("/getImage/")) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("GET") && requestUrl.contains("api/tokens/admin_token")) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("GET") && samInstallerPattern.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("POST") && userEvents.matcher(requestUrl).find()) {
				isValidRequest = true;
			} else if (requestMethod.equalsIgnoreCase("POST") && controllerPostPattern.matcher(requestUrl).find()) {
				String uuid = req.getHeader("authentication_token");
				if (uuid == null) {
					uuid = queryParams.get("authentication_token");
				}
				if (auth.isValidToken(uuid, subdomain, true)) {
					isValidRequest = true;
				} else {
					isValidRequest = false;
				}
			} else if (requestMethod.equalsIgnoreCase("GET") && chatwidgetPattern.matcher(requestUrl).find()) {
				isValidRequest = true;

//				res.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
				res.setHeader("Access-Control-Allow-Methods", "GET");
				res.setHeader("Access-Control-Allow-Headers", "*");
				res.setHeader("Access-Control-Allow-Credentials", "true");
				res.setHeader("Access-Control-Max-Age", "180");
			} else if ((requestMethod.equalsIgnoreCase("POST") || requestMethod.equalsIgnoreCase("DELETE"))
					&& dnsInsertPattern.matcher(requestUrl).find()) {
				if (queryParams.containsKey("secret")) {
					String uuid = queryParams.get("secret");
					if (uuid.equals("9cada280-1700-47bd-b00e-fcf8b098f085")) {
						isValidRequest = true;
					}
				}
			} else if (requestUrl.contains("ngdesk/rest-websocket")) {
				isValidRequest = true;
			} else {

				String uuid = req.getHeader("authentication_token");
				if (uuid == null) {
					uuid = queryParams.get("authentication_token");

				}
				if (auth.isValidToken(uuid, subdomain, false)) {
					JSONObject userDetails = auth.getUserDetails(uuid);
//					if (!subdomain.equals(userDetails.getString("COMPANY_SUBDOMAIN"))) {
//						isValidRequest = false;
//					} else {
					companyId = userDetails.getString("COMPANY_ID");
					String language = userDetails.getString("LANGUAGE");
					request.setAttribute("LANGUAGE", language);
					isValidRequest = true;

					String userId = userDetails.getString("USER_ID");
					MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);
					usersCollection.updateOne(Filters.eq("_id", new ObjectId(userId)),
							Updates.set("LAST_SEEN", new Date()));

//					}
				} else {
					// NO AUTHENTICATION TOKEN NOT A VALID REQUEST
					isValidRequest = false;
				}
			}
			if (isValidRequest) {
				// EXECUTES THE REQUEST

				requestObj.put("HEADERS", headers);
				requestObj.put("COOKIES", cookies);
				requestObj.put("URL", requestUrl);
				requestObj.put("REQUEST_TYPE", requestMethod);
				requestObj.put("DATE_CREATED", new Timestamp(new Date().getTime()));
				requestObj.put("IP_ADDRESS", request.getRemoteAddr());
				requestObj.put("QUERY_PARAMS", queryParams);
				requestObj.put("COMPANY_ID", companyId);

				String collectionName = "audit_logs";

				// Retrieving a collection
				MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

				Document requestDocument = Document.parse(requestObj.toString());

//				collection.insertOne(document);
//				request.setAttribute("AUDIT_LOG_ID", document.getObjectId("_id"));

				MDC.put("subdomain", subdomain);
				MDC.put("ipAddress", request.getRemoteAddr());
				MDC.put("url", url);
				chain.doFilter(request, response);
				log.trace("Exit RequestResponseLoggingFilter.doFilter()");
				MDC.clear();
			} else {
				Map<String, String> error = new HashMap<String, String>();
				error.put("ERROR", "Unauthorized");

				((HttpServletResponse) response).setStatus(HttpStatus.UNAUTHORIZED.value());
				((HttpServletResponse) response).setContentType("application/json");
				response.getWriter().write(new ObjectMapper().writeValueAsString(error));
			}

			responseObj.put("HEADERS", headers);
			responseObj.put("COOKIES", cookies);
			responseObj.put("URL", requestUrl);
			responseObj.put("REQUEST_TYPE", requestMethod);
			responseObj.put("DATE_CREATED", new Timestamp(new Date().getTime()));
			responseObj.put("IP_ADDRESS", request.getRemoteAddr());
			responseObj.put("QUERY_PARAMS", queryParams);
			responseObj.put("COMPANY_ID", companyId);

			String collectionName = "audit_logs";

			// Retrieving a collection
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			if (request.getAttribute("AUDIT_LOG_ID") != null) {
				String auditLogId = request.getAttribute("AUDIT_LOG_ID").toString();
				Document responseDocument = Document.parse(responseObj.toString());
				if (!ObjectId.isValid(auditLogId)) {
					throw new BadRequestException("INVALID_AUDIT_LOG_ID");
				}
				Document document = collection.find(Filters.eq("_id", new ObjectId(auditLogId))).first();
				document.put("RESPONSE", responseDocument);

				collection.findOneAndReplace(Filters.eq("_id", new ObjectId(auditLogId)), document);
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}
	}

	@Override
	public void destroy() {

	}

	public JSONObject getHeaders(HttpServletRequest req) throws JSONException {
		log.trace("Enter RequestResponseLoggingFilter.getHeaders()");
		JSONObject headers = new JSONObject();

		Enumeration<String> headerNames = req.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String header = headerNames.nextElement();
			headers.put(header.toLowerCase(), req.getHeader(header));
		}
		log.trace("Exit RequestResponseLoggingFilter.getHeaders()");
		return headers;
	}

	public JSONObject setCookies(HttpServletRequest req) throws JSONException {
		log.trace("Enter RequestResponseLoggingFilter.setCookies()");
		JSONObject cookies = new JSONObject();
		Cookie[] cookiesArray = req.getCookies();
		if (cookiesArray != null) {
			for (Cookie cookie : cookiesArray) {
				cookies.put(cookie.getName(), cookie.getValue());

			}
		}
		log.trace("Exit RequestResponseLoggingFilter.setCookies()");
		return cookies;
	}

}
