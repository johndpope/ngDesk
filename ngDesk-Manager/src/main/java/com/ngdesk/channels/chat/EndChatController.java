package com.ngdesk.channels.chat;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import org.redisson.Redisson;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.ngdesk.Global;
import com.ngdesk.SendMail;
import com.ngdesk.discussion.DiscussionController;
import com.ngdesk.discussion.DiscussionMessage;
import com.ngdesk.discussion.EndChat;
import com.ngdesk.discussion.Sender;
import com.ngdesk.nodes.ParentNode;
import com.ngdesk.nodes.PushNotification;

//@Component
//@Controller
public class EndChatController {

	private final Logger log = LoggerFactory.getLogger(EndChatController.class);

	@Autowired
	private Environment env;

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	Global global;

	@Autowired
	SendMail sendMail;

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

	@MessageMapping("/chat/end")
	public void endChat(EndChat endChat) {
		log.trace("Enter EndChatController.endChat() subdoamin: " + endChat.getSubdomain() + " sessionUuid: "
				+ endChat.getSessionUuid());
		try {
			if (endChat.getSubdomain() != null) {
				Document company = global.getCompanyFromSubdomain(endChat.getSubdomain());

				if (company != null) {
					String companyId = company.getObjectId("_id").toString();
					String subdomain = company.getString("COMPANY_SUBDOMAIN");
					String companyUUID = company.getString("COMPANY_UUID");

					if (endChat.getChannel() != null && ObjectId.isValid(endChat.getChannel())) {

						MongoCollection<Document> chatChannelCollection = mongoTemplate
								.getCollection("channels_chat_" + companyId);

						Document channel = chatChannelCollection
								.find(Filters.eq("_id", new ObjectId(endChat.getChannel()))).first();
						if (channel != null) {

							String moduleId = channel.getString("MODULE");
							if (ObjectId.isValid(moduleId)) {
								MongoCollection<Document> modulesCollection = mongoTemplate
										.getCollection("modules_" + companyId);

								Document module = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId)))
										.first();
								if (module != null) {
									String moduleName = module.getString("NAME");
									
									MongoCollection<Document> entriesCollection = mongoTemplate
											.getCollection(moduleName.replaceAll("\\s+", "_") + "_" + companyId);
									Document entry = entriesCollection
											.find(Filters.eq("SESSION_UUID", endChat.getSessionUuid())).first();

									if (entry != null) {
										String entryId = entry.getObjectId("_id").toString();
										entriesCollection.updateOne(Filters.eq("_id", new ObjectId(entryId)),
												Updates.combine(Updates.set("STATUS", "Browsing"),
														Updates.set("DATE_UPDATED",
																new Date()),
														Updates.unset("META_DATA")));

										RMap<String, Map<String, Map<String, Object>>> companiesMap = redisson
												.getMap("companiesUsers");
										log.debug("Contains subdomain: " + companiesMap.containsKey(subdomain));

										if (companiesMap.containsKey(subdomain)) {

											log.debug("Entry contains agents: "
													+ (entry.containsKey("AGENTS") && entry.get("AGENTS") != null));
											Document metaData = null;
											if (entry.containsKey("META_DATA") && entry.get("META_DATA") != null) {
												metaData = (Document) entry.get("META_DATA");
											}
											if ((entry.containsKey("AGENTS") && entry.get("AGENTS") != null)
													|| (metaData != null
															&& metaData.getBoolean("IS_BEING_SERVED_BY_BOT", false))) {
												List<String> agents = (List<String>) entry.get("AGENTS");
												if (agents != null) {
													Map<String, Map<String, Object>> usersMap = companiesMap
															.get(subdomain);
													for (String userId : agents) {

														log.debug("UsersMap contains user: "
																+ usersMap.containsKey(userId));

														if (usersMap.containsKey(userId)) {
															Map<String, Object> uMap = usersMap.get(userId);
															int noOfChats = (int) uMap.get("NO_OF_CHATS");
															noOfChats = noOfChats - 1;
															uMap.put("NO_OF_CHATS", noOfChats);
															usersMap.put(userId, uMap);
														}
													}
													companiesMap.put(subdomain, usersMap);
												}

												JSONObject notifyMessage = new JSONObject();
												notifyMessage.put("MESSAGE_TYPE", "NOTIFICATION");
												notifyMessage.put("DATE_CREATED",
														global.getFormattedDate(new Timestamp(new Date().getTime())));
												notifyMessage.put("DATA_ID", entryId);
												notifyMessage.put("MODULE_ID", moduleId);
												notifyMessage.put("MODULE_NAME", module.getString("NAME"));

												// ADD META_DATA ON THE MESSAGE
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

												String message = global.getFile("metadata_chat_end.html");
												Map<String, Object> metaDataMessage = new HashMap<String, Object>();
												String systemUserUUID = global.getSystemUser(companyId);

												MongoCollection<Document> usersCollection = mongoTemplate
														.getCollection("Users_" + companyId);
												Document user = usersCollection
														.find(Filters.eq("USER_UUID", endChat.getUserUuid())).first();
												String firstName = user.getString("FIRST_NAME");
												String lastName = "";
												if (user.getString("LAST_NAME") != null) {
													lastName = user.getString("LAST_NAME");
												}
												String endUserId = user.getObjectId("_id").toString();
												// NOTIFY END USER
												notifyMessage.put("MESSAGE",
														firstName + " " + lastName + " has ended the chat");

												message = message.replace("FIRST_NAME_REPLACE",
														user.getString("FIRST_NAME"));
												message = message.replace("LAST_NAME_REPLACE",
														user.getString("LAST_NAME"));

												metaDataMessage.put("COMPANY_UUID", companyUUID);
												metaDataMessage.put("MESSAGE_ID", UUID.randomUUID().toString());
												metaDataMessage.put("USER_UUID", systemUserUUID);

												List<Map<String, Object>> builtMessage = global
														.buildDiscussionPayload(metaDataMessage, message, "META_DATA");
												Document messageDoc = Document.parse(
														new ObjectMapper().writeValueAsString(builtMessage.get(0)));
												if (discussionFieldName != null) {
													// INSERT META_DATA INTO MESSAGES USING DISCUSSION CONTROLLER
													builtMessage.get(0).remove("DATE_CREATED");
													DiscussionMessage discussionMessage = new ObjectMapper().readValue(
															new ObjectMapper().writeValueAsString(builtMessage.get(0))
																	.toString(),
															DiscussionMessage.class);
													discussionMessage
															.setSubdomain(company.getString("COMPANY_SUBDOMAIN"));
													discussionMessage.setModuleId(moduleId);
													discussionMessage.setEntryId(entryId);

													discussionController.post(discussionMessage);
												}

												// SEND META_DATA TO ALL PARTIES INVOLVED

												String confirmationTopic = "topic/chat/" + endChat.getSessionUuid();
												this.template.convertAndSend(confirmationTopic,
														messageDoc.toJson().toString());

												MongoCollection<Document> notificationsCollection = mongoTemplate
														.getCollection("notifications_" + companyId);

												String userTopic = "topic/notify/" + endChat.getSessionUuid();
												this.template.convertAndSend(userTopic, notifyMessage.toString());
												if (agents != null) {
													for (String userId : agents) {

														// FILTERING OUT THE AGENT WHO ENDED THE CHAT
														if (endUserId.equals(userId)) {
															continue;
														}
														// NOTIFYING AGENTS OTHER THAN THE ONE WHO ENDED THE CHAT
														String agentTopic = "topic/notify/" + userId;
														notifyMessage.put("READ", false);
														notifyMessage.put("NOTIFICATION_UUID",
																UUID.randomUUID().toString());
														notifyMessage.put("RECEPIENT", userId);
														notificationsCollection
																.insertOne(Document.parse(notifyMessage.toString()));
														this.template.convertAndSend(agentTopic,
																notifyMessage.toString());

														// FIREBASE PUSH NOTIFICATION

														MongoCollection<Document> tokenCollection = mongoTemplate
																.getCollection("user_tokens_" + companyId);
														Document agentDoc = usersCollection
																.find(Filters.eq("_id", new ObjectId(userId))).first();
														Document userDoc = tokenCollection.find(Filters.eq("USER_UUID",
																agentDoc.getString("USER_UUID"))).first();
														String url = "https://" + subdomain
																+ ".ngdesk.com/render/" + moduleId
																+ "/edit/" + entryId + "";
														browserNotification.sendWebNotifications(userDoc,
																firstName + " " + lastName + " has ended the chat",
																subdomain, url);
														browserNotification.sendAndroidNotifications(userDoc,
																"Your chat has ended", "ngDesk notification");
														browserNotification.sendIosNotifications(userDoc,
																"Your chat has ended");

													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}

			log.trace("Enter EndChatController.endChat()");
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.trace("Exit EndChatController.endChat()");
	}
}
