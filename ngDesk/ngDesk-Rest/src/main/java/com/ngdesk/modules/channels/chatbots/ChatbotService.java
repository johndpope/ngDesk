package com.ngdesk.modules.channels.chatbots;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.bson.Document;
import org.bson.types.ObjectId;
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
public class ChatbotService {

	private final Logger log = LoggerFactory.getLogger(ChatbotService.class);

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Authentication auth;

	@Autowired
	private Global global;

	@Autowired
	private RoleService roleService;

	@GetMapping("/modules/{module_id}/chatbots")
	public ResponseEntity<Object> getChatBots(HttpServletRequest request,
			@RequestParam(value = "sort", required = false) String sort,
			@RequestParam(value = "order", required = false) String order,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId) {

		JSONArray chatBots = new JSONArray();
		JSONObject resultObj = new JSONObject();
		int totalSize = 0;
		try {
			log.trace("Enter ChatbotService.chatBots()  moduleId: " + moduleId);

			// GET COMPANY ID
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String companyId = user.getString("COMPANY_ID");

			// ACCESS MONGO
			MongoCollection<Document> collection = mongoTemplate.getCollection("modules_" + companyId);
			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();

			// CHECK DOC
			if (module != null) {

				if (!roleService.isAuthorizedForModule(userId, "GET", moduleId, companyId)) {
					throw new ForbiddenException("FORBIDDEN");
				}
				ArrayList<Document> chatBotDocuments = (ArrayList) module.get("CHAT_BOTS");

				// SORT THE CHATBOTS BASED ON ASCENDING OR DESCENDING ORDER
				if (sort != null && order != null) {
					Collections.sort(chatBotDocuments, new Comparator<Document>() {
						@Override
						public int compare(Document chatBot1, Document chatBot2) {
							if (order.equals("desc")) {
								return chatBot2.getString(sort).compareTo(chatBot1.getString(sort));
							}
							return chatBot1.getString(sort).compareTo(chatBot2.getString(sort));
						}
					});
				}

				totalSize = chatBotDocuments.size();
				for (Document chatBotDocument : chatBotDocuments) {
					Chatbot chatBot = new ObjectMapper().readValue(chatBotDocument.toJson(), Chatbot.class);
					JSONObject chatBotJson = new JSONObject(new ObjectMapper().writeValueAsString(chatBot));
					chatBots.put(chatBotJson);
				}

			} else {
				throw new ForbiddenException("MODULE_NOT_EXISTS");
			}
			resultObj.put("CHAT_BOTS", chatBots);
			resultObj.put("TOTAL_RECORDS", totalSize);
			log.trace("Exit ChatbotService.chatBots()  moduleId: " + moduleId);
			return new ResponseEntity<>(resultObj.toString(), Global.postHeaders, HttpStatus.OK);

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

	@GetMapping("/modules/{module_id}/chatbots/{chatbot_id}")
	public Chatbot getChatbotById(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@RequestParam(value = "chatbotName", required = false) String chatbotName,
			@PathVariable("module_id") String moduleId, @PathVariable("chatbot_id") String chatbotId)
			throws JsonParseException, JsonMappingException, IOException {
		try {
			log.trace("Enter ChatbotService.getChatbotById() moduleId: " + moduleId + ", chatbotId: " + chatbotId);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}

			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String companyId = user.getString("COMPANY_ID");
			String collectionName = "modules_" + companyId;

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();

			if (module != null) {
				String moduleName = module.getString("NAME");

				if (!roleService.isAuthorizedForModule(userId, "GET", moduleId, companyId)) {
					throw new ForbiddenException("FORBIDDEN");
				}

				if (collection
						.find(Filters.and(Filters.eq("NAME", moduleName),
								Filters.elemMatch("CHAT_BOTS", Filters.eq("CHAT_BOT_ID", chatbotId))))
						.first() != null) {
					ArrayList<Document> chatbotDocuments = (ArrayList) module.get("CHAT_BOTS");
					// GET SPECIFIC CHATBOT
					for (Document chatbotDocument : chatbotDocuments) {

						if (chatbotDocument.get("CHAT_BOT_ID") != null
								&& chatbotDocument.get("CHAT_BOT_ID").toString().equals(chatbotId)) {

							Chatbot chatbot = new ObjectMapper().readValue(chatbotDocument.toJson(), Chatbot.class);
							log.trace("Exit ChatBotService.getChatbotById() moduleName: " + moduleName + ", chatbotId: "
									+ chatbotId);
							return chatbot;
						}

					}
				} else {
					throw new ForbiddenException("CHATBOT_NOT_EXISTS");
				}
			} else {
				throw new ForbiddenException("MODULE_NOT_EXISTS");
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");

	}

	@PostMapping("/modules/{module_id}/chatbots")
	public Chatbot postChatbot(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId, @Valid @RequestBody Chatbot chatbot) {
		try {
			log.trace("Enter ChatbotService.postChatbot()");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			// GET COMPANY ID
			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String companyId = user.getString("COMPANY_ID");
			String roleId = user.getString("ROLE");

			// ACCESS MONGO
			MongoCollection<Document> collection = mongoTemplate.getCollection("modules_" + companyId);

			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();

			if (module == null) {
				throw new BadRequestException("MODULE_NOT_EXISTS");
			}

			if (!roleService.isSystemAdmin(roleId, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			String moduleName = module.getString("NAME");

			Document existingDocument = collection.find(Filters.and(Filters.eq("NAME", moduleName),
					Filters.elemMatch("CHAT_BOTS", Filters.eq("NAME", chatbot.getName())))).first();

			if (existingDocument != null) {
				throw new BadRequestException("CHAT_BOT_NAME_ALREADY_EXISTS");
			}

			for (ChatbotNode node : chatbot.getWorkflow().getNodes()) {
				String subType = node.getSubType();

				if (subType.equalsIgnoreCase("Phone") || subType.equalsIgnoreCase("Email")
						|| subType.equalsIgnoreCase("Name")) {
					node.getValues().setMapping("");
				}
			}
			// TODO: Validate incoming fields

			chatbot.setChatbotId(UUID.randomUUID().toString());
			String chatbotBody = new ObjectMapper().writeValueAsString(chatbot).toString();
			Document chatbotDocument = Document.parse(chatbotBody);

			collection.updateOne(Filters.eq("_id", new ObjectId(moduleId)),
					Updates.addToSet("CHAT_BOTS", chatbotDocument));

			log.trace("Exit ChatbotService.postChatbot()");

			return chatbot;
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PutMapping("/modules/{module_id}/chatbots/{chatbot_id}")
	public Chatbot putChatbot(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId, @PathVariable("chatbot_id") String chatbotId,
			@Valid @RequestBody Chatbot chatbot) {
		try {
			log.trace("Enter ChatbotService.putChatbot() moduleId: " + moduleId + ", chatbotID: " + chatbotId);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");
			String userId = user.getString("USER_ID");

			String collectionName = "modules_" + companyId;

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();

			if (chatbot.getChatbotId() != null) {

				if (module != null) {
					String moduleName = module.getString("NAME");

					if (!roleService.isAuthorizedForModule(userId, "PUT", moduleId, companyId)) {
						throw new ForbiddenException("FORBIDDEN");
					}

					if (collection
							.find(Filters.and(Filters.eq("NAME", moduleName),
									Filters.elemMatch("CHAT_BOTS", Filters.eq("CHAT_BOT_ID", chatbotId))))
							.first() != null) {

						for (ChatbotNode node : chatbot.getWorkflow().getNodes()) {
							String subType = node.getSubType();
							if (subType.equalsIgnoreCase("Phone") || subType.equalsIgnoreCase("Email")
									|| subType.equalsIgnoreCase("Name")) {
								node.getValues().setMapping("");
							}
						}

						Document existingDocument = collection.find(Filters.and(Filters.eq("NAME", moduleName),
								Filters.elemMatch("CHAT_BOTS", Filters.eq("CHAT_BOT_ID", chatbotId)))).first();

						List<Document> chatBots = (List<Document>) module.get("CHAT_BOTS");
						for (Document chatBot : chatBots) {
							if (chatBot.getString("NAME").equalsIgnoreCase(chatbot.getName())
									&& !chatBot.getString("CHAT_BOT_ID").equals(chatbotId)) {
								throw new ForbiddenException("WORKFLOW_NAME_EXISTS");
							}
						}

						ArrayList<Document> chatbots = (ArrayList<Document>) module.get("CHAT_BOTS");
						for (Document chatbotDoc : chatbots) {
							if (chatbotDoc.getString("CHAT_BOT_ID").equals(chatbotId)) {
								// SAME CHATBOT
								JSONObject existingChatBot = new JSONObject(chatbotDoc.toJson());
								String payload = new ObjectMapper().writeValueAsString(chatbot).toString();
								Document chatbotDocument = Document.parse(payload);
								collection.updateOne(Filters.eq("NAME", moduleName),
										Updates.pull("CHAT_BOTS", Filters.eq("CHAT_BOT_ID", chatbot.getChatbotId())));
								collection.updateOne(Filters.eq("NAME", moduleName),
										Updates.push("CHAT_BOTS", chatbotDocument));

								log.trace("Exit ChatbotService.putChatbot() moduleName: " + moduleName + ", chatbotId: "
										+ chatbotId);
								return chatbot;
							}

						}

					} else {
						throw new ForbiddenException("CHATBOT_NOT_EXISTS");
					}

				} else {
					throw new ForbiddenException("MODULE_NOT_EXISTS");
				}
			} else {
				throw new BadRequestException("CHAT_BOT_ID_NULL");
			}

		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@DeleteMapping("/modules/{module_id}/chatbots/{chatbot_id}")
	public ResponseEntity<Object> deleteChatbots(HttpServletRequest request,
			@RequestParam("authentication_token") String uuid, @PathVariable("chatbot_id") String chatBotId,
			@PathVariable("module_id") String moduleId) {
		try {
			log.trace("Enter ChatbotService.deleteChatbots() moduleId: " + moduleId + ", chatbot_id: " + chatBotId);
			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String companyId = user.getString("COMPANY_ID");
			String collectionName = "modules_" + companyId;
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();

			if (module != null) {

				String moduleName = module.getString("NAME");
				if (!roleService.isAuthorizedForModule(userId, "DELETE", moduleId, companyId)) {
					throw new ForbiddenException("FORBIDDEN");
				}

				List<Document> chatbots = (List<Document>) module.get("CHAT_BOTS");

				if (collection
						.find(Filters.and(Filters.eq("NAME", moduleName),
								Filters.elemMatch("CHAT_BOTS", Filters.eq("CHAT_BOT_ID", chatBotId))))
						.first() != null) {
					collection.updateOne(Filters.eq("NAME", moduleName),
							Updates.pull("CHAT_BOTS", Filters.eq("CHAT_BOT_ID", chatBotId)));
					log.trace("Exit ChatbotService.deleteChatbots() moduleId " + moduleName + ", chatbot_id: "
							+ chatBotId);
					return new ResponseEntity<Object>(HttpStatus.OK);
				} else {
					throw new ForbiddenException("CHATBOT_NOT_EXISTS");
				}
			} else {
				throw new ForbiddenException("MODULE_NOT_EXISTS");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@GetMapping("/modules/{module_id}/chatbots/templates")
	public ResponseEntity<Object> getChatBotsTemplates(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId) {

		JSONArray chatBots = new JSONArray();
		JSONObject resultObj = new JSONObject();
		int totalSize = 0;
		try {
			log.trace("Enter ChatbotService.chatBots()  moduleId: " + moduleId);

			// GET COMPANY ID
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String companyId = user.getString("COMPANY_ID");

			// ACCESS MONGO
			MongoCollection<Document> collection = mongoTemplate.getCollection("modules_" + companyId);
			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();

			// CHECK DOC
			if (module != null) {

				if (!roleService.isAuthorizedForModule(userId, "GET", moduleId, companyId)) {
					throw new ForbiddenException("FORBIDDEN");
				}
				ArrayList<Document> chatBotDocuments = (ArrayList) module.get("CHAT_BOTS");

				Document chatbotDocument = Document.parse(global.getFile("ChatBotTemplate.json"));
				List<Document> templates = (List<Document>) chatbotDocument.get("TEMPLATES");
				for (Document template : templates) {
					chatBots.put(template);
					totalSize++;
				}

			} else {
				throw new ForbiddenException("MODULE_NOT_EXISTS");
			}
			resultObj.put("CHAT_BOTS", chatBots);
			resultObj.put("TOTAL_RECORDS", totalSize);
			log.trace("Exit ChatbotService.chatBots()  moduleId: " + moduleId);
			return new ResponseEntity<>(resultObj.toString(), Global.postHeaders, HttpStatus.OK);

		} catch (JSONException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@GetMapping("/modules/{module_id}/chatbots/template")
	public Chatbot getChatbotTemplate(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@RequestParam(value = "chatbotName", required = false) String chatbotName,
			@PathVariable("module_id") String moduleId) throws JsonParseException, JsonMappingException, IOException {
		try {
			log.trace("Enter ChatbotService.getChatbotTemplate() moduleId: " + moduleId + "template");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}

			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String companyId = user.getString("COMPANY_ID");
			String collectionName = "modules_" + companyId;

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();

			if (module != null) {
				String moduleName = module.getString("NAME");

				if (!roleService.isAuthorizedForModule(userId, "GET", moduleId, companyId)) {
					throw new ForbiddenException("FORBIDDEN");
				}
				if (chatbotName != null) {
					Document chatbotDocument = Document.parse(global.getFile("ChatBotTemplate.json"));
					List<Document> templates = (List<Document>) chatbotDocument.get("TEMPLATES");
					for (Document template : templates) {

						if (chatbotName.equals(template.get("NAME").toString())) {

							Chatbot chatbot = new ObjectMapper().readValue(template.toJson(), Chatbot.class);
							return chatbot;
						}
					}

				} else {
					throw new ForbiddenException("CHATBOT_NOT_EXISTS");
				}
			} else {
				throw new ForbiddenException("MODULE_NOT_EXISTS");
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");

	}

}
