package com.ngdesk.nodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.jsoup.Jsoup;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.ngdesk.Global;
import com.ngdesk.discussion.DiscussionController;
import com.ngdesk.discussion.DiscussionMessage;
import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;

@Component
public class SendSms extends Node {

	private static final Logger logger = LogManager.getLogger(SendSms.class);

	@Autowired
	Global global;

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	private ParentNode parentNode;

	@Autowired
	DiscussionController discussionController;
	
	@Autowired
    RedissonClient client;
	
	@Value("${twillo.from.number}")
	private String fromNumber;


	@Override
	public Map<String, Object> executeNode(Document node, Map<String, Object> inputMessage) {
		logger.trace("Enter SendSms.executeNode()");
		Map<String, Object> resultMap = new HashMap<String, Object>();

		try {

			String moduleId = inputMessage.get("MODULE").toString();
			String dataId = inputMessage.get("DATA_ID").toString();
			Document company = global.getCompanyFromUUID(inputMessage.get("COMPANY_UUID").toString());

			if (company != null) {
				String companyId = company.getObjectId("_id").toString();

				Document values = (Document) node.get("VALUES");
				String to = values.getString("TO");

				String reg = "\\{\\{(?i)(inputMessage[_a-zA-Z\\.\\-]+)\\}\\}";
				Pattern r = Pattern.compile(reg);
				Matcher matcherTo = r.matcher(to);

				if (matcherTo.find()) {
					String path = matcherTo.group(1).split("(?i)inputMessage\\.")[1];
					String toValue = getValue(inputMessage, path, null);
					if (toValue != null) {
						to = to.replaceAll("\\{\\{" + matcherTo.group(1) + "\\}\\}", toValue);
					}
				}

				MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);
				Document user = usersCollection.find(Filters.eq("EMAIL_ADDRESS", to + "@twilio.com")).first();

				MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
				List<Document> roles = rolesCollection.find().into(new ArrayList<Document>());
				Map<String, String> rolesMap = new HashMap<String, String>();

				for (Document role : roles) {
					rolesMap.put(role.getObjectId("_id").toString(), role.getString("NAME"));
				}

				String userRole = "Customers";
				if (user != null) {
					userRole = rolesMap.get(user.getString("ROLE"));
				}

				String body = values.getString("BODY");
				Matcher matcherBody = r.matcher(body);
				while (matcherBody.find()) {
					String path = matcherBody.group(1).split("(?i)inputMessage\\.")[1];
					String value = getValue(inputMessage, path, userRole);
					if (value != null) {
						body = body.replaceAll("\\{\\{" + matcherBody.group(1) + "\\}\\}",
								Matcher.quoteReplacement(value));
					} else {
						body = value;
					}
				}
				// ADDED 'VIEW IT ON NGDESK' LINK
				String ticketLink = "https://" + company.getString("COMPANY_SUBDOMAIN")
						+ ".ngdesk.com/render/" + inputMessage.get("MODULE").toString() + "/detail/"
						+ inputMessage.get("DATA_ID").toString() + "";
				body += "\nTo view it on ngdesk, click: \n";
				body += ticketLink;

				if (body != null) {

					MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
					Document moduleDocument = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();

					MongoCollection<Document> roleCollection = mongoTemplate.getCollection("roles_" + companyId);
					HashMap<String, String> roleMap = new HashMap<String, String>();
					List<Document> roleList = roleCollection.find().into(new ArrayList<Document>());
					for (Document roleDocument : roleList) {
						roleMap.put(roleDocument.getString("NAME"), roleDocument.getObjectId("_id").toString());
					}

					String fromNumberSms = fromNumber;
					String channelId = null;

					if (inputMessage.containsKey("CHANNEL") && inputMessage.get("CHANNEL") != null) {
						channelId = inputMessage.get("CHANNEL").toString();
					}

					if (channelId != null && ObjectId.isValid(channelId)) {
						MongoCollection<Document> smsChannelCollection = mongoTemplate.getCollection("channels_sms");
						Document smsChannel = smsChannelCollection.find(Filters.eq("_id", new ObjectId(channelId)))
								.first();
						if (smsChannel != null) {
							fromNumberSms = smsChannel.getString("PHONE_NUMBER");
						}
					}

					if (new ObjectId().isValid(to)) {
						Document userDocument = usersCollection.find(Filters.eq("_id", new ObjectId(to))).first();
						if (userDocument != null) {
							if (userDocument.containsKey("PHONE_NUMBER")) {
								Document phoneNumberDoc = (Document) userDocument.get("PHONE_NUMBER");
								if (phoneNumberDoc != null) {
									to = phoneNumberDoc.getString("DIAL_CODE")
											+ phoneNumberDoc.getString("PHONE_NUMBER");
								}
							}
						}
					}

					try {
						Twilio.init(global.ACCOUNT_SID, global.AUTH_TOKEN);

						String sourceType = "web";

						if (inputMessage.containsKey("SOURCE_TYPE") && inputMessage.get("SOURCE_TYPE") != null) {
							sourceType = inputMessage.get("SOURCE_TYPE").toString();
						}

						if (sourceType.equals("whatsapp")) {
							Message msg = Message.creator(new com.twilio.type.PhoneNumber("whatsapp:" + to),
									new com.twilio.type.PhoneNumber("whatsapp:" + fromNumberSms), body).create();
						} else {
							Message msg = Message.creator(new com.twilio.type.PhoneNumber(to),
									new com.twilio.type.PhoneNumber(fromNumberSms), body).create();
						}

						Map<String, Object> smsMetadata = new HashMap<String, Object>();

						String systemUserUUID = global.getSystemUser(companyId);
						String companyUUID = inputMessage.get("COMPANY_UUID").toString();
						String message = global.getFile("metadata_sms.html");

						if (sourceType.equals("whatsapp")) {
							message = global.getFile("metadata_whatsapp.html");
						}

						smsMetadata.put("COMPANY_UUID", companyUUID);
						smsMetadata.put("MESSAGE_ID", UUID.randomUUID().toString());
						smsMetadata.put("USER_UUID", systemUserUUID);

						message = message.replace("PHONE_NUMBER_REPLACE", to);

						MongoCollection<Document> entriesCollection = mongoTemplate
								.getCollection(moduleDocument.getString("NAME") + "_" + companyId);

						List<Document> fields = (List<Document>) moduleDocument.get("FIELDS");
						String discussionFieldName = null;
						String discussionFieldId = null;
						for (Document field : fields) {
							Document dataType = (Document) field.get("DATA_TYPE");
							if (dataType.getString("DISPLAY").equals("Discussion")) {
								discussionFieldName = field.getString("NAME");
								discussionFieldId = field.getString("FIELD_ID");
								break;
							}
						}

						if (discussionFieldName != null) {
							
							// INSERT META_DATA INTO MESSAGES USING DISCUSSION CONTROLLER
							
							List<Map<String, Object>> discussion = global.buildDiscussionPayload(smsMetadata, message,
									"META_DATA");
							discussion.get(0).remove("DATE_CREATED");
							DiscussionMessage discussionMessage = new ObjectMapper().readValue(
									new ObjectMapper().writeValueAsString(discussion.get(0)).toString(),
									DiscussionMessage.class);
							discussionMessage.setSubdomain(company.getString("COMPANY_SUBDOMAIN"));
							discussionMessage.setModuleId(moduleId);
							discussionMessage.setEntryId(dataId);

							discussionController.post(discussionMessage);
						}
					} catch (ApiException e) {
						logger.debug("Invalid Number");
					}
				}
				logger.trace("SendSMS.executeNode() : META_DATA added to the messages");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		logger.trace("Exit SendSms.executeNode()");

		ArrayList<Document> connections = (ArrayList<Document>) node.get("CONNECTIONS_TO");
		if (connections.size() == 1) {
			Document connection = connections.get(0);
			resultMap.put("NODE_ID", connection.getString("TO_NODE"));
		}
		resultMap.put("INPUT_MESSAGE", inputMessage);
		return resultMap;
	}

	private String getValue(Map<String, Object> inputMessage, String path, String userRole) {
		try {

			String companyUUID = inputMessage.get("COMPANY_UUID").toString();
			String companyId = global.getCompanyId(companyUUID);
			Document company = global.getCompanyFromUUID(companyUUID);

			String moduleId = inputMessage.get("MODULE").toString();
			String dataId = inputMessage.get("DATA_ID").toString();

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			Document moduleDocument = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();

			List<Document> fields = (List<Document>) moduleDocument.get("FIELDS");
			Map<String, Document> relationFieldsMap = new HashMap<String, Document>();
			List<String> phoneDatatypes = new ArrayList<String>();

			String discussionField = null;
			for (Document field : fields) {
				String fieldName = field.getString("NAME");
				Document dataType = (Document) field.get("DATA_TYPE");

				if (dataType.getString("DISPLAY").equals("Relationship")) {

					if (field.getString("RELATIONSHIP_TYPE").equals("One to One")
							|| field.getString("RELATIONSHIP_TYPE").equals("Many to One")
							|| (field.getString("RELATIONSHIP_TYPE").equals("Many to Many")
									&& fieldName.equals("TEAMS"))) {

						relationFieldsMap.put(fieldName, field);
					}

				} else if (dataType.getString("DISPLAY").equals("Discussion")) {
					discussionField = field.getString("NAME");
				} else if (dataType.getString("DISPLAY").equals("Phone")) {
					phoneDatatypes.add(fieldName);
				}
			}

			String section = path.split("\\.")[0];
			if (relationFieldsMap.containsKey(section) && relationFieldsMap.get(section) != null) {
				Document field = relationFieldsMap.get(section);
				String relationModuleId = field.getString("MODULE");
				Document relationModule = modulesCollection.find(Filters.eq("_id", new ObjectId(relationModuleId)))
						.first();
				String value = inputMessage.get(section).toString();
				String primaryDisplayField = field.getString("PRIMARY_DISPLAY_FIELD");

				if (relationModule != null) {

					String id = relationModule.getObjectId("_id").toString();

					String fieldName = field.getString("NAME");

					if (fieldName.equals("TEAMS")) {

						List<String> teamIds = (List<String>) inputMessage.get(fieldName);
						List<ObjectId> teamObjectIds = new ArrayList<ObjectId>();

						for (String teamId : teamIds) {
							teamObjectIds.add(new ObjectId(teamId));
						}

						MongoCollection<Document> teamsCollection = mongoTemplate.getCollection("Teams_" + companyId);
						List<Document> teamsDocuments = teamsCollection.find(Filters.in("_id", teamObjectIds))
								.into(new ArrayList<Document>());

						List<String> userIds = new ArrayList<String>();

						for (Document team : teamsDocuments) {
							List<String> users = (List<String>) team.get("USERS");
							userIds.addAll(users);
						}

						List<ObjectId> userObjectIds = new ArrayList<ObjectId>();
						for (String userId : userIds) {
							userObjectIds.add(new ObjectId(userId));
						}

						MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);
						List<Document> userDocuments = usersCollection.find(Filters.in("_id", userObjectIds))
								.into(new ArrayList<Document>());

						List<String> emailIds = new ArrayList<String>();

						for (Document userDoc : userDocuments) {
							if (!userDoc.getBoolean("DELETED")) {
								emailIds.add(userDoc.getString("EMAIL_ADDRESS"));
							}
						}

						return new ObjectMapper().writeValueAsString(emailIds);

					} else {
						List<Document> relationFields = (List<Document>) relationModule.get("FIELDS");

						for (Document relationField : relationFields) {
							if (relationField.getString("FIELD_ID").equals(primaryDisplayField)) {
								relationField.getString("NAME");
								break;
							}
						}

						String relationModuleName = relationModule.getString("NAME");
						String entriesCollectionName = relationModuleName + "_" + companyId;

						MongoCollection<Document> entriesCollection = mongoTemplate
								.getCollection(entriesCollectionName);
						Document entry = entriesCollection.find(Filters.eq("_id", new ObjectId(value))).first();
						String entryId = entry.getObjectId("_id").toString();
						entry.remove("_id");
						Map<String, Object> newMap = new ObjectMapper().readValue(entry.toJson(), Map.class);
						newMap.put("DATA_ID", entryId);

						if (path.split("\\.").length > 1) {
							newMap.put("COMPANY_UUID", companyUUID);
							newMap.put("MODULE", id);
							return getValue(newMap, path.split(section + "\\.")[1], null);
						} else {
							return new ObjectMapper().writeValueAsString(newMap);
						}
					}
				}
			} else if (discussionField != null && discussionField.equals(section) && (path.split("\\.").length == 1
					|| (path.split("\\.").length == 2 && path.split("\\.")[1].equalsIgnoreCase("Latest")))) {
				boolean isLatest = false;
				if (path.split("\\.").length == 2 && path.split("\\.")[1].equalsIgnoreCase("Latest")) {
					isLatest = true;
				}

				if (inputMessage.containsKey(section) && inputMessage.get(section) != null) {

					String body = "";
					List<Map<String, Object>> messages = (List<Map<String, Object>>) inputMessage.get(section);
					MongoCollection<Document> attachmentsCollection = mongoTemplate
							.getCollection("attachments_" + companyId);
					mongoTemplate.getCollection("modules_" + companyId);

					for (int i = messages.size() - 1; i >= 0; i--) {

						Map<String, Object> message = messages.get(i);
						Map<String, Object> senderMap = (Map<String, Object>) message.get("SENDER");
						String messageType = message.get("MESSAGE_TYPE").toString();

						if (!isLatest && userRole.equals("Customers")
								&& messageType.equalsIgnoreCase("INTERNAL_COMMENT")) {
							continue;
						}

						// IGNORING META_DATA FROM BEING SENT
						if (!(messageType.equalsIgnoreCase("META_DATA"))) {

							String messageId = message.get("MESSAGE_ID").toString();
							String messageBody = Jsoup.parse(message.get("MESSAGE").toString()).text();
							body += messageBody;

							body += "\n";

							if (isLatest) {
								if (messageType.equals("INTERNAL_COMMENT") && userRole.equals("Customers")) {
									return null;
								}
								break;
							} else {
								body += "**********************************\n";
							}
						}
					}
					return body;
				}
			} else {
				if (path.split("\\.").length > 1) {
					Map<String, Object> newMap = (Map<String, Object>) inputMessage.get(section);
					newMap.put("COMPANY_UUID", companyUUID);
					newMap.put("MODULE", moduleId);
					newMap.put("DATA_ID", dataId);
					return getValue(newMap, path.split(section + "\\.")[1], null);
				} else {
					if (inputMessage.get(section) != null) {
						if (phoneDatatypes.contains(section) && inputMessage.get(section) != null) {
							Map<String, Object> phoneMap = (Map<String, Object>) inputMessage.get(section);
							String number = "";
							if (phoneMap.get("DIAL_CODE") != null && phoneMap.get("PHONE_NUMBER") != null) {
								number = phoneMap.get("DIAL_CODE").toString() + phoneMap.get("PHONE_NUMBER").toString();
							}
							return number;
						} else {
							return inputMessage.get(section).toString();
						}
					} else {
						return path;
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}