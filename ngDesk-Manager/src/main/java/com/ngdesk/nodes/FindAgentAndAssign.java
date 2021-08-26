package com.ngdesk.nodes;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import org.redisson.Redisson;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.ngdesk.Global;
import com.ngdesk.discussion.DiscussionController;
import com.ngdesk.discussion.DiscussionMessage;

@Component
public class FindAgentAndAssign extends Node {

	private static final Logger logger = LogManager.getLogger(FindAgentAndAssign.class);

	@Autowired
	Environment env;

	@Autowired
	Global global;

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	private SimpMessagingTemplate template;

	@Autowired
	PushNotification browserNotification;

	@Autowired
	RedissonClient redisson;

	@Autowired
	private ParentNode parentNode;

	@Autowired
	DiscussionController discussionController;

	// TODO: Improve assignment of chat to agent this is basic
	@Override
	public Map<String, Object> executeNode(Document node, Map<String, Object> inputMessage) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		logger.trace("Enter FindAgentAndAssign.executeNode()");
		try {

			Document company = global.getCompanyFromUUID(inputMessage.get("COMPANY_UUID").toString());

			if (company != null) {

				RMap<String, Map<String, Map<String, Object>>> companiesMap = redisson.getMap("companiesUsers");
				String subdomain = company.getString("COMPANY_SUBDOMAIN");
				String companyId = company.getObjectId("_id").toString();
				String companyUUID = company.getString("COMPANY_UUID");
				int maxNoOfChatsPerAgent = company.getInteger("MAX_CHATS_PER_AGENT");

				String moduleId = inputMessage.get("MODULE").toString();

				if (new ObjectId().isValid(moduleId)) {
					MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
					Document module = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();

					if (module.containsKey("SETTINGS") && module.get("SETTINGS") != null) {

						Document settings = (Document) module.get("SETTINGS");

						if (settings.containsKey("PERMISSIONS") && settings.get("PERMISSIONS") != null) {
							Document permissions = (Document) settings.get("PERMISSIONS");

							if (inputMessage.get("TYPE").toString().equalsIgnoreCase("CHAT")) {

								if (permissions.containsKey("CHAT") && permissions.get("CHAT") != null) {

									List<String> teamsWhoCanChat = (List<String>) permissions.get("CHAT");

									MongoCollection<Document> usersCollection = mongoTemplate
											.getCollection("Users_" + companyId);
									List<Document> users = usersCollection
											.find(Filters.and(Filters.eq("DELETED", false),
													Filters.or(Filters.eq("EFFECTIVE_TO", null),
															Filters.exists("EFFECTIVE_TO", false))))
											.into(new ArrayList<Document>());

									Map<String, List<String>> userTeamsMap = new HashMap<String, List<String>>();
									Map<String, String> userNamesMap = new HashMap<String, String>();

									for (Document user : users) {

										String firstName = user.getString("FIRST_NAME");
										String lastName = "";

//										if (user.getObjectId("_id").toString().equals("5d11290265675c2e86c89f75"))

										if (user.containsKey("LAST_NAME") && user.get("LAST_NAME") != null) {
											lastName = user.getString("LAST_NAME");
										}

										String userId = user.getObjectId("_id").toString();
										List<String> teams = (List<String>) user.get("TEAMS");
										userTeamsMap.put(userId, teams);

										userNamesMap.put(userId, firstName + " " + lastName);
									}

									if (companiesMap.containsKey(subdomain)) {

										Map<String, Map<String, Object>> usersMap = companiesMap.get(subdomain);

										SortedMap<Integer, List<String>> availableUsers = new TreeMap<Integer, List<String>>();

										for (String userId : usersMap.keySet()) {

											// TODO: once the map gets cleared on delete of company clean all existing
											// maps n delete code
											// If userId exists on map it is from previous instance of the company
											if (!userNamesMap.containsKey(userId)) {
												continue;
											}

											Map<String, Object> uMap = usersMap.get(userId);

											String status = uMap.get("STATUS").toString();
											boolean isOnline = false;
											if (status.equalsIgnoreCase("Online")) {
												isOnline = true;
											}

											boolean isAcceptingChats = (boolean) uMap.get("ACCEPTING_CHATS");
											int noOfChats = (int) uMap.get("NO_OF_CHATS");

											if (isOnline && isAcceptingChats && noOfChats < maxNoOfChatsPerAgent) {
												List<String> teams = userTeamsMap.get(userId);
												for (String teamId : teams) {
													if (teamsWhoCanChat.contains(teamId)) {

														List<String> usersList = new ArrayList<String>();
														if (availableUsers.containsKey(noOfChats)) {
															usersList = availableUsers.get(noOfChats);
														}

														if (!usersList.contains(userId)) {
															usersList.add(userId);
														}
														availableUsers.put(noOfChats, usersList);
														break;
													}
												}
											}
										}

										List<String> agentsWhoCanChat = new ArrayList<String>();
										// THE FIRST KEY IS THE LOWEST
										for (int key : availableUsers.keySet()) {
											agentsWhoCanChat = availableUsers.get(key);
											break;
										}

										if (agentsWhoCanChat.size() > 0) {
											int max = agentsWhoCanChat.size();
											int min = 1;

											int randomNumber = new Random().nextInt((max - min) + 1) + min;
											String agentAssignedId = agentsWhoCanChat.get(randomNumber - 1);

											// INCREMENT NO OF CHATS FOR AGENT
											int noOfChats = (int) usersMap.get(agentAssignedId).get("NO_OF_CHATS");

											usersMap.get(agentAssignedId).put("NO_OF_CHATS", noOfChats + 1);
											companiesMap.put(subdomain, usersMap);

											// ADD META_DATA ON THE CHAT
											String discussionFieldName = null;
											String discussionFieldId = null;
											List<Document> fields = (List<Document>) module.get("FIELDS");
											for (Document field : fields) {
												Document dataType = (Document) field.get("DATA_TYPE");
												if (dataType.getString("DISPLAY").equalsIgnoreCase("Discussion")) {
													discussionFieldName = field.getString("NAME");
													discussionFieldId = field.getString("FIELD_ID");
												}
											}

											String message = global.getFile("metadata_chat_agent_join.html");
											Map<String, Object> metaDataMessage = new HashMap<String, Object>();

											message = message.replace("NAME_REPLACE",
													userNamesMap.get(agentAssignedId));
											message = message.replace("DATE_TIME_REPLACE",
													new SimpleDateFormat("MMM d y h:mm:ss a")
															.format(new Timestamp(new Date().getTime())));

											String systemUserUUID = global.getSystemUser(companyId);

											metaDataMessage.put("COMPANY_UUID", companyUUID);
											metaDataMessage.put("MESSAGE_ID", UUID.randomUUID().toString());
											metaDataMessage.put("USER_UUID", systemUserUUID);

											String moduleName = module.getString("NAME");
											MongoCollection<Document> entriesCollection = mongoTemplate.getCollection(
													moduleName.replaceAll("\\s+", "_") + "_" + companyId);
											List<Map<String, Object>> builtMessage = global
													.buildDiscussionPayload(metaDataMessage, message, "META_DATA");
											Document messageDoc = Document
													.parse(new ObjectMapper().writeValueAsString(builtMessage.get(0)));
											Document entry = entriesCollection
													.find(Filters.eq("SESSION_UUID", inputMessage.get("SESSION_UUID")))
													.first();
											if (discussionFieldName != null) {

												// INSERT META_DATA INTO MESSAGES USING DISCUSSION CONTROLLER
												builtMessage.get(0).remove("DATE_CREATED");
												DiscussionMessage discussionMessage = new ObjectMapper().readValue(
														new ObjectMapper().writeValueAsString(builtMessage.get(0))
																.toString(),
														DiscussionMessage.class);
												discussionMessage.setSubdomain(company.getString("COMPANY_SUBDOMAIN"));
												discussionMessage.setModuleId(moduleId);
												discussionMessage.setEntryId(entry.getObjectId("_id").toString());

												discussionController.post(discussionMessage);

											}

											// SEND META_DATA TO ALL PARTIES INVOLVED

											String confirmationTopic = "topic/chat/"
													+ inputMessage.get("SESSION_UUID").toString();
											this.template.convertAndSend(confirmationTopic,
													messageDoc.toJson().toString());

											MongoCollection<Document> notificationsCollection = mongoTemplate
													.getCollection("notifications_" + companyId);

											// SEND OUT NOTIFICATIONS TO AGENTS
//											JSONObject notificationMessage = new JSONObject();
//											notificationMessage.put("MESSAGE_TYPE", "NOTIFICATION");
//											notificationMessage.put("DATE_CREATED",
//													global.getFormattedDate(new Timestamp(new Date().getTime())));
//											notificationMessage.put("DATA_ID", entry.getObjectId("_id").toString());
//											notificationMessage.put("MODULE_ID", moduleId);
//											notificationMessage.put("MODULE_NAME", module.getString("NAME"));
//											notificationMessage.put("MESSAGE",
//													"You have been assigned a new chat");
//											notificationMessage.put("READ", false);
//											notificationMessage.put("NOTIFICATION_UUID", UUID.randomUUID().toString());
//											notificationMessage.put("RECEPIENT", agentAssignedId);
//
//											// NOTIFY AGENT AND CUSTOMER
//
											String agentTopic = "topic/notify/" + agentAssignedId;
											String userTopic = "topic/notify/"
													+ inputMessage.get("SESSION_UUID").toString();
//
//											this.template.convertAndSend(agentTopic, notificationMessage.toString());
//
//											notificationsCollection
//													.insertOne(Document.parse(notificationMessage.toString()));

											JSONObject notifyMessage = new JSONObject();
											notifyMessage.put("MESSAGE_TYPE", "NOTIFICATION");
											notifyMessage.put("DATE_CREATED",
													global.getFormattedDate(new Timestamp(new Date().getTime())));
											notifyMessage.put("DATA_ID", inputMessage.get("DATA_ID").toString());
											notifyMessage.put("MODULE_ID", moduleId);
											notifyMessage.put("MODULE_NAME", module.getString("NAME"));

											// NOTIFY END USER
											notifyMessage.put("MESSAGE",
													userNamesMap.get(agentAssignedId) + " has joined the chat");
											this.template.convertAndSend(userTopic, notifyMessage.toString());

											// NOTIFY AGENT
											notifyMessage.put("MESSAGE", "You have been assigned a new chat");
											notifyMessage.put("READ", false);
											notifyMessage.put("NOTIFICATION_UUID", UUID.randomUUID().toString());
											notifyMessage.put("RECEPIENT", agentAssignedId);

											this.template.convertAndSend(agentTopic, notifyMessage.toString());

											notificationsCollection.insertOne(Document.parse(notifyMessage.toString()));

											// FIREBASE PUSH NOTIFICATION
											MongoCollection<Document> tokenCollection = mongoTemplate
													.getCollection("user_tokens_" + companyId);
											Document agentDoc = usersCollection
													.find(Filters.eq("_id", new ObjectId(agentAssignedId))).first();
											Document userDoc = tokenCollection
													.find(Filters.eq("USER_UUID", agentDoc.getString("USER_UUID")))
													.first();
											String url = "https://" + subdomain + ".ngdesk.com/render/" + moduleId
													+ "/detail/" + entry.getObjectId("_id").toString() + "";
											browserNotification.sendWebNotifications(userDoc,
													"You have been assigned a new chat", subdomain, url);
											browserNotification.sendAndroidNotifications(userDoc,
													"You have been assigned a new chat, please respond from Web",
													"ngDesk notification");
											browserNotification.sendIosNotifications(userDoc,
													"You have been assigned a new chat, please respond from Web");

											List<String> list = new ArrayList<String>();
											list.add(agentAssignedId);
											inputMessage.put("AGENTS", list);

										}
									}
								}
							}
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		ArrayList<Document> connections = (ArrayList<Document>) node.get("CONNECTIONS_TO");

		// UPDATE ENTRY NODE HAS ONLY ONE CONNECTION
		if (connections != null && connections.size() == 1) {
			Document connection = connections.get(0);
			resultMap.put("NODE_ID", connection.getString("TO_NODE"));
		}

		resultMap.put("INPUT_MESSAGE", inputMessage);

		logger.trace("Exit FindAgentAndAssign.executeNode() ");
		return resultMap;
	}

}
