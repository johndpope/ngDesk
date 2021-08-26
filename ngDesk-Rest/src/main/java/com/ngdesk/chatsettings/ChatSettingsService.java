package com.ngdesk.chatsettings;

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

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.ngdesk.Authentication;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.ForbiddenException;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.roles.RoleService;

@Component
@RestController
public class ChatSettingsService {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Authentication auth;

	@Autowired
	RoleService roleService;

	private final Logger log = LoggerFactory.getLogger(ChatSettingsService.class);

	@GetMapping("/companies/chat/general")
	public ChatSettings getMaxChatPerAgent(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid) {

		try {
			
			MongoCollection<Document> collection = mongoTemplate.getCollection("companies");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");
			String userRoleId = user.getString("ROLE");

			// CHECKING IF SYSTEM ADMIN OR NO
			if (!roleService.isSystemAdmin(userRoleId, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			Document company = collection.find(Filters.eq("_id", new ObjectId(companyId))).first();

			if (company == null) {
				throw new BadRequestException("COMPANY_INVALID");
			}

			int value = company.getInteger("MAX_CHATS_PER_AGENT");

			ChatSettings settings = new ChatSettings(value);
			return settings;

		} catch (JSONException e) {
			
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PutMapping("/companies/chat/general")
	public ChatSettings updateMaxChatPerAgent(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@Valid @RequestBody ChatSettings data) {

		try {
			MongoCollection<Document> collection = mongoTemplate.getCollection("companies");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");
			String userRoleId = user.getString("ROLE");

			if (!roleService.isSystemAdmin(userRoleId, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			Document company = collection.find(Filters.eq("_id", new ObjectId(companyId))).first();

			if (company == null) {
				throw new BadRequestException("COMPANY_INVALID");
			}

			int value = data.getMaxChatsPerAgent();
			collection.findOneAndUpdate(Filters.eq("_id", new ObjectId(companyId)),
					Updates.set("MAX_CHATS_PER_AGENT", value));

			return data;
		} catch (JSONException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}
}
