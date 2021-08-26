package com.ngdesk.companies.security;

import java.io.IOException;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.ngdesk.Authentication;
import com.ngdesk.exceptions.ForbiddenException;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.roles.RoleService;

@RestController
@Component
public class CompanySecurityService {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Authentication auth;

	@Autowired
	private RoleService roleService;

	private final Logger log = LoggerFactory.getLogger(CompanySecurityService.class);

	@GetMapping("/companies/security")
	public Security getCompanySecuritySettings(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid) {
		try {

			log.trace("Enter CompanySecurityService.getCompanySecuritySettings()");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");
			String userRole = user.getString("ROLE");
			String collectionName = "companies_security";
			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			if (collection.countDocuments() > 0) {
				Document securityDocument = collection.find(Filters.eq("COMPANY_ID", companyId)).first();
				securityDocument.remove("_id");
				Security security = new ObjectMapper().readValue(securityDocument.toJson(), Security.class);
				log.trace("Exit CompanySecurityService.getCompanySecuritySettings()");
				return security;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PutMapping("/companies/security")
	public Security postCompanySecuritySettings(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@Valid @RequestBody Security security) {
		try {

			log.trace("Enter CompanySecurityService.getCompanySecuritySettings()");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");
			String userRole = user.getString("ROLE");
			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}
			MongoCollection<Document> collection = mongoTemplate.getCollection("companies_security");
			collection.updateOne(Filters.eq("COMPANY_ID", companyId),
					Updates.combine(Updates.set("MAX_LOGIN_RETRIES", security.getMaxLoginRetries()),
							Updates.set("ENABLE_SIGNUPS", security.isEnableSignups())));

			log.trace("Exit CompanySecurityService.postCompanySecuritySettings()");
			return security;

		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	public void putCompanySecuritySettings(String companyId, Security security) {
		try {
			String collectionName = "companies_security";
			security.setCompanyId(companyId);
			String payload = new ObjectMapper().writeValueAsString(security);
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			Document document = Document.parse(payload);
			collection.insertOne(document);
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

}
