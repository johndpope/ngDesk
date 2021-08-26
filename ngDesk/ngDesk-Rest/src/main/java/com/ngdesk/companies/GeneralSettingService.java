package com.ngdesk.companies;

import javax.servlet.http.HttpServletRequest;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.ngdesk.Authentication;
import com.ngdesk.Global;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.ForbiddenException;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.roles.RoleService;

@RestController
public class GeneralSettingService {
	@Autowired
	private Authentication auth;

	@Autowired
	RoleService role;

	private final Logger log = LoggerFactory.getLogger(GeneralSettingService.class);

	@Autowired
	MongoTemplate mongoTemplate;

	@GetMapping("/companies/general")
	public GeneralSettings getCompanyGeneralSettings(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid) {
		try {
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");

			if (!role.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}
			GeneralSettings settings = new GeneralSettings();
			MongoCollection<Document> collection = mongoTemplate.getCollection("companies");

			Document company = collection.find(Filters.eq("_id", new ObjectId(companyId))).first();
			if (company != null) {
				settings.setLanguage(company.getString("LANGUAGE"));
				settings.setTimezone(company.getString("TIMEZONE"));
				settings.setLocale(company.getString("LOCALE"));
			}
			return settings;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PutMapping("/companies/general")
	public ResponseEntity<Object> putGeneralSettings(HttpServletRequest request,
			@RequestParam(value="authentication_token", required = false) String uuid, @RequestBody GeneralSettings settings) {
		try {
			log.trace("Enter GeneralSettingService.putGeneralSettings()");
			
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");

			if (!role.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}
			if (!Global.languages.contains(settings.getLanguage())) {
				throw new BadRequestException("INVALID_LANGUAGE");
			}
			if (!Global.timezones.contains(settings.getTimezone())) {
				throw new BadRequestException("TIMEZONE_INVALID");
			}
			if (!Global.locale.contains(settings.getLocale())) {
				throw new BadRequestException("INVALID_LOCALE");
			}
			
			MongoCollection<Document> collection = mongoTemplate.getCollection("companies");
			collection.findOneAndUpdate(Filters.eq("_id", new ObjectId(companyId)), Updates.combine(
					Updates.set("LANGUAGE", settings.getLanguage()), Updates.set("TIMEZONE", settings.getTimezone()),Updates.set("LOCALE", settings.getLocale())));

			return new ResponseEntity<Object>(HttpStatus.OK);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");

	}
}
