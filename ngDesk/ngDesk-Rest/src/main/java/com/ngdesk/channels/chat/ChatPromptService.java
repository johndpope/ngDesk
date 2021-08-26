package com.ngdesk.channels.chat;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.bson.Document;
import org.bson.json.JsonParseException;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ngdesk.workflow.Workflow;
import com.twilio.twiml.voice.Prompt;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.ngdesk.Authentication;
import com.ngdesk.Global;
import com.ngdesk.channels.chat.triggers.ChatTrigger;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.ForbiddenException;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.roles.RoleService;
import com.ngdesk.schedules.Restriction;

@RestController
@Component
public class ChatPromptService {

	@Autowired
	private Authentication auth;

	@Autowired
	RoleService role;

	@Autowired
	private Global global;

	@Autowired
	MongoTemplate mongoTemplate;

	private final Logger log = LoggerFactory.getLogger(ChatPromptService.class);

	@GetMapping("/channels/chat/{name}/prompt")
	public ResponseEntity<Object> getChatPrompts(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("name") String chatChannelName) {
		JSONArray prompts = new JSONArray();
		int totalSize = 0;
		JSONObject resultObj = new JSONObject();
		try {
			log.trace("Enter ChatPrompt.getChatPrompts()");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");

			if (!role.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			String collectionName = "channels_chat_" + companyId;
			MongoCollection<Document> collection = mongoTemplate.getCollection("channels_chat_" + companyId);

			if (global.isExists("NAME", chatChannelName, collectionName)) {
				Document chatChannel = collection.find(Filters.eq("NAME", chatChannelName)).first();
				if (chatChannel.containsKey("CHAT_PROMPTS")) {
					ArrayList<Document> promptDocuments = (ArrayList) chatChannel.get("CHAT_PROMPTS");
					totalSize = promptDocuments.size();
					for (int i = 0; i < totalSize; i++) {

						Document document = promptDocuments.get(i);
						JSONObject prompt = new JSONObject(document.toJson().toString());
						prompts.put(prompt);

					}
				} else {
					throw new ForbiddenException("CHANNEL_DOES_NOT_EXIST");
				}

				resultObj.put("CHAT_PROMPTS", prompts);
				resultObj.put("TOTAL_RECORDS", totalSize);
				log.trace("Exit ChatPrompt.getChatPrompts()");

				return new ResponseEntity<>(resultObj.toString(), HttpStatus.OK);
			} else {
				ChatPrompt chatPrompt = new ChatPrompt();
				return new ResponseEntity<>(chatPrompt, HttpStatus.OK);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@GetMapping("/channels/chat/{name}/prompt/{promptId}")
	public ChatPrompt getChatPrompt(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("promptId") String promptId, @PathVariable("name") String chatChannelName) {
		try {
			log.trace("Enter ChatPrompt.getChatPrompt()");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");

			if (!role.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			String collectionName = "channels_chat_" + companyId;
			MongoCollection<Document> collection = mongoTemplate.getCollection("channels_chat_" + companyId);
			List<ChatPrompt> chatprompts = new ArrayList<ChatPrompt>();

			if (global.isExists("NAME", chatChannelName, collectionName)) {
				Document chatChannel = collection.find(Filters.eq("NAME", chatChannelName)).first();
				if (chatChannel.containsKey("CHAT_PROMPTS")) {
					ArrayList<Document> promptsDocuments = (ArrayList) chatChannel.get("CHAT_PROMPTS");
					for (Document document : promptsDocuments) {
						if (document.get("PROMPT_ID").equals(promptId)) {
							ChatPrompt prompt = new ObjectMapper().readValue(document.toJson(), ChatPrompt.class);

							return prompt;

						}
					}
				} else {
					throw new ForbiddenException("CHANNEL_DOES_NOT_EXIST");
				}

			} else {
				ChatPrompt chatPrompt = new ChatPrompt();
				return chatPrompt;
			}
			log.trace("Exit ChatPrompt.getChatPrompt()");
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

	@PostMapping("/channels/chat/{name}/prompt")
	public @Valid ChatPrompt postChatPrompt(HttpServletRequest request,
			@RequestParam("authentication_token") String uuid, @PathVariable("name") String chatChannelName,
			@Valid @RequestBody ChatPrompt chatPrompt) {
		try {
			log.trace("Enter ChatPrompt.postChatPrompt()");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userRole = user.getString("ROLE");
			String userId = user.getString("USER_ID");
			String companyId = user.getString("COMPANY_ID");

			if (!role.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			String collectionName = "channels_chat_" + companyId;
			MongoCollection<Document> collection = mongoTemplate.getCollection("channels_chat_" + companyId);

			chatPrompt.setPromptId(UUID.randomUUID().toString());

			List<Conditions> conditions = chatPrompt.getConditions();
			for (Conditions condition : conditions) {
				if (condition.getCondition().equals("STILL_ON_PAGE")
						|| condition.getCondition().equals("STILL_ON_SITE")) {
					if (condition.getOpearator() != null) {
						throw new BadRequestException("INVALID_OPERATOR");
					}
				} else if (condition.getOpearator() == null) {
					throw new BadRequestException("OPERATOR_NOT_NULL");
				} else if (!Global.validChatPromptOperators.contains(condition.getOpearator())) {
					throw new BadRequestException("INVALID_OPERATOR");
				}

			}

			if (global.isExists("NAME", chatChannelName, collectionName)) {
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
				chatPrompt.setDateUpdated(format.format(new Timestamp(new Date().getTime())));
				chatPrompt.setLastUpdatedBy(userId);
				Workflow workflow = chatPrompt.getWorkflow();
				workflow.setDateUpdated(new Timestamp(new Date().getTime()));
				workflow.setLastUpdated(userId);
				String json = new ObjectMapper().writeValueAsString(chatPrompt);
				Document promtDoc = Document.parse(json);
				collection.findOneAndUpdate(Filters.eq("NAME", chatChannelName),
						Updates.addToSet("CHAT_PROMPTS", promtDoc));
				log.trace("Exit ChatPrompt.postChatPrompt()");
			} else {
				throw new ForbiddenException("CHANNEL_DOES_NOT_EXIST");
			}

		} catch (Exception e) {
			e.printStackTrace();

		}

		return chatPrompt;

	}

	@PutMapping("/channels/chat/{name}/prompt/{promptId}")
	public @Valid ChatPrompt putChatPrompt(HttpServletRequest request,
			@RequestParam("authentication_token") String uuid, @PathVariable("name") String chatChannelName,
			@PathVariable("promptId") String promptId, @Valid @RequestBody ChatPrompt chatPrompt) {
		try {
			log.trace("Enter ChatPrompt.putChatPrompt()");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userRole = user.getString("ROLE");
			String userId = user.getString("USER_ID");
			String companyId = user.getString("COMPANY_ID");
			Boolean update = false;

			if (!role.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			String collectionName = "channels_chat_" + companyId;

			MongoCollection<Document> collection = mongoTemplate.getCollection("channels_chat_" + companyId);
			Document chatchannel = collection.find().first();
			List<Document> pts = (List<Document>) chatchannel.get("CHAT_PROMPTS");
			List<Document> updatedPrompts = new ArrayList<Document>();
			for (Document pt : pts) {
				if (pt.get("PROMPT_ID").equals(promptId)) {
					update = true;
					List<Conditions> conditions = chatPrompt.getConditions();
					for (Conditions condition : conditions) {
						if (condition.getCondition().equals("STILL_ON_PAGE")
								|| condition.getCondition().equals("STILL_ON_SITE")) {
							if (condition.getOpearator() != null) {
								throw new BadRequestException("INVALID_OPERATOR");
							}
						} else if (condition.getOpearator() == null) {
							throw new BadRequestException("OPERATOR_NOT_NULL");
						} else if (!Global.validChatPromptOperators.contains(condition.getOpearator())) {
							throw new BadRequestException("INVALID_OPERATOR");
						}

					}
					if (global.isExists("NAME", chatChannelName, collectionName)) {
						SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
						chatPrompt.setDateUpdated(format.format(new Timestamp(new Date().getTime())));
						chatPrompt.setLastUpdatedBy(userId);
						Workflow workflow = chatPrompt.getWorkflow();
						workflow.setDateUpdated(new Timestamp(new Date().getTime()));
						workflow.setLastUpdated(userId);
						String json = new ObjectMapper().writeValueAsString(chatPrompt);
						Document promtDoc = Document.parse(json);
						promtDoc.put("PROMPT_ID", pt.getString("PROMPT_ID"));
						updatedPrompts.add(promtDoc);
						log.trace("Exit ChatPrompt.putChatPrompt()");
					} else {
						throw new ForbiddenException("CHANNEL_DOES_NOT_EXIST");
					}

				} else {
					updatedPrompts.add(pt);
				}
				if (update) {
					collection.findOneAndUpdate(Filters.eq("NAME", chatChannelName),
							Updates.set("CHAT_PROMPTS", updatedPrompts));
				}
			}

		} catch (Exception e) {
			e.printStackTrace();

		}

		return chatPrompt;

	}

	@DeleteMapping("/channels/chat/{name}/prompt/{promptId}")
	public RequestEntity<Object> deleteChatPrompt(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("promptId") String promptId, @PathVariable("name") String chatChannelName) {
		try {
			log.trace("Enter ChatPrompt.getChatPrompt()");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");

			if (!role.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			String collectionName = "channels_chat_" + companyId;
			MongoCollection<Document> collection = mongoTemplate.getCollection("channels_chat_" + companyId);

			if (global.isExists("NAME", chatChannelName, collectionName)) {
				Document chatChannel = collection.find(Filters.eq("NAME", chatChannelName)).first();
				if (chatChannel.containsKey("CHAT_PROMPTS")) {

					ArrayList<Document> promptsDocuments = (ArrayList) chatChannel.get("CHAT_PROMPTS");
					for (Document document : promptsDocuments) {
						if (document.get("PROMPT_ID").equals(promptId)) {
							collection.updateOne(Filters.eq("NAME", chatChannelName),
									Updates.pull("CHAT_PROMPTS", Filters.eq("PROMPT_ID", promptId)));
							return null;
						}
					}
				} else {
					throw new ForbiddenException("PROMPT_DOES_NOT_EXIST");
				}

			}
			log.trace("Enter ChatPrompt.putChatPrompt()");
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonParseException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");

	}

	/*
	 * @DeleteMapping("/channels/chat/{name}/prompt") public ResponseEntity<Object>
	 * deleteChatPrompt(HttpServletRequest request,
	 * 
	 * @RequestParam("authentication_token") String uuid, @PathVariable("name")
	 * String chatChannelName) { if (request.getHeader("authentication_token") !=
	 * null) { uuid = request.getHeader("authentication_token"); }
	 * 
	 * JSONObject user = auth.getUserDetails(uuid); String userRole =
	 * user.getString("ROLE"); String companyId = user.getString("COMPANY_ID");
	 * 
	 * if (!role.isSystemAdmin(userRole, companyId)) { throw new
	 * ForbiddenException("FORBIDDEN"); }
	 * 
	 * String collectionName = "channels_chat_" + companyId;
	 * MongoCollection<Document> collection =
	 * mongoTemplate.getCollection("channels_chat_" + companyId); if
	 * (global.isExists("NAME", chatChannelName, collectionName)) { Document empty =
	 * new Document(); collection.findOneAndUpdate(Filters.eq("NAME",
	 * chatChannelName), Updates.set("CHAT_PROMPT", empty));
	 * 
	 * 
	 * return null;
	 * 
	 * } return null; }
	 */

}
