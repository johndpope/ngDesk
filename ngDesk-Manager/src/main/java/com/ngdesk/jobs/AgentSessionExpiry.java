package com.ngdesk.jobs;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.text.StringEscapeUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import org.redisson.api.RMap;
import org.redisson.api.RSortedSet;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.ngdesk.Global;
import com.ngdesk.SendMail;
import com.ngdesk.channels.chat.ChatChannelController;
import com.ngdesk.discussion.DiscussionController;
import com.ngdesk.discussion.DiscussionMessage;
import com.ngdesk.nodes.HttpRequestNode;
import com.ngdesk.nodes.ParentNode;
import com.ngdesk.nodes.PushNotification;

//@Component
public class AgentSessionExpiry {
	private final Logger log = LoggerFactory.getLogger(AgentSessionExpiry.class);

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	Global global;

	@Autowired
	Environment environment;

	@Autowired
	private HttpRequestNode httpRequestNode;

	@Autowired
	private SimpMessagingTemplate template;

	@Autowired
	ChatChannelController chatChannelController;

	@Autowired
	private ParentNode parentNode;

	@Autowired
	private SendMail sendMail;

	@Autowired
	private Environment env;

	@Autowired
	PushNotification browserNotification;

	@Autowired
	RedissonClient redisson;

	@Autowired
	DiscussionController discussionController;

	@Scheduled(fixedRate = 60000)
	public void executeJob() {
		log.trace("Enter AgentSessionExpiry.executeJob()");
		String subdomain = "";

		try {

			RMap<String, Long> agentSessionExpiryTimeMap = redisson.getMap("agentSessionExpiryTimeMap");
			RMap<Long, Map<String, String>> agentSessionsToClear = redisson.getMap("agentSessionsExpiryMap");
			RSortedSet<Long> agentSessionsExpiryList = redisson.getSortedSet("agentSessionExpiryList");

			String epochDate = "01/01/1970";
			Date date = new SimpleDateFormat("dd/MM/yyyy").parse(epochDate);
			Timestamp epoch = new Timestamp(date.getTime());
			Timestamp today = new Timestamp(new Date().getTime());
			long currentTimeDiff = today.getTime() - epoch.getTime();

			List<Long> timesToRemove = new ArrayList<Long>();

			for (Long timeDiff : agentSessionsExpiryList) {
				Map<String, String> map = agentSessionsToClear.get(timeDiff);

				String companySubdomain = map.get("COMPANY_SUBDOMAIN");
				String dataId = map.get("DATA_ID");

				if (currentTimeDiff >= timeDiff) {

					// MAKING AGENT OFFLINE
					RMap<String, Map<String, Map<String, Object>>> companiesMap = redisson.getMap("companiesUsers");
					Map<String, Map<String, Object>> usersMap = companiesMap.get(companySubdomain);
					usersMap.get(dataId).put("STATUS", "Offline");
					usersMap.get(dataId).put("NO_OF_CHATS", 0);
					companiesMap.put(companySubdomain, usersMap);

					Document company = global.getCompanyFromSubdomain(companySubdomain);
					if (company != null) {

						int maxChatsPerAgent = company.getInteger("MAX_CHATS_PER_AGENT");

						String companyId = company.getObjectId("_id").toString();
						subdomain = company.getString("COMPANY_SUBDOMAIN");
						String companyUUID = company.get("COMPANY_UUID").toString();

						MongoCollection<Document> modulesCollection = mongoTemplate
								.getCollection("modules_" + companyId);
						Document module = modulesCollection.find(Filters.eq("NAME", "Chat")).first();

						if (module == null) {
							continue;
						}

						String moduleId = module.getObjectId("_id").toString();

						MongoCollection<Document> entriesCollection = mongoTemplate.getCollection("Chat_" + companyId);
						List<Document> entries = entriesCollection
								.find(Filters.and(Filters.eq("STATUS", "Chatting"), Filters.in("AGENTS", dataId),
										Filters.eq("DELETED", false),
										Filters.or(Filters.eq("EFFECTIVE_TO", null),
												Filters.exists("EFFECTIVE_TO", false))))
								.into(new ArrayList<Document>());

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

						for (Document entry : entries) {
							String entryId = entry.getObjectId("_id").toString();
							List<String> agents = (List<String>) entry.get("AGENTS");

							JSONObject notifyMessage = new JSONObject();
							notifyMessage.put("MESSAGE_TYPE", "NOTIFICATION");
							notifyMessage.put("DATE_CREATED",
									global.getFormattedDate(new Timestamp(new Date().getTime())));
							notifyMessage.put("DATA_ID", entryId);
							notifyMessage.put("MODULE_ID", moduleId);
							notifyMessage.put("MODULE_NAME", module.getString("NAME"));

							// ADDING META DATA

							String message = global.getFile("metadata_chat_agent_left.html");
							Map<String, Object> metaDataMessage = new HashMap<String, Object>();
							String systemUserUUID = global.getSystemUser(companyId);

							MongoCollection<Document> usersCollection = mongoTemplate
									.getCollection("Users_" + companyId);
							Document user = usersCollection.find(Filters.eq("_id", new ObjectId(dataId))).first();

							String firstName = user.getString("FIRST_NAME");
							String lastName = user.getString("LAST_NAME");

							// NOTIFY END USER
							notifyMessage.put("MESSAGE", firstName + " " + lastName + " has left the chat");

							message = message.replace("FIRST_NAME_REPLACE", user.getString("FIRST_NAME"));
							message = message.replace("LAST_NAME_REPLACE", user.getString("LAST_NAME"));

							metaDataMessage.put("COMPANY_UUID", companyUUID);
							metaDataMessage.put("MESSAGE_ID", UUID.randomUUID().toString());
							metaDataMessage.put("USER_UUID", systemUserUUID);

							List<Map<String, Object>> builtMessage = global.buildDiscussionPayload(metaDataMessage,
									message, "META_DATA");
							Document messageDoc = Document
									.parse(new ObjectMapper().writeValueAsString(builtMessage.get(0)));
							if (discussionFieldName != null) {

								// INSERT META_DATA INTO MESSAGES USING DISCUSSION CONTROLLER
								builtMessage.get(0).remove("DATE_CREATED");
								DiscussionMessage discussionMessage = new ObjectMapper().readValue(
										new ObjectMapper().writeValueAsString(builtMessage.get(0)).toString(),
										DiscussionMessage.class);
								discussionMessage.setSubdomain(company.getString("COMPANY_SUBDOMAIN"));
								discussionMessage.setModuleId(moduleId);
								discussionMessage.setEntryId(dataId);

								discussionController.post(discussionMessage);

							}
							// SEND META_DATA TO ALL PARTIES INVOLVED

							String confirmationTopic = "topic/chat/" + entry.get("SESSION_UUID").toString();
							this.template.convertAndSend(confirmationTopic, messageDoc.toJson().toString());

							MongoCollection<Document> notificationsCollection = mongoTemplate
									.getCollection("notifications_" + companyId);

							String userTopic = "topic/notify/" + entry.get("SESSION_UUID").toString();
							this.template.convertAndSend(userTopic, notifyMessage.toString());

							for (String userId : agents) {
								if (userId.equals(dataId)) {
									continue;

								}
								String agentTopic = "topic/notify/" + userId;
								notifyMessage.put("READ", false);
								notifyMessage.put("NOTIFICATION_UUID", UUID.randomUUID().toString());
								notifyMessage.put("RECEPIENT", userId);
								notificationsCollection.insertOne(Document.parse(notifyMessage.toString()));
								this.template.convertAndSend(agentTopic, notifyMessage.toString());

								// FIREBASE PUSH NOTIFICATION
								MongoCollection<Document> tokenCollection = mongoTemplate
										.getCollection("user_tokens_" + companyId);
								Document agentDoc = usersCollection.find(Filters.eq("_id", new ObjectId(userId)))
										.first();
								Document userDoc = tokenCollection
										.find(Filters.eq("USER_UUID", agentDoc.getString("USER_UUID"))).first();
								String url = "https://" + companySubdomain + ".ngdesk.com/render/" + moduleId
										+ "/detail/" + entryId + "";
								browserNotification.sendWebNotifications(userDoc,
										firstName + " " + lastName + " has left the chat", companySubdomain, url);
							}
							// CHECK IF ANY OTHER AGENT IS AVAILABLE

							boolean agentExists = chatChannelController.checkIfAgentExists(companySubdomain, companyId,
									module, maxChatsPerAgent);
							if (agentExists) {

								// IF ANY OTHER AGENT IS AVAILABLE, START THE WORKFLOW
								String transferChatWorkflowFile = global.getFile("TransferChatWorkflow.json");
								transferChatWorkflowFile = transferChatWorkflowFile.replaceAll("MODULE_ID", moduleId);
								Document transferChatWorkflowDocument = Document.parse(transferChatWorkflowFile);

								// FETCHING CHANNEL NAME
								String channelId = entry.getString("CHANNEL");
								MongoCollection<Document> chatChannelCollection = mongoTemplate
										.getCollection("channels_chat_" + companyId);
								Document chatChannel = chatChannelCollection
										.find(Filters.eq("_id", new ObjectId(channelId))).first();
								String channelName = chatChannel.getString("NAME");

								// INPUT MAP
								entry.remove("_id");
								Map<String, Object> inputMap = new ObjectMapper().readValue(entry.toJson(), Map.class);
								inputMap.put("DATA_ID", entryId);
								inputMap.put("TYPE", "chat");
								inputMap.put("CHANNEL_NAME", channelName);
								inputMap.put("MODULE", moduleId);
								inputMap.put("COMPANY_UUID", companyUUID);
								inputMap.put("USER_UUID", user.get("USER_UUID"));

								// TRIGGER THE WORKFLOW TO TRANSFER CHAT TO ANOTHER AGENT
								if (transferChatWorkflowDocument != null) {
									Document transferChatWorkflow = (Document) transferChatWorkflowDocument
											.get("WORKFLOW");
									if (transferChatWorkflow.containsKey("NODES")) {
										ArrayList<Document> nodeDocuments = (ArrayList<Document>) transferChatWorkflow
												.get("NODES");
										if (nodeDocuments != null && nodeDocuments.size() > 0) {
											Document firstNode = nodeDocuments.get(0);
											if ("Start".equals(firstNode.getString("TYPE"))) {
												log.trace("parentNode.executeWorkflow()");
												parentNode.executeWorkflow(firstNode, nodeDocuments, inputMap);
											}
										}
									}
								}
							} else {
								// NO OTHER AGENT IS AVAILABLE
								// CHAGING STATUS BACK TO BROWSING
								entriesCollection.updateOne(Filters.eq("_id", new ObjectId(entryId)),
										Updates.set("STATUS", "Browsing"));

							}
						}
					}

					// REMOVE FROM EXPIRY MAP
					agentSessionExpiryTimeMap.remove(dataId);

					// ADD IT TO LIST TO REMOVE FROM SORTED LIST AND MAP AT THE END
					timesToRemove.add(timeDiff);

				} else {
					break;
				}
			}
			for (Long timeDiff : timesToRemove) {
				agentSessionsToClear.remove(timeDiff);
				agentSessionsExpiryList.remove(timeDiff);
			}
			timesToRemove.clear();
		} catch (Exception e) {
			e.printStackTrace();

			String subject = "Call Failed on AgentSessionExpiry for " + subdomain;
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			String sStackTrace = sw.toString();
			sStackTrace += "<br/><br/>" + sStackTrace;
			String environment = env.getProperty("env");
			if (environment.equals("prd")) {
				sendMail.send("spencer@allbluesolutions.com", "support@ngdesk.com", subject, sStackTrace);
				sendMail.send("shashank@allbluesolutions.com", "support@ngdesk.com", subject, sStackTrace);
			}

		}
		log.trace("Exit AgentSessionExpiry.executeJob()");
	}
}
