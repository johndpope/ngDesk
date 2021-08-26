package com.ngdesk.companies;

import java.io.IOException;

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
import com.ngdesk.Global;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.ForbiddenException;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.roles.RoleService;

@RestController
@Component
public class ColorPickerService {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private RoleService roleService;

	@Autowired
	Global global;

	@Autowired
	private Authentication auth;

	private final Logger log = LoggerFactory.getLogger(ColorPickerService.class);

	@GetMapping("/companies/themes")
	public ColorPicker getThemes(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid) {

		try {
			log.trace("Enter ColorPickerService.getThemes()");
			String collectionName = "companies";
			String subdomain = request.getAttribute("SUBDOMAIN").toString();

			// Retrieving a collection
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			// Get Document
			Document document = collection.find(Filters.eq("COMPANY_SUBDOMAIN", subdomain)).first();

			if (document == null) {
				throw new BadRequestException("SUBDOMAIN_NOT_EXIST");
			}

			Document themeDocument = (Document) document.get("THEMES");
			ColorPicker colorPicker = new ObjectMapper().readValue(themeDocument.toJson(), ColorPicker.class);
			log.trace("Exit ColorPickerService.getThemes()");
			return colorPicker;
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		log.trace("Exit ColorPickerService.getThemes()");
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PutMapping("/companies/themes")
	public ColorPicker putThemes(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@Valid @RequestBody ColorPicker colorPicker) {
		try {
			log.trace("Enter ColorPickerService.putThemes()");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject userDetails = auth.getUserDetails(uuid);

			String companyId = userDetails.getString("COMPANY_ID");
			String role = userDetails.getString("ROLE");

			if (!roleService.isSystemAdmin(role, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			// Retrieving a collection
			MongoCollection<Document> collection = mongoTemplate.getCollection("companies");
			Document company = collection.find(Filters.eq("_id", new ObjectId(companyId))).first();

			if (company == null) {
				throw new BadRequestException("SUBDOMAIN_NOT_EXIST");
			}

			String json = new ObjectMapper().writeValueAsString(colorPicker);
			Document themes = Document.parse(json);
			String primaryColor = themes.getString("PRIMARY_COLOR");
			String secondaryColor = themes.getString("SECONDARY_COLOR");
			if (global.validatePrimaryColor(primaryColor) && global.validateSecondaryColor(secondaryColor)) {
				collection.updateOne(Filters.eq("_id", new ObjectId(companyId)), Updates.set("THEMES", themes));
			}

			log.trace("Exit ColorPickerService.putThemes()");
			return colorPicker;
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		log.trace("Exit ColorPickerService.putThemes()");
		throw new InternalErrorException("INTERNAL_ERROR");
	}

}
