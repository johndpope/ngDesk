package com.ngdesk.modules.settings;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
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
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.ForbiddenException;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.roles.RoleService;

@RestController
@Component
public class ModuleSettingsService {

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	Authentication auth;

	@Autowired
	RoleService roleService;

	@GetMapping("/modules/{module_id}/settings")
	public ModuleSettings getSettings(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId) {
		try {
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject userDetails = auth.getUserDetails(uuid);
			String companyId = userDetails.getString("COMPANY_ID");
			String userRole = userDetails.getString("ROLE");

			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);

			if (!new ObjectId().isValid(moduleId)) {
				throw new BadRequestException("MODULE_NOT_EXISTS");
			}

			Document module = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
			if (module == null) {
				throw new BadRequestException("MODULE_NOT_EXISTS");
			}

			if (!module.getString("NAME").equals("Chat")) {
				throw new ForbiddenException("FORBIDDEN");
			}

			Document settings = (Document) module.get("SETTINGS");

			ModuleSettings moduleSettings = new ObjectMapper().readValue(settings.toJson(), ModuleSettings.class);
			return moduleSettings;

		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PutMapping("/modules/{module_id}/settings")
	public ModuleSettings updateModuleSettings(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId, @Valid @RequestBody ModuleSettings settings) {

		try {
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject userDetails = auth.getUserDetails(uuid);
			String companyId = userDetails.getString("COMPANY_ID");
			String userRole = userDetails.getString("ROLE");

			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);

			if (!new ObjectId().isValid(moduleId)) {
				throw new BadRequestException("MODULE_NOT_EXISTS");
			}

			Document module = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
			if (module == null) {
				throw new BadRequestException("MODULE_NOT_EXISTS");
			}

			if (!module.getString("NAME").equals("Chat")) {
				throw new ForbiddenException("FORBIDDEN");
			}

			Document settingsDocument = Document.parse(new ObjectMapper().writeValueAsString(settings));
			modulesCollection.updateOne(Filters.eq("_id", new ObjectId(moduleId)),
					Updates.set("SETTINGS", settingsDocument));

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}
}
