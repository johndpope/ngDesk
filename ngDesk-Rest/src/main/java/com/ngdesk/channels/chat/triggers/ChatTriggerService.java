package com.ngdesk.channels.chat.triggers;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
@RequestMapping("/channels")
public class ChatTriggerService {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Authentication auth;

	@Autowired
	Global global;

	@Autowired
	private RoleService roleService;

	private static String channelType = "chat";

	private final Logger log = LoggerFactory.getLogger(ChatTriggerService.class);

	@GetMapping("chat/{chat_name}/triggers")
	public ResponseEntity<Object> getTriggers(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@RequestParam(value = "page_size", required = false) String pageSize,
			@RequestParam(value = "page", required = false) String page,
			@RequestParam(value = "sort", required = false) String sort,
			@RequestParam(value = "order", required = false) String order, @PathVariable("chat_name") String chatName) {

		JSONArray triggers = new JSONArray();
		int totalSize = 0;
		JSONObject resultObj = new JSONObject();

		try {
			log.trace("Enter ChatTriggerService.getTriggers(), ChatName: " + chatName);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");

			String collectionName = "channels_" + channelType + "_" + companyId;
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}
			Document chatChannel = collection.find(Filters.eq("NAME", chatName)).first();

			if (chatChannel.containsKey("CHAT_TRIGGERS")) {
				ArrayList<Document> triggerDocuments = (ArrayList) chatChannel.get("CHAT_TRIGGERS");
				totalSize = triggerDocuments.size();

				// by default return all documents
				int lowerLimit = 0;
				int maxLimit = triggerDocuments.size();

				if (pageSize != null && page != null) {
					int pgSize = Integer.valueOf(pageSize);
					int pg = Integer.valueOf(page);

					if (pgSize < 1) {
						throw new BadRequestException("INVALID_PAGESIZE");
					} else if (pg < 0) {
						throw new BadRequestException("INVALID_PAGE_NUMBER");
					} else if (maxLimit >= pgSize) {
						maxLimit = pgSize * pg;
						lowerLimit = maxLimit - pgSize;
					}
				}

				for (int i = lowerLimit; i < maxLimit; i++) {
					if (i == totalSize) {
						break;
					}
					Document document = triggerDocuments.get(i);
					JSONObject trigger = new JSONObject(document.toJson().toString());
					triggers.put(trigger);
				}
			}

			resultObj.put("CHAT_TRIGGERS", triggers);
			resultObj.put("TOTAL_RECORDS", totalSize);
			log.trace("Exit ChatTriggerService.getTriggers(), ChatName: " + chatName);
			return new ResponseEntity<>(resultObj.toString(), global.postHeaders, HttpStatus.OK);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@GetMapping("chat/{chat_name}/triggers/{trigger_name}")
	public ChatTrigger getTrigger(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("chat_name") String chatName, @PathVariable("trigger_name") String triggerName) {

		try {
			log.trace("Enter ChatTriggerService.getTrigger(), TriggerName: " + triggerName);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");

			String collectionName = "channels_" + channelType + "_" + companyId;
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}
			Document chatChannel = collection
					.find(Filters.and(Filters.eq("NAME", chatName), Filters.eq("CHAT_TRIGGERS.NAME", triggerName)))
					.first();
			if (chatChannel != null) {
				ArrayList<Document> triggerDocuments = (ArrayList) chatChannel.get("CHAT_TRIGGERS");
				for (Document document : triggerDocuments) {
					if (document.get("NAME").equals(triggerName)) {
						ChatTrigger trigger = new ObjectMapper().readValue(document.toJson(), ChatTrigger.class);
						log.trace("Exit ChatTriggerService.getTrigger(), TriggerName: " + triggerName);
						return trigger;
					}
				}
			} else {
				throw new ForbiddenException("TRIGGER_DOES_NOT_EXIST");
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

	@PostMapping("chat/{chat_name}/triggers/{trigger_name}")
	public ChatTrigger createTrigger(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("chat_name") String chatName, @PathVariable("trigger_name") String triggerName,
			@Valid @RequestBody ChatTrigger trigger) {

		try {
			log.trace("Enter ChatTriggerService.createTrigger(), TriggerName: " + triggerName);

			trigger.setDateCreated(new Timestamp(new Date().getTime()));
			trigger.setDateUpdated(new Timestamp(new Date().getTime()));
			trigger.setTriggerId(UUID.randomUUID().toString());

			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");

			String triggerJson = new ObjectMapper().writeValueAsString(trigger);
			String collectionName = "channels_" + channelType + "_" + companyId;

			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			Document existingDocument = collection
					.find(Filters.and(Filters.eq("NAME", chatName), Filters.eq("CHAT_TRIGGERS.NAME", triggerName)))
					.first();

			if (existingDocument == null) {

				Document triggerDocument = Document.parse(triggerJson);
				collection.findOneAndUpdate(Filters.eq("NAME", chatName),
						Updates.addToSet("CHAT_TRIGGERS", triggerDocument));
				log.trace("Exit ChatTriggerService.createTrigger(), TriggerName: " + triggerName);
				return trigger;

			} else {
				throw new BadRequestException("TRIGGER_EXISTS");
			}

		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PutMapping("chat/{chat_name}/triggers/{trigger_name}")
	public ChatTrigger updateTrigger(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("chat_name") String chatName, @PathVariable("trigger_name") String triggerName,
			@Valid @RequestBody ChatTrigger trigger) {

		try {
			log.trace("Enter ChatTriggerService.updateTrigger(), TriggerName: " + triggerName);

			if (trigger.getTriggerId() != null) {
				if (request.getHeader("authentication_token") != null) {
					uuid = request.getHeader("authentication_token").toString();
				}
				JSONObject user = auth.getUserDetails(uuid);
				String userId = user.getString("USER_ID");
				String userRole = user.getString("ROLE");
				String companyId = user.getString("COMPANY_ID");

				String collectionName = "channels_" + channelType + "_" + companyId;

				if (!roleService.isSystemAdmin(userRole, companyId)) {
					throw new ForbiddenException("FORBIDDEN");
				}

				MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
				Document existingDocument = collection.find(Filters.and(Filters.eq("NAME", chatName),
						Filters.eq("CHAT_TRIGGERS.TRIGGER_ID", trigger.getTriggerId()))).first();

				if (existingDocument != null) {
					trigger.setDateUpdated(new Timestamp(new Date().getTime()));
					trigger.setLastUpdated(userId);
					String triggerJson = new ObjectMapper().writeValueAsString(trigger);
					Document triggerDocument = Document.parse(triggerJson);
					collection.updateOne(Filters.eq("NAME", chatName),
							Updates.pull("CHAT_TRIGGERS", Filters.eq("TRIGGER_ID", trigger.getTriggerId())));
					collection.updateOne(Filters.eq("NAME", chatName), Updates.push("CHAT_TRIGGERS", triggerDocument));

					log.trace("Exit ChatTriggerService.updateTrigger(), TriggerName: " + triggerName);
					return trigger;
				} else {
					throw new ForbiddenException("TRIGGER_DOES_NOT_EXIST");
				}

			} else {
				throw new BadRequestException("TRIGGER_ID_NULL");
			}

		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@DeleteMapping("chat/{chat_name}/triggers/{trigger_name}")
	public ResponseEntity<Object> deleteTrigger(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("chat_name") String chatName, @PathVariable("trigger_name") String triggerName) {

		try {
			log.trace("Enter ChatTriggerService.deleteTrigger(), TriggerName: " + triggerName);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userRole = user.getString("ROLE");
			String userId = user.getString("USER_ID");
			String companyId = user.getString("COMPANY_ID");

			String collectionName = "channels_" + channelType + "_" + companyId;
			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			Document existingDocument = collection
					.find(Filters.and(Filters.eq("NAME", chatName), Filters.eq("CHAT_TRIGGERS.NAME", triggerName)))
					.first();

			if (existingDocument != null) {
				collection.updateOne(Filters.eq("NAME", chatName),
						Updates.pull("CHAT_TRIGGERS", Filters.eq("NAME", triggerName)));
				log.trace("Exit ChatTriggerService.deleteTrigger(), TriggerName: " + triggerName);
				return new ResponseEntity<>(HttpStatus.OK);
			} else {
				throw new ForbiddenException("TRIGGER_DOES_NOT_EXIST");
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}
}
