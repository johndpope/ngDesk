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
import com.ngdesk.exceptions.ForbiddenException;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.roles.RoleService;

@RestController
public class KnowledgeBaseGeneralSettingsService {
	@Autowired
	private Authentication auth;

	@Autowired
	RoleService role;

	private final Logger log = LoggerFactory.getLogger(KnowledgeBaseGeneralSettingsService.class);

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	Global global;

	@GetMapping("/companies/knowledgebase/general")
	public KnowledgeBaseGeneralSettings getCompanyKnowledgeBaseGeneralSettings(HttpServletRequest request) {
		try {
			log.trace("Enter KnowledgeBaseGeneralSettings.getCompanyKnowledgeBaseGeneralSettings()");

			String companySubdomain = request.getAttribute("SUBDOMAIN").toString();
			Document company = global.getCompanyFromSubdomain(companySubdomain);

			if (company == null) {
				throw new ForbiddenException("COMPANY_DOES_NOT_EXIST");
			}
			String companyId = company.getObjectId("_id").toString();

			KnowledgeBaseGeneralSettings settings = new KnowledgeBaseGeneralSettings();
			if (company.containsKey("ENABLE_DOCS")) {
				settings.setEnableDocs(company.getBoolean("ENABLE_DOCS"));
			} else {
				settings.setEnableDocs(false);
			}
			log.trace("Exit KnowledgeBaseGeneralSettings.getCompanyKnowledgeBaseGeneralSettings()");
			return settings;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PutMapping("/companies/knowledgebase/general")
	public ResponseEntity<Object> putCompanyKnowledgeBaseGeneralSettings(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@RequestBody KnowledgeBaseGeneralSettings settings) {
		try {
			log.trace("Exit KnowledgeBaseGeneralSettings.putCompanyKnowledgeBaseGeneralSettings()");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");

			if (!role.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			MongoCollection<Document> collection = mongoTemplate.getCollection("companies");
			collection.findOneAndUpdate(Filters.eq("_id", new ObjectId(companyId)),
					Updates.set("ENABLE_DOCS", settings.isEnableDocs()));
			log.trace("Exit KnowledgeBaseGeneralSettings.putCompanyKnowledgeBaseGeneralSettings()");
			return new ResponseEntity<Object>(HttpStatus.OK);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");

	}
}
