package com.ngdesk.sam.controllers.dao;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.CompanyRepository;
import com.ngdesk.repositories.RolesRepository;
import com.ngdesk.repositories.UserRepository;
import com.ngdesk.sam.company.dao.Company;
import com.ngdesk.sam.controllers.user.dao.User;
import com.ngdesk.sam.roles.dao.Role;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class ControllerService {

	@Autowired
	private AuthManager authManager;

	@Autowired
	private RolesRepository rolesRepository;

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private CompanyRepository companyRepository;

	@Value("${jwt.secret}")
	private String jwtSecret;


	public String generateInfiniteJwtToken(String email) {

		Optional<User> optional = userRepository.findByUserEmail(email,
				"Users_" + authManager.getUserDetails().getCompanyId());

		String user = null;
		try {
			user = new ObjectMapper().writeValueAsString(optional.get());
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		Claims claims = Jwts.claims().setSubject(email);
		claims.setIssuedAt(new Date(new Date().getTime()));

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.YEAR, 1000);
		claims.setExpiration(new Timestamp(calendar.getTimeInMillis()));
		
		Optional<Company> optionalCompany = companyRepository.findById(authManager.getUserDetails().getCompanyId(), "companies");
		if (optionalCompany.isEmpty()) {
			return null;
		}
		Company company = optionalCompany.get();
		claims.put("USER", user);
		claims.put("COMPANY_UUID", company.getCompanyUuid());
		claims.put("SUBDOMAIN", company.getCompanySubdomain());
		claims.put("COMPANY_ID", authManager.getUserDetails().getCompanyId());

		return Jwts.builder().setClaims(claims).signWith(SignatureAlgorithm.HS512, jwtSecret).compact();
	}

	public boolean isAuthorised() {
		Optional<Role> role = rolesRepository.findById(authManager.getUserDetails().getRole(),
				"roles_" + authManager.getUserDetails().getCompanyId());
		return role.get().getName().equals("LimitedUser") || role.get().getName().equals("ExternalProbe")
				|| role.get().getName().equals("SystemAdmin");
	}
}
