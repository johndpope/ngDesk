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

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import org.redisson.Redisson;
import org.redisson.api.RMap;
import org.redisson.api.RSortedSet;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
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
import com.ngdesk.discussion.DiscussionController;
import com.ngdesk.discussion.DiscussionMessage;
import com.ngdesk.nodes.ParentNode;

@Component
public class ClientSessionExpiry {

	private final Logger log = LoggerFactory.getLogger(ClientSessionExpiry.class);

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	Global global;

	@Autowired
	Environment environment;

	@Autowired
	private Environment env;

	@Autowired
	private SimpMessagingTemplate template;

	@Autowired
	RedissonClient redisson;

	@Autowired
	private SendMail sendMail;
	
	@Autowired
	private ParentNode parentNode;
	
	@Autowired
	DiscussionController discussionController;

	@Scheduled(fixedRate = 60000)
	public void executeJob() {
		log.trace("Enter ClientSessionExpiry.executeJob()");
		String subdomain = "";
		try {
			RSortedSet<Long> clientSessionsExpiryList = redisson.getSortedSet("clientSessionExpiryList");
			RMap<Long, Map<String, String>> sessionsToClear = redisson.getMap("clientSessionsExpiryMap");
			RMap<String, Long> sessionExpiryTimeMap = redisson.getMap("sessionToExpiryMap");

			String epochDate = "01/01/1970";
			Date date = new SimpleDateFormat("dd/MM/yyyy").parse(epochDate);
			Timestamp epoch = new Timestamp(date.getTime());

			Timestamp today = new Timestamp(new Date().getTime());
			long currentTimeDiff = today.getTime() - epoch.getTime();

			List<Long> timesToRemove = new ArrayList<Long>();
			
			for (Long timeDiff : clientSessionsExpiryList) {

				if (currentTimeDiff >= timeDiff) {
					Map<String, String> map = sessionsToClear.get(timeDiff);
					String companyId = map.get("COMPANY_ID");
					String moduleName = map.get("MODULE_NAME");
					String sessionUuid = map.get("SESSION_UUID");

					MongoCollection<Document> companiesCollection = mongoTemplate.getCollection("companies");
					Document company = companiesCollection.find(Filters.eq("_id", new ObjectId(companyId))).first();

					MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);

					Document module = modulesCollection.find(Filters.eq("NAME", moduleName)).first();

					if (module != null) {
						MongoCollection<Document> entriesCollection = mongoTemplate
								.getCollection(moduleName.replaceAll("\\s+", "_") + "_" + companyId);

						Document entry = entriesCollection.find(Filters.eq("SESSION_UUID", sessionUuid)).first();

						String moduleId = module.getObjectId("_id").toString();
						String companyUUID = company.getString("COMPANY_UUID");
						subdomain = company.getString("COMPANY_SUBDOMAIN");

						// Check if status is chatting and send notification for end chat

						if (entry != null && entry.containsKey("STATUS")) {

							String entryId = entry.getObjectId("_id").toString();
							if (entry.getString("STATUS").equalsIgnoreCase("chatting")) {

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
										.find(Filters.eq("_id", new ObjectId(entry.getString("REQUESTOR")))).first();

								String firstName = user.getString("FIRST_NAME");
								String lastName = user.getString("LAST_NAME");

								message = message.replace("FIRST_NAME_REPLACE", firstName);
								message = message.replace("LAST_NAME_REPLACE", lastName);

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
									discussionMessage.setEntryId(entryId);

									discussionController.post(discussionMessage);
								}

								// SEND META_DATA TO ALL PARTIES INVOLVED
								String confirmationTopic = "topic/chat/" + sessionUuid;
								this.template.convertAndSend(confirmationTopic, messageDoc.toJson().toString());

								// SEND THE NOTIFICATION AGENT FOR THE END CHAT
								MongoCollection<Document> notificationsCollection = mongoTemplate
										.getCollection("notifications_" + companyId);

								List<String> agents = new ArrayList<String>();
								if (entry.containsKey("AGENTS")) {
									agents = (List<String>) entry.get("AGENTS");
								}
								
								
								RMap<String, Map<String, Map<String, Object>>> companiesMap = redisson.getMap("companiesUsers");
								Map<String, Map<String, Object>> usersMap = companiesMap.get(subdomain);
								
								for (String agentId : agents) {
									
									// DECREMENT CHAT OF AGENT ASSIGNED BY 1
									
									int noOfChats = (int) usersMap.get(agentId).get("NO_OF_CHATS");
									noOfChats = noOfChats - 1;
									
									usersMap.get(agentId).put("NO_OF_CHATS", noOfChats);
									companiesMap.put(subdomain, usersMap);
									
									JSONObject notificationMessage = new JSONObject();
									notificationMessage.put("MESSAGE_TYPE", "NOTIFICATION");
									notificationMessage.put("DATE_CREATED",
											global.getFormattedDate(new Timestamp(new Date().getTime())));
									notificationMessage.put("DATA_ID", entryId);
									notificationMessage.put("MODULE_ID", moduleId);
									notificationMessage.put("MODULE_NAME", moduleName);
									notificationMessage.put("MESSAGE",
											firstName + " " + lastName + " " + "left the chat");
									notificationMessage.put("READ", false);
									notificationMessage.put("NOTIFICATION_UUID", UUID.randomUUID().toString());
									notificationMessage.put("RECEPIENT", agentId);

									this.template.convertAndSend("topic/notify/" + agentId,
											notificationMessage.toString());

									notificationsCollection.insertOne(Document.parse(notificationMessage.toString()));

								}
							}
						}
						entriesCollection.updateOne(Filters.eq("SESSION_UUID", sessionUuid),
								Updates.set("STATUS", "Offline"));
						
						
						
						
						
					}

					// REMOVE FROM EXPIRY MAP
					sessionExpiryTimeMap.remove(sessionUuid);

					// ADD IT TO LIST TO REMOVE FROM SORTED LIST AND MAP AT THE END
					timesToRemove.add(timeDiff);
				} else {
					break;
				}
			}

			for (Long timeDiff : timesToRemove) {
				sessionsToClear.remove(timeDiff);
				clientSessionsExpiryList.remove(timeDiff);
			}
			timesToRemove.clear();
		} catch (Exception e) {
			e.printStackTrace();

			String subject = "Call Failed on ClientSessionExpiry for " + subdomain;
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

		log.trace("Exit ClientSessionExpiry.executeJob()");
	}
}
