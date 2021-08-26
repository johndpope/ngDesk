package com.ngdesk.ldap;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.validator.routines.UrlValidator;
import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;
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
import com.mongodb.client.model.Projections;
import com.ngdesk.Authentication;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.ForbiddenException;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.roles.RoleService;

@RestController
@Component
public class AdService {

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	RoleService roleService;

	@Autowired
	Authentication auth;

	@GetMapping("/companies/sso/ad")
	public Ad getAdSettings(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid) {
		try {
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject userDetails = auth.getUserDetails(uuid);
			String companyId = userDetails.getString("COMPANY_ID");
			String role = userDetails.getString("ROLE");

			if (!roleService.isSystemAdmin(role, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			MongoCollection<Document> adCollection = mongoTemplate.getCollection("ad");
			Document document = adCollection.find(Filters.eq("COMPANY_ID", companyId))
					.projection(Filters.and(Projections.excludeId(), Projections.exclude("COMPANY_ID"))).first();

			if (document == null) {
				return new Ad("", "", "", new ArrayList<String>());
			}

			Ad ad = new ObjectMapper().readValue(document.toJson(), Ad.class);
			return ad;

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

	@PutMapping("/companies/sso/ad")
	public Ad updateAdSettings(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid, @Valid @RequestBody Ad ad) {

		try {
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject userDetails = auth.getUserDetails(uuid);
			String companyId = userDetails.getString("COMPANY_ID");
			String role = userDetails.getString("ROLE");

			if (!roleService.isSystemAdmin(role, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			UrlValidator urlValidator = new UrlValidator();
			if (!urlValidator.isValid(ad.getSsoUrl())) {
				throw new BadRequestException("INVALID_SSO_URL");
			}

			if (ad.getRemoteLogoutUrl() != null && ad.getRemoteLogoutUrl().length() > 0
					&& !urlValidator.isValid(ad.getRemoteLogoutUrl())) {
				throw new BadRequestException("INVALID_LOGOUT_URL");
			}

			MongoCollection<Document> adCollection = mongoTemplate.getCollection("ad");
			Document document = adCollection.find(Filters.eq("COMPANY_ID", companyId)).first();

			String json = new ObjectMapper().writeValueAsString(ad);
			Document adDocument = Document.parse(json);
			adDocument.put("COMPANY_ID", companyId);

			if (document != null) {
				adCollection.findOneAndReplace(Filters.eq("COMPANY_ID", companyId), adDocument);
			} else {
				adCollection.insertOne(adDocument);
			}

			return ad;
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");

	}
}
