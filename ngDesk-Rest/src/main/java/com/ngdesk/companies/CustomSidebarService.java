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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
import com.ngdesk.channels.chat.ChatService;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.ForbiddenException;
import com.ngdesk.exceptions.InternalErrorException;

@RestController
@Component
public class CustomSidebarService {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Global global;

	@Autowired
	private Authentication auth;

	private final Logger log = LoggerFactory.getLogger(CustomSidebarService.class);

	@GetMapping("/companies/sidebar")
	public Sidebar getCustomSidebar(HttpServletRequest request, @RequestParam("authentication_token") String uuid) {

		try {
			log.trace("Enter CustomSidebarService.getCustomSidebar()");
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");

			// Accessing the database
			String collectionName = "companies_sidebar";

			// Retrieving a collection
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			Document sidebarDocument = collection.find(Filters.eq("COMPANY_ID", companyId)).first();
			if (sidebarDocument == null) {
				throw new BadRequestException("SIDEBAR_DOES_NOT_EXIST");
			}
			Document companySidebarDocument = (Document) sidebarDocument.get("SIDE_BAR");
			Sidebar sidebar = new ObjectMapper().readValue(companySidebarDocument.toJson(), Sidebar.class);

			log.trace("Exit CustomSidebarService.getCustomSidebar()");
			return sidebar;

		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@GetMapping("/companies/sidebar/role")
	public Menu getCustomSidebarByRole(HttpServletRequest request, @RequestParam("authentication_token") String uuid) {

		try {
			log.trace("Enter CustomSidebarService.getCustomSidebar()");
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");
			String role = user.getString("ROLE");

			// Accessing the database
			String collectionName = "companies_sidebar";

			// Retrieving a collection
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			Document sidebarDocument = collection.find(Filters.eq("COMPANY_ID", companyId)).first();
			if (sidebarDocument == null) {
				throw new BadRequestException("SIDEBAR_DOES_NOT_EXIST");
			}
			Document companySidebarDocument = (Document) sidebarDocument.get("SIDE_BAR");
			Sidebar sidebar = new ObjectMapper().readValue(companySidebarDocument.toJson(), Sidebar.class);

			for (Menu menu : sidebar.getSidebarMenu()) {
				if (menu.getRole().equals(role)) {
					return menu;
				}
			}
			log.trace("Exit CustomSidebarService.getCustomSidebar()");
			return new Menu();

		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PutMapping("/companies/sidebar")
	public Sidebar putCustomSidebar(HttpServletRequest request, @RequestParam("authentication_token") String uuid,
			@Valid @RequestBody Sidebar sidebar) {

		try {
			log.trace("Enter CustomSidebarService.putCustomSidebar()");
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");
			String collectionName = "companies_sidebar";
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			Document document = collection.find(Filters.eq("COMPANY_ID", companyId)).first();
			if (document == null)
				throw new BadRequestException("SIDEBAR_DOES_NOT_EXIST");

			for (MenuItem menuItem : sidebar.getSidebarMenu().get(0).getMenuItems()) {
				if (menuItem.isIsmodule()) {
					String moduleId = menuItem.getModule();
					if (!global.isDocumentIdExists(moduleId, "modules_" + companyId)) {
						throw new BadRequestException("MODULE_NOT_EXIST");
					}
				}
			}

			String sidebarJson = new ObjectMapper().writeValueAsString(sidebar);
			Document sidebarDocument = Document.parse(sidebarJson);
			collection.updateOne(Filters.eq("COMPANY_ID", companyId), Updates.set("SIDE_BAR", sidebarDocument));
			log.trace("Exit CustomSidebarService.putCustomSidebar()");
			return sidebar;

		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}
}
