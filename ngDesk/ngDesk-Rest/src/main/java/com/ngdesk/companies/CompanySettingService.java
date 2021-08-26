package com.ngdesk.companies;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.ngdesk.Authentication;
import com.ngdesk.Global;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.modules.ModuleService;

@RestController
@Component
public class CompanySettingService {
	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Global global;

	@Autowired
	private Authentication auth;

	private final Logger log = LoggerFactory.getLogger(CompanySettingService.class);

	@GetMapping("/companies/settings")
	public Settings getCompanySettings(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid) {
		try {

			log.trace("Enter CompanySettingService.getCompanySettings()");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject user = auth.getUserDetails(uuid);
			String subdomain = user.getString("COMPANY_SUBDOMAIN");
			String collectionName = "companies";

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			Document companyDocument = collection.find(Filters.eq("COMPANY_SUBDOMAIN", subdomain)).first();
			if (companyDocument == null) {
				throw new BadRequestException("COMPANY_DOES_NOT_EXIST");
			}
			Settings setting = new Settings();
			setting.setEnableSignups(companyDocument.getBoolean("ENABLE_SIGNUPS"));
			setting.setEnableGoogleSignups(companyDocument.getBoolean("ENABLE_GOOGLE_SIGNUPS"));
			setting.setMaxLoginRetries(companyDocument.getInteger("MAX_LOGIN_RETRIES"));
			log.trace("Exit CompanySettingService.getCompanySettings()");
			return setting;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PutMapping("/companies/settings")
	public ResponseEntity<Object> postCompanySettings(HttpServletRequest request,
			@RequestParam("authentication_token") String uuid, @Valid @RequestBody Settings setting) {
		try {

			log.trace("Enter CompanySettingService.postCompanySettings()");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject user = auth.getUserDetails(uuid);
			String subdomain = user.getString("COMPANY_SUBDOMAIN");
			String collectionName = "companies";
			String payload = new ObjectMapper().writeValueAsString(setting).toString();

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			Document companySettingDocument = Document.parse(payload);

			collection.updateOne(Filters.eq("COMPANY_SUBDOMAIN", subdomain),
					Updates.set("ENABLE_SIGNUPS", companySettingDocument.get("ENABLE_SIGNUPS")));
			collection.updateOne(Filters.eq("COMPANY_SUBDOMAIN", subdomain),
					Updates.set("ENABLE_GOOGLE_SIGNUPS", companySettingDocument.get("ENABLE_GOOGLE_SIGNUPS")));
			collection.updateOne(Filters.eq("COMPANY_SUBDOMAIN", subdomain),
					Updates.set("MAX_LOGIN_RETRIES", companySettingDocument.get("MAX_LOGIN_RETRIES")));

			log.trace("Exit CompanySettingService.postCompanySettings()");
			return new ResponseEntity<Object>(HttpStatus.OK);

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
