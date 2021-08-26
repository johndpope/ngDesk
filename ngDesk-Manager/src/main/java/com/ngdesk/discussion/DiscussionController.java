package com.ngdesk.discussion;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.validation.Valid;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.ngdesk.Global;
import com.ngdesk.SendMail;
import com.ngdesk.nodes.HttpRequestNode;
import com.ngdesk.nodes.ParentNode;
import com.ngdesk.nodes.PushNotification;
import com.ngdesk.wrapper.Wrapper;

import net.logstash.logback.encoder.org.apache.commons.lang.StringEscapeUtils;
import net.logstash.logback.encoder.org.apache.commons.lang.exception.NestableRuntimeException;

@Component
@RestController
public class DiscussionController {

	private final Logger log = LoggerFactory.getLogger(DiscussionController.class);

	@Autowired
	private Global global;

	@Autowired
	private SimpMessagingTemplate template;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	Environment env;

	@Autowired
	HttpRequestNode httpRequestNode;

	@Autowired
	SendMail sendMail;

	@Autowired
	PushNotification browserNotification;

	@Autowired
	ParentNode parent;

	@Autowired
	Wrapper wrapper;

	@PostMapping("/discussion")
	public void postDiscussion(@Valid @RequestBody DiscussionMessage message) {
		post(message);
	}

	@MessageMapping("/discussion")
	public void post(DiscussionMessage message) {
		try {
			log.trace("Enter DiscussionController.post() subdomain: " + message.getSubdomain() + " sessionUuid: "
					+ message.getSessionUuid());

			Document company = global.getCompanyFromSubdomain(message.getSubdomain());

			if (company != null) {
				String companyId = company.getObjectId("_id").toString();
				if (message.getChannelId() != null && new ObjectId().isValid(message.getChannelId())) {

					log.debug("Valid Channel");

					// Needs to be unescaped since we escaped it in DataService
					try {
						message.setMessage(StringEscapeUtils.unescapeJava(message.getMessage()));
					} catch (NumberFormatException e) {
						log.debug("Improper Formatting");
					} catch (NestableRuntimeException e) {
						log.debug("Imrpoer Formatting");
					}

					// FOR CHAT MODULE
					MongoCollection<Document> chatChannelCollection = mongoTemplate
							.getCollection("channels_chat_" + companyId);
					Document chatChannel = chatChannelCollection
							.find(Filters.eq("_id", new ObjectId(message.getChannelId()))).first();

					String channelId = chatChannel.getObjectId("_id").toString();

					if (chatChannel != null) {
						Document settings = (Document) chatChannel.get("SETTINGS");
						boolean preSurveyRequired = settings.getBoolean("PRE_SURVEY_REQUIRED");

						log.debug("Found Channel");

						String moduleId = chatChannel.getString("MODULE");

						Document module = global.getModuleFromId(moduleId, companyId);

						if (module != null) {
							String moduleName = module.getString("NAME");

							String collectionName = moduleName.replaceAll("\\s+", "_") + "_" + companyId;

							String discussionFieldName = null;
							List<Document> fields = (List<Document>) module.get("FIELDS");
							for (Document field : fields) {
								JSONObject fieldJson = new JSONObject(field.toJson());
								String dataType = fieldJson.getJSONObject("DATA_TYPE").getString("DISPLAY");

								if (dataType.equalsIgnoreCase("Discussion")) {
									discussionFieldName = fieldJson.getString("NAME");
									break;
								}
							}

							if (discussionFieldName != null) {

								MongoCollection<Document> entriesCollection = mongoTemplate
										.getCollection(collectionName);
								Document entry = entriesCollection
										.find(Filters.and(Filters.eq("SESSION_UUID", message.getSessionUuid()),
												Filters.eq("DELETED", false),
												Filters.or(Filters.eq("EFFECTIVE_TO", null),
														Filters.exists("EFFECTIVE_TO", false))))
										.first();

								if (entry != null) {

									log.debug("Found Entry");

									String entryId = entry.getObjectId("_id").toString();

									List<String> agents = new ArrayList<String>();
									if (entry.containsKey("AGENTS")) {
										agents = (List<String>) entry.get("AGENTS");
									}

									List<ObjectId> agentIds = new ArrayList<ObjectId>();

									for (String agent : agents) {
										agentIds.add(new ObjectId(agent));
									}

									MongoCollection<Document> usersCollection = mongoTemplate
											.getCollection("Users_" + companyId);
									Document user = null;
									user = usersCollection
											.find(Filters.and(Filters.eq("EMAIL_ADDRESS", message.getEmailAddress()),
													Filters.eq("DELETED", false),
													Filters.or(Filters.eq("EFFECTIVE_TO", null),
															Filters.exists("EFFECTIVE_TO", false))))
											.first();
									if (user == null && !preSurveyRequired && message.getEmailAddress().isEmpty()
											|| user == null && preSurveyRequired) {
										user = usersCollection.find(Filters.and(
												Filters.eq("_id", new ObjectId(entry.getString("REQUESTOR"))),
												Filters.eq("DELETED", false))).first();
									}

//									if (!preSurveyRequired && message.getEmailAddress().isEmpty()) {
//										
//									} else {
//										user = usersCollection.find(
//												Filters.and(Filters.eq("EMAIL_ADDRESS", message.getEmailAddress()),
//														Filters.eq("DELETED", false)))
//												.first();
//									}

									if (user != null) {

										log.debug("Found Agent");

										Sender sender = new Sender();
										sender.setFirstName(user.getString("FIRST_NAME"));
										if (user.containsKey("LAST_NAME") && user.get("LAST_NAME") != null) {
											sender.setLastName(user.getString("LAST_NAME"));
										} else {
											sender.setLastName("");
										}
										sender.setRole(user.getString("ROLE"));
										sender.setSender(user.getString("USER_UUID"));

										JSONObject senderJson = new JSONObject(
												new ObjectMapper().writeValueAsString(sender));
										JSONArray attachments = new JSONArray();

										if (message.getAttachments() != null) {
											attachments = new JSONArray(
													new ObjectMapper().writeValueAsString(message.getAttachments()));
										}

										JSONObject entryMessage = new JSONObject();

										org.jsoup.nodes.Document html = Jsoup.parse(message.getMessage());
										html.select("script, .hidden").remove();
										html.outputSettings().prettyPrint(false);
										String messageBody = html.toString();
										messageBody = messageBody.replaceAll("&amp;", "&");

										entryMessage.put("MESSAGE", messageBody);
										entryMessage.put("SENDER", senderJson);
										entryMessage.put("MESSAGE_ID", UUID.randomUUID().toString());
										entryMessage.put("ATTACHMENTS", attachments);
										entryMessage.put("MESSAGE_TYPE", "MESSAGE");
										Document entryMessageDoc = Document.parse(entryMessage.toString());
										entryMessageDoc.put("DATE_CREATED", new Date());

										log.debug("Updating the entry");

										entriesCollection.updateOne(Filters.eq("_id", new ObjectId(entryId)),
												Updates.addToSet(discussionFieldName, entryMessageDoc));

										MongoCollection<Document> attachmentsCollection = mongoTemplate
												.getCollection("attachments_" + companyId);

										for (int i = 0; i < attachments.length(); i++) {
											JSONObject attachment = attachments.getJSONObject(i);

											if (!attachment.isNull("HASH")) {
												Document attachmentDoc = attachmentsCollection
														.find(Filters.eq("HASH", attachment.getString("HASH"))).first();
												if (attachmentDoc != null) {
													String uuid = attachmentDoc.getString("ATTACHMENT_UUID");
													attachment.put("ATTACHMENT_UUID", uuid);
												}
											}
										}

										// SEND CONFIRMATION TO ALL PARTIES INVOLVED
										String confirmationTopic = "topic/chat/" + message.getSessionUuid();
										log.trace("Sending to Topic: " + confirmationTopic + ", Message: "
												+ entryMessage.toString());
										this.template.convertAndSend(confirmationTopic, entryMessage.toString());

										MongoCollection<Document> notificationsCollection = mongoTemplate
												.getCollection("notifications_" + companyId);

										// SEND OUT NOTIFICATIONS TO AGENTS
										for (String agentId : agents) {
											if (!agentId.equals(user.getObjectId("_id").toString())) {
												JSONObject notifyMessage = new JSONObject();
												notifyMessage.put("MESSAGE_TYPE", "NOTIFICATION");
												notifyMessage.put("DATE_CREATED",
														global.getFormattedDate(new Timestamp(new Date().getTime())));
												notifyMessage.put("DATA_ID", entryId);
												notifyMessage.put("MODULE_ID", moduleId);
												notifyMessage.put("MODULE_NAME", module.getString("NAME"));
												notifyMessage.put("MESSAGE",
														"You have a new message in the conversation");
												notifyMessage.put("READ", false);
												notifyMessage.put("NOTIFICATION_UUID", UUID.randomUUID().toString());
												notifyMessage.put("RECEPIENT", agentId);

												this.template.convertAndSend("topic/notify/" + agentId,
														notifyMessage.toString());

												notificationsCollection
														.insertOne(Document.parse(notifyMessage.toString()));

												// FIREBASE PUSH NOTIFICATION
												MongoCollection<Document> tokenCollection = mongoTemplate
														.getCollection("user_tokens_" + companyId);
												Document agentDoc = usersCollection
														.find(Filters.eq("_id", new ObjectId(agentId))).first();
												Document userDoc = tokenCollection
														.find(Filters.eq("USER_UUID", agentDoc.getString("USER_UUID")))
														.first();
												String url = "https://" + message.getSubdomain() + ".ngdesk.com/render/"
														+ moduleId + "/edit/" + entryId + "";
												browserNotification.sendWebNotifications(userDoc, html.text(),
														message.getSubdomain(), url);
												browserNotification.sendAndroidNotifications(userDoc,
														"You have a new message from," + sender.getFirstName()
																+ sender.getLastName() + "please respond from Web",
														"ngDesk notification");
												browserNotification.sendIosNotifications(userDoc,
														"You have a new message from," + sender.getFirstName()
																+ sender.getLastName() + "please respond from Web");
											}
										}
										// IF SERVED BY BOT
										Document updatedEntry = entriesCollection
												.find(Filters
														.and(Filters.eq("SESSION_UUID", message.getSessionUuid()),
																Filters.eq("DELETED", false),
																Filters.or(Filters.eq("EFFECTIVE_TO", null),
																		Filters.exists("EFFECTIVE_TO", false))))
												.first();

										if (updatedEntry.containsKey("META_DATA")) {
											Document botMetaData = (Document) updatedEntry.get("META_DATA");
											if (botMetaData.getBoolean("IS_BEING_SERVED_BY_BOT")
													&& botMetaData.get("CURRENT_STATE") != null && botMetaData
															.getString("CURRENT_STATE").equals("QUESTION_ASKED")) {

												updatedEntry.put("DATA_ID", updatedEntry.remove("_id").toString());
												updatedEntry.put("COMPANY_UUID", company.getString("COMPANY_UUID"));
												updatedEntry.put("USER_UUID", user.getString("USER_UUID"));
												updatedEntry.put("MODULE", moduleId);
												updatedEntry.put("CHANNEL_ID", channelId);
												updatedEntry.put("ANSWER", html.text());

												Map<String, Object> existingInputMap = new ObjectMapper()
														.readValue(entry.toJson(), Map.class);
												entry.put("OLD_COPY", existingInputMap);

												Map<String, Object> inputMap = new ObjectMapper()
														.readValue(updatedEntry.toJson(), Map.class);
												ArrayList<Document> workflows = (ArrayList<Document>) ((Document) chatChannel
														.get("WORKFLOW")).get("NODES");
												parent.executeWorkflow(workflows.get(0), workflows, inputMap);
												// parent.executeModuleWorkflow(inputMap, companyId, moduleId,
												// "UPDATE");
											}
										}
									}
								}
							}
						}
					}
				} else {
					// OTHER THAN CHAT MODULE
					Document module = global.getModuleFromId(message.getModuleId(), companyId);
					String moduleName = module.getString("NAME");

					// Needs to be unescaped since we escaped it in DataService
					try {
						message.setMessage(StringEscapeUtils.unescapeJava(message.getMessage()));
					} catch (NumberFormatException e) {
						log.debug("Improper Formatting");
					} catch (NestableRuntimeException e) {
						log.debug("Imrpoer Formatting");
					}

					String collectionName = moduleName.replaceAll("\\s+", "_") + "_" + companyId;

					String discussionFieldName = null;
					List<Document> fields = (List<Document>) module.get("FIELDS");
					for (Document field : fields) {
						JSONObject fieldJson = new JSONObject(field.toJson());
						String dataType = fieldJson.getJSONObject("DATA_TYPE").getString("DISPLAY");

						if (dataType.equalsIgnoreCase("Discussion")) {
							discussionFieldName = fieldJson.getString("NAME");
							break;
						}
					}

					if (discussionFieldName != null) {

						MongoCollection<Document> entriesCollection = mongoTemplate.getCollection(collectionName);
						Document existingEntry = entriesCollection
								.find(Filters.and(Filters.eq("_id", new ObjectId(message.getEntryId())),
										Filters.eq("DELETED", false)))
								.first();

						if (existingEntry != null) {
							String entryId = message.getEntryId();

							MongoCollection<Document> usersCollection = mongoTemplate
									.getCollection("Users_" + companyId);
							Sender sender = message.getSender();

							Document user = usersCollection
									.find(Filters.and(Filters.eq("USER_UUID", sender.getSender()),
											Filters.eq("DELETED", false), Filters.or(Filters.eq("EFFECTIVE_TO", null),
													Filters.exists("EFFECTIVE_TO", false))))
									.first();

							if (user != null) {

								JSONObject senderJson = new JSONObject(new ObjectMapper().writeValueAsString(sender));
								JSONArray attachments = new JSONArray();

								if (message.getAttachments() != null) {
									attachments = new JSONArray(
											new ObjectMapper().writeValueAsString(message.getAttachments()));
								}

								// ATTACHMENT
								MongoCollection<Document> attachmentsCollection = mongoTemplate
										.getCollection("attachments_" + companyId);
								for (int i = 0; i < attachments.length(); i++) {
									JSONObject attachment = attachments.getJSONObject(i);
									if (!attachment.isNull("HASH")) {
										Document attachmentDoc = attachmentsCollection
												.find(Filters.eq("HASH", attachment.getString("HASH"))).first();
										if (attachmentDoc != null) {
											String uuid = attachmentDoc.getString("ATTACHMENT_UUID");
											attachment.put("ATTACHMENT_UUID", uuid);
										}
									}
								}

								JSONObject entryMessage = new JSONObject();

								org.jsoup.nodes.Document html = Jsoup.parse(message.getMessage());
								html.select("script, .hidden").remove();
								html.outputSettings().prettyPrint(false);
								String messageBody = html.toString();
								messageBody = messageBody.replaceAll("&amp;", "&");

								entryMessage.put("MESSAGE", messageBody);
								entryMessage.put("SENDER", senderJson);
								if (message.getMessageId() == null) {
									entryMessage.put("MESSAGE_ID", UUID.randomUUID().toString());
								} else {
									entryMessage.put("MESSAGE_ID", message.getMessageId());
								}
								entryMessage.put("ATTACHMENTS", attachments);
								entryMessage.put("MESSAGE_TYPE", message.getMessageType());
								Document entryMessageDoc = Document.parse(entryMessage.toString());
								entryMessageDoc.put("DATE_CREATED", new Date());

								log.debug("Updating Entry: " + entryId);
								log.debug("Updating Message: " + entryMessage.toString());

								entriesCollection.updateOne(Filters.eq("_id", new ObjectId(entryId)),
										Updates.combine(Updates.set("DATE_UPDATED", new Date()),
												Updates.addToSet(discussionFieldName, entryMessageDoc)));
								Document entry = entriesCollection
										.find(Filters.and(Filters.eq("_id", new ObjectId(message.getEntryId())),
												Filters.eq("DELETED", false)))
										.first();

								entry.remove("_id");
								/*
								 * wrapper.loadDataToRedis(companyId, message.getModuleId(),
								 * message.getEntryId(), moduleName);
								 */

								// PUBLISH TO ENTRY ID
								String confirmationTopic = "topic/update/" + message.getEntryId();
								log.trace("Sending to Topic: " + confirmationTopic + ", Message: "
										+ entryMessage.toString());

								this.template.convertAndSend(confirmationTopic, entryMessage.toString());
								entry.put("DATA_ID", message.getEntryId());
								entry.put("COMPANY_UUID", company.getString("COMPANY_UUID"));
								entry.put("USER_UUID", user.getString("USER_UUID"));
								entry.put("MODULE", message.getModuleId());
								Map<String, Object> existingInputMap = new ObjectMapper()
										.readValue(existingEntry.toJson(), Map.class);

								if (message.isTriggerWorkflow()) {
									entry.put("OLD_COPY", existingInputMap);

									Map<String, Object> inputMap = new ObjectMapper().readValue(entry.toJson(),
											Map.class);
									parent.executeModuleWorkflow(inputMap, companyId, message.getModuleId(), "UPDATE");
								}
							}
						}
					}
				}
			}
			log.trace("Exit DiscussionController.post()");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
