package com.ngdesk.auth.dao;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.auth.company.dao.Company;
import com.ngdesk.commons.mail.SendMail;
import com.ngdesk.commons.models.User;
import com.ngdesk.repositories.CompanyRepository;
import com.ngdesk.repositories.ModuleEntryRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;

@RestController
public class AuthService {

	@Value("${jwt.secret}")
	private String jwtSecret;

	@Autowired
	ModuleEntryRepository entryRepository;

	@Autowired
	CompanyRepository companyRepository;

	public boolean subscriptionActive;

	@Autowired
	AuthService authService;

	@Autowired
	SendMail sendMail;

	@PostConstruct
	public void init() {
		authService.subscriptionActive = true;
	}

	public String getEnvironmentVariable(String fileName) {
		return System.getenv(fileName);
	}

	@GetMapping("/user/details")
	public User getUserDetails(@RequestHeader(value = "authentication_token") String uuid) {

		String environmentVariable = getEnvironmentVariable("NGDESK_PREMISE");
		if (environmentVariable != null && environmentVariable.equals("on-premise")) {
			if (!authService.subscriptionActive) {
				return null;
			}
		}

		return retrieveUserDetails(uuid);
	}

	@GetMapping("/user/details/internal")
	public User getUserDetailsForInternalCalls(@RequestParam("user_uuid") String userUuid,
			@RequestParam("company_id") String companyId) {
		String environmentVariable = getEnvironmentVariable("NGDESK_PREMISE");
		if (environmentVariable != null && environmentVariable.equals("on-premise")) {
			if (!authService.subscriptionActive) {
				return null;
			}
		}
		return getUserDetails(userUuid, companyId);
	}

	public User retrieveUserDetails(String uuid) {
		try {
			Claims body = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(uuid).getBody();
			Map<String, Object> userMap = new ObjectMapper().readValue(body.get("USER").toString(), Map.class);

			User user = new User(userMap.get("EMAIL_ADDRESS").toString(), userMap.get("FIRST_NAME").toString(),
					userMap.get("LAST_NAME").toString(), userMap.get("ROLE").toString(),
					userMap.get("EMAIL_ADDRESS").toString(), body.get("COMPANY_ID").toString(),
					body.get("SUBDOMAIN").toString(), userMap.get("DATA_ID").toString(),
					userMap.get("LANGUAGE").toString(), userMap.get("USER_UUID").toString(), null);

			Map<String, Object> attributes = new HashMap<String, Object>();

			String[] keys = { "EMAIL_ADDRESS", "FIRST_NAME", "LAST_NAME", "ROLE", "USER_UUID", "LANGUAGE", "DATA_ID" };
			List<String> usedKeys = Arrays.asList(keys);

			for (String key : userMap.keySet()) {
				if (!usedKeys.contains(key)) {
					attributes.put(key, userMap.get(key));
				}
			}
			user.setAttributes(attributes);
			return user;
		} catch (MalformedJwtException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public User getUserDetails(String userUuid, String companyId) {

		try {
			Optional<Company> optionalCompany = companyRepository.findById(companyId, "companies");
			if (optionalCompany.isEmpty()) {
				return null;
			}

			Optional<Map<String, Object>> optionalUser = entryRepository.findUserById(userUuid, "Users_" + companyId);
			if (optionalUser.isEmpty()) {
				return null;
			}

			Map<String, Object> userMap = optionalUser.get();
			Company company = optionalCompany.get();

			Optional<Map<String, Object>> optionalContact = entryRepository.findById(userMap.get("CONTACT").toString(),
					"Contacts_" + companyId);
			Map<String, Object> contact = optionalContact.get();

			User user = new User(userMap.get("EMAIL_ADDRESS").toString(), contact.get("FIRST_NAME").toString(),
					contact.get("LAST_NAME").toString(), userMap.get("ROLE").toString(),
					userMap.get("EMAIL_ADDRESS").toString(), company.getCompanyId(), company.getCompanySubdomain(),
					userMap.get("_id").toString(), userMap.get("LANGUAGE").toString(),
					userMap.get("USER_UUID").toString(), null);

			String[] keys = { "EMAIL_ADDRESS", "FIRST_NAME", "LAST_NAME", "ROLE", "USER_UUID", "LANGUAGE", "DATA_ID" };
			List<String> usedKeys = Arrays.asList(keys);

			Map<String, Object> attributes = new HashMap<String, Object>();
			for (String key : userMap.keySet()) {
				if (!usedKeys.contains(key)) {
					attributes.put(key, userMap.get(key));
				}
			}
			user.setAttributes(attributes);

			return user;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
