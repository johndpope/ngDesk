package com.ngdesk.nodes;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import org.json.JSONArray;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.ngdesk.Global;
import com.ngdesk.SendMail;
import com.ngdesk.createuser.CreateUserController;
import com.ngdesk.data.dao.DataService;
import com.ngdesk.data.dao.PublishDiscussionMessage;
import com.ngdesk.discussion.DiscussionController;
import com.ngdesk.discussion.DiscussionMessage;

@Component
public class SendEmail extends Node {
	private static final Logger logger = LogManager.getLogger(SendEmail.class);

	@Autowired
	SendMail sendMail;

	@Autowired
	CreateUserController createUserController;

	@Autowired
	Global global;

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	private ParentNode parentNode;

	@Autowired
	DiscussionController discussionController;

	@Autowired
	RedissonClient redisson;
	
	@Autowired
	DataService dataService;

	private final org.slf4j.Logger log = LoggerFactory.getLogger(SendEmail.class);

	@Override
	public Map<String, Object> executeNode(Document node, Map<String, Object> inputMessage) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		boolean isNotInternalComment = true;
		logger.trace("Enter SendEmail.executeNode()");

		try {
			// GET VALUES OF NODE
			Document values = (Document) node.get("VALUES");

			// GET ALL REQUIRED INFORMATION FROM VALUES
			String to = values.getString("TO");
			String from = values.getString("FROM");
			String subject = values.getString("SUBJECT");

			String body = values.getString("BODY");
			JSONArray toEmails = new JSONArray();
			// META_DATA Message
			new SimpleDateFormat("MMM d y h:mm:ss a").format(new Timestamp(new Date().getTime()));
			boolean messageSent = false;
			String message = global.getFile("metadata_message.html");
			String emailToReplace = "";
			String companyUUID = inputMessage.get("COMPANY_UUID").toString();
			String companyId = global.getCompanyId(companyUUID);
			String moduleId = inputMessage.get("MODULE").toString();
			String dataId = inputMessage.get("DATA_ID").toString();

			HashMap<String, Object> oldEntry = (HashMap<String, Object>) inputMessage.get("OLD_COPY");

			MongoCollection<Document> roleCollection = mongoTemplate.getCollection("roles_" + companyId);
			MongoCollection<Document> userCollection = mongoTemplate.getCollection("Users_" + companyId);

			HashMap<String, String> roleMap = new HashMap<String, String>();
			HashMap<String, String> toEmailMap = new HashMap<String, String>();
			List<Document> roleList = roleCollection.find().into(new ArrayList<Document>());
			for (Document d : roleList) {
				roleMap.put(d.getString("NAME"), d.getObjectId("_id").toString());
			}

			Document company = global.getCompanyFromUUID(companyUUID);

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			Document moduleDocument = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();

			// GET DISCUSSION FIELD
			List<Document> fields = (List<Document>) moduleDocument.get("FIELDS");
			String discussionFieldName = null;
			String discussionFieldId = null;
			for (Document field : fields) {
				String fieldName = field.getString("NAME");
				Document dataType = (Document) field.get("DATA_TYPE");
				if (dataType.getString("DISPLAY").equals("Discussion")) {
					discussionFieldName = field.getString("NAME");
					discussionFieldId = field.getString("FIELD_ID");
					break;
				}
			}
			MongoCollection<Document> channelsCollection = mongoTemplate.getCollection("channels_email_" + companyId);

			// CHECK SPF RECORD FOR EXTERNAL EMAIL CHANNEL AND USE IT AS FROM EMAIL ADDRESS
//			String moduleName = moduleDocument.getString("NAME");
//			MongoCollection<Document> entriesCollection = mongoTemplate.getCollection(moduleName + "_" + companyId);
//			Document entry = entriesCollection.find(Filters.eq("_id", new ObjectId(dataId))).first();
//			if (entry.containsKey("CHANNEL")) {
//				String ticketChannelId = entry.getString("CHANNEL");
//				Document ticketChannel = channelsCollection.find(Filters.eq("_id", new ObjectId(ticketChannelId)))
//						.first();
//				if (ticketChannel.getString("TYPE").equals("External") && ticketChannel.getBoolean("IS_VERIFIED")) {
//					String ticketChannelEmailAddress = ticketChannel.getString("EMAIL_ADDRESS");
//					MongoCollection<Document> spfRecordCollection = mongoTemplate
//							.getCollection("spf_records_" + companyId);
//					Document spfRecord = spfRecordCollection
//							.find(Filters.eq("EMAIL_ADDRESS", ticketChannelEmailAddress)).first();
//					if (spfRecord != null) {
//						from = ticketChannelEmailAddress;
//					}
//				}
//			}

			if (moduleDocument != null) {

				MongoCollection<Document> blackListWhiteListCollection = mongoTemplate
						.getCollection("blacklisted_whitelisted_emails_" + companyId);

				// TO
				String reg = "\\{\\{(?i)(inputMessage[_a-zA-Z\\.\\-]+)\\}\\}";
				Pattern r = Pattern.compile(reg);
				Matcher matcherTo = r.matcher(to);

				if (matcherTo.find()) {
					String path = matcherTo.group(1).split("(?i)inputMessage\\.")[1];
					String toValue = getValue(inputMessage, path);
					if (toValue != null) {
						to = to.replaceAll("\\{\\{" + matcherTo.group(1) + "\\}\\}", toValue);
					}
				}

				if (to != null) {
					try {
						toEmails = new JSONArray(to);
					}

					catch (Exception e) {
						logger.debug("To address not an array");
					}
				}
				// FROM
				Matcher matcherFrom = r.matcher(from);
				if (matcherFrom.find()) {
					String path = matcherFrom.group(1).split("(?i)inputMessage\\.")[1];
					String fromValue = getValue(inputMessage, path);

					if (fromValue != null) {
						from = from.replaceAll("\\{\\{" + matcherFrom.group(1) + "\\}\\}", fromValue);
					}
				}

				// SUBJECT
				Matcher matcherSubject = r.matcher(subject);
				while (matcherSubject.find()) {
					String path = matcherSubject.group(1).split("(?i)inputMessage\\.")[1];
					String value = getValue(inputMessage, path);
					if (value != null) {
						subject = subject.replaceAll("\\{\\{" + matcherSubject.group(1) + "\\}\\}",
								Matcher.quoteReplacement(value));
					}
				}

				// BODY
				Matcher matcherBody = r.matcher(body);
				while (matcherBody.find()) {
					String path = matcherBody.group(1).split("(?i)inputMessage\\.")[1];
					String value = getValue(inputMessage, path);

					if (value != null) {
						body = body.replaceAll("\\{\\{" + matcherBody.group(1) + "\\}\\}",
								Matcher.quoteReplacement(value));

						// FILTERING AND RESTRICTING INTERNAL COMMENT FOR CUSTOMERS AND PUBLIC BEING THE
						// REQUESTER
						Document userDocument = userCollection
								.find(Filters.and(Filters.eq("EMAIL_ADDRESS", to), Filters.eq("DELETED", false), Filters
										.or(Filters.eq("EFFECTIVE_TO", null), Filters.exists("EFFECTIVE_TO", false))))
								.first();
						String toEmailRole = "";

						if (userDocument != null) {
							toEmailRole = userDocument.getString("ROLE");

						}

						if (discussionFieldName != null) {
							if ((!toEmailRole.equals(roleMap.get("SystemAdmin"))
									&& !toEmailRole.equals(roleMap.get("Agent"))) || userDocument == null) {

								int index = ((List<HashMap<String, Object>>) inputMessage.get(discussionFieldName))
										.size();
								List<HashMap<String, Object>> latestMessages = (ArrayList<HashMap<String, Object>>) inputMessage
										.get(discussionFieldName);

								HashMap<String, Object> latest = latestMessages.get(index - 1);
								String internalCommentRegex = "<table class=\"INTERNAL_COMMENT\"(.*?)<hr\\/>";
								Pattern p = Pattern.compile(internalCommentRegex, Pattern.DOTALL);

								if (latest.get("MESSAGE_TYPE").equals("INTERNAL_COMMENT")) {
									isNotInternalComment = false;
								} else {
									Matcher matchermailBody = p.matcher(body);
									while (matchermailBody.find()) {
										body = body.replace(matchermailBody.group(0), "");
									}
								}
							}
						}
					}
				}

				if (toEmails != null && toEmails.length() > 0) {
					for (int i = 0; i < toEmails.length(); i++) {
						isNotInternalComment = true;
						String toEmail = "";

						try {
							toEmail = toEmails.getString(i);
							Document userDocument = userCollection.find(Filters.eq("EMAIL_ADDRESS", toEmail)).first();
							String toEmailRole = "";

							if (userDocument != null) {
								toEmailRole = userDocument.getString("ROLE");

							}
							// FILTERING AND RESTRICTING INTERNAL COMMENT FOR CUSTOMERS AND PUBLIC FOR
							// TO_CC_EMAILS
							if (discussionFieldName != null) {
								if ((!toEmailRole.equals(roleMap.get("SystemAdmin"))
										&& !toEmailRole.equals(roleMap.get("Agent"))) || userDocument == null) {

									int index = ((List<HashMap<String, Object>>) inputMessage.get(discussionFieldName))
											.size();
									List<HashMap<String, Object>> latestMessages = (ArrayList<HashMap<String, Object>>) inputMessage
											.get(discussionFieldName);

									HashMap<String, Object> latest = latestMessages.get(index - 1);
									String internalCommentRegex = "<table class=\"INTERNAL_COMMENT\"(.*?)<hr\\/>";
									Pattern p = Pattern.compile(internalCommentRegex, Pattern.DOTALL);

									if (latest.get("MESSAGE_TYPE").equals("INTERNAL_COMMENT")) {
										isNotInternalComment = false;
									} else {
										Matcher matchermailBody = p.matcher(body);
										while (matchermailBody.find()) {
											body = body.replace(matchermailBody.group(0), "");
										}
									}
								}
							}
							// META DATA
							if (i == 0) {
								emailToReplace = emailToReplace + "<span>" + toEmail + "</span>";
							} else {
								emailToReplace = emailToReplace + ", <span>" + toEmail + "</span>";
							}

							Document channel = channelsCollection.find(Filters.eq("EMAIL_ADDRESS", toEmail)).first();
							if (channel == null) {

								String entryRegex = "ENTRY_ID: ([a-z0-9]{24})";
								String entryId = createUserController.getIdFromMessage(entryRegex, body);

								if (entryId == null) {
									body += "<br/><br/>*****************************************************************";
									body += "<br/>" + "Do not modify the following content: <br/>" + "ENTRY_ID: "
											+ inputMessage.get("DATA_ID").toString() + "<br/>" + "MODULE_ID: "
											+ inputMessage.get("MODULE").toString() + "<br/>" + "COMPANY_SUBDOMAIN: "
											+ company.getString("COMPANY_SUBDOMAIN").toLowerCase();
								}

								String domain = null;

								// CHECKING WHITELIST
								Document whiteListedEmail = blackListWhiteListCollection
										.find(Filters.and(Filters.eq("TYPE", "OUTGOING"),
												Filters.eq("EMAIL_ADDRESS", toEmail),
												Filters.eq("STATUS", "WHITELIST")))
										.first();

								Document whitelistedDomain = null;

								if (to.contains("@")) {
									domain = to.split("@")[1];
									domain = domain.trim().toLowerCase();

									whitelistedDomain = blackListWhiteListCollection
											.find(Filters.and(Filters.eq("TYPE", "OUTGOING"),
													Filters.eq("STATUS", "WHITELIST"),
													Filters.eq("EMAIL_ADDRESS", domain), Filters.eq("IS_DOMAIN", true)))
											.first();
								}

								// CHECKING BLACKLIST

								Document blacklistedEmail = blackListWhiteListCollection
										.find(Filters.and(Filters.eq("TYPE", "OUTGOING"),
												Filters.eq("EMAIL_ADDRESS", toEmail),
												Filters.eq("STATUS", "BLACKLIST")))
										.first();

								Document blacklistedDomain = null;

								if (to.contains("@")) {
									domain = to.split("@")[1];
									domain = domain.trim().toLowerCase();

									blacklistedDomain = blackListWhiteListCollection
											.find(Filters.and(Filters.eq("TYPE", "OUTGOING"),
													Filters.eq("STATUS", "BLACKLIST"),
													Filters.eq("EMAIL_ADDRESS", domain), Filters.eq("IS_DOMAIN", true)))
											.first();
								}
								boolean addToJob = true;
								if (whitelistedDomain != null || whiteListedEmail != null) {
									addToJob = false;
								}
								if (isNotInternalComment) {
									if (blacklistedEmail == null && blacklistedDomain == null) {
										messageSent = sendMail.send(toEmail, from, subject, body);
										if (messageSent && addToJob) {
											Timestamp currentTimestamp = new Timestamp(new Date().getTime());

											RMap<String, Map<String, List<Timestamp>>> outgoingMails = redisson
													.getMap("outgoingMails");
											if (!outgoingMails.containsKey(companyId)) {
												outgoingMails.put(companyId, new HashMap<String, List<Timestamp>>());
											}

											if (outgoingMails.get(companyId).containsKey(toEmail)) {
												outgoingMails.get(companyId).get(toEmail).add(currentTimestamp);
											} else {
												List<Timestamp> timestamps = new ArrayList<Timestamp>();
												timestamps.add(currentTimestamp);
												outgoingMails.get(companyId).put(toEmail, timestamps);
											}
										}
									}
								} else {
									messageSent = false;
								}

							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

				} else {

					// META_DATA
					emailToReplace = emailToReplace + "<span>" + to + "</span>";

					Document channel = channelsCollection.find(Filters.eq("EMAIL_ADDRESS", to)).first();
					if (channel == null) {

						String entryRegex = "ENTRY_ID: ([a-z0-9]{24})";
						String entryId = createUserController.getIdFromMessage(entryRegex, body);

						if (entryId == null) {
							body += "<br/><br/>*****************************************************************";
							body += "<br/>" + "Do not modify the following content: <br/>" + "ENTRY_ID: "
									+ inputMessage.get("DATA_ID").toString() + "<br/>" + "MODULE_ID: "
									+ inputMessage.get("MODULE").toString() + "<br/>" + "COMPANY_SUBDOMAIN: "
									+ company.getString("COMPANY_SUBDOMAIN").toLowerCase();

						}

						String domain = null;
						// CHECK WHITELIST

						Document whiteListedEmail = blackListWhiteListCollection
								.find(Filters.and(Filters.eq("TYPE", "OUTGOING"), Filters.eq("EMAIL_ADDRESS", to),
										Filters.eq("STATUS", "WHITELIST")))
								.first();

						Document whiteListedDomain = null;

						if (to.contains("@")) {
							domain = to.split("@")[1];
							domain = domain.trim();

							whiteListedDomain = blackListWhiteListCollection.find(
									Filters.and(Filters.eq("TYPE", "OUTGOING"), Filters.eq("EMAIL_ADDRESS", domain),
											Filters.eq("IS_DOMAIN", true), Filters.eq("STATUS", "WHITELIST")))
									.first();
						}

						// CHECK BLACKLIST

						Document blacklistedDomain = null;

						if (to.contains("@")) {
							domain = to.split("@")[1];
							domain = domain.trim();

							blacklistedDomain = blackListWhiteListCollection.find(
									Filters.and(Filters.eq("TYPE", "OUTGOING"), Filters.eq("EMAIL_ADDRESS", domain),
											Filters.eq("IS_DOMAIN", true), Filters.eq("STATUS", "BLACKLIST")))
									.first();
						}

						Document blacklistedEmail = blackListWhiteListCollection
								.find(Filters.and(Filters.eq("TYPE", "OUTGOING"), Filters.eq("EMAIL_ADDRESS", to),
										Filters.eq("STATUS", "BLACKLIST")))
								.first();

						boolean addToJob = true;
						if (whiteListedDomain != null || whiteListedEmail != null) {
							addToJob = false;
						}
						if (isNotInternalComment) {
							if (blacklistedEmail == null && blacklistedDomain == null) {
								messageSent = sendMail.send(to, from, subject, body);
								if (messageSent && addToJob) {
									Timestamp currentTimestamp = new Timestamp(new Date().getTime());

									RMap<String, Map<String, List<Timestamp>>> outgoingMails = redisson
											.getMap("outgoingMails");
									if (!outgoingMails.containsKey(companyId)) {
										outgoingMails.put(companyId, new HashMap<String, List<Timestamp>>());
									}

									if (outgoingMails.get(companyId).containsKey(to)) {
										outgoingMails.get(companyId).get(to).add(currentTimestamp);
									} else {
										List<Timestamp> timestamps = new ArrayList<Timestamp>();
										timestamps.add(currentTimestamp);
										outgoingMails.get(companyId).put(to, timestamps);
									}
								}
							} else {
								messageSent = false;

							}
						}
					}
				}
			}
			// INPUT FOR DISCUSSION MESSAGE BUILDER - META_DATA ONLY FOR MESSAGES TYPE
			if (discussionFieldName != null) {
				int index = ((List<HashMap<String, Object>>) inputMessage.get(discussionFieldName)).size();
				List<HashMap<String, Object>> latestMessages = (ArrayList<HashMap<String, Object>>) inputMessage
						.get(discussionFieldName);

				HashMap<String, Object> latest = latestMessages.get(index - 1);
				String internalCommentRegex = "<table class=\"INTERNAL_COMMENT\"(.*?)<hr\\/>";
				Pattern p = Pattern.compile(internalCommentRegex, Pattern.DOTALL);

				if (messageSent && !latest.get("MESSAGE_TYPE").equals("INTERNAL_COMMENT")) {
					Map<String, Object> inputMessage1 = new HashMap<String, Object>();

					String systemUserUUID = global.getSystemUser(companyId);

					inputMessage1.put("COMPANY_UUID", companyUUID);
					inputMessage1.put("MESSAGE_ID", UUID.randomUUID().toString());
					inputMessage1.put("USER_UUID", systemUserUUID);

					message = message.replace("EMAIL_IDS_REPLACE", emailToReplace);
					message = message.replaceAll("[\\n\\t]", " ");

					// INSERT META_DATA INTO MESSAGES USING DISCUSSION CONTROLLER
					List<Map<String, Object>> discussion = global.buildDiscussionPayload(inputMessage1, message,
							"META_DATA");
					discussion.get(0).remove("DATE_CREATED");
					DiscussionMessage discussionMessage = new ObjectMapper().readValue(
							new ObjectMapper().writeValueAsString(discussion.get(0)).toString(),
							DiscussionMessage.class);
					discussionMessage.setSubdomain(company.getString("COMPANY_SUBDOMAIN"));
					discussionMessage.setModuleId(moduleId);
					discussionMessage.setEntryId(dataId);

					String systemAdminUserId = dataService.generateSystemUserEntry(companyId).getObjectId("_id").toString();
					dataService.addToDiscussionQueue(new PublishDiscussionMessage(discussionMessage,
							company.getString("COMPANY_SUBDOMAIN"), systemAdminUserId, true));

					logger.trace("SendEmail.executeNode() : META_DATA added to the messages");
				}
			}
		} catch (

		Exception e) {
			e.printStackTrace();
		}
		ArrayList<Document> connections = (ArrayList<Document>) node.get("CONNECTIONS_TO");
		// EMAIL NODE HAS ONLY ONE CONNECTION
		if (connections.size() == 1) {
			Document connection = connections.get(0);
			resultMap.put("NODE_ID", connection.getString("TO_NODE"));
		}
		resultMap.put("INPUT_MESSAGE", inputMessage);
		logger.trace("Exit SendEmail.executeNode()");
		
		return resultMap;
	}

	private String getValue(Map<String, Object> inputMessage, String path) {
		try {
			String companyUUID = inputMessage.get("COMPANY_UUID").toString();
			String companyId = global.getCompanyId(companyUUID);
			Document company = global.getCompanyFromUUID(companyUUID);
			String subdomain = company.getString("COMPANY_SUBDOMAIN");
			String companyName = company.getString("COMPANY_NAME");

			String moduleId = inputMessage.get("MODULE").toString();
			String dataId = inputMessage.get("DATA_ID").toString();

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			Document moduleDocument = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();

			List<Document> fields = (List<Document>) moduleDocument.get("FIELDS");
			Map<String, Document> relationFieldsMap = new HashMap<String, Document>();
			String discussionField = null;
			List<String> phoneDatatypes = new ArrayList<String>();
			List<String> dateTimeDatatypes = new ArrayList<String>();
			List<String> dateDatatypes = new ArrayList<String>();
			List<String> timeDatatypes = new ArrayList<String>();
			List<String> chronometers = new ArrayList<String>();

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
				} else if (dataType.getString("DISPLAY").equals("Date/Time")) {
					dateTimeDatatypes.add(fieldName);
				} else if (dataType.getString("DISPLAY").equals("Date")) {
					dateDatatypes.add(fieldName);
				} else if (dataType.getString("DISPLAY").equals("Time")) {
					timeDatatypes.add(fieldName);
				} else if (dataType.getString("DISPLAY").equals("Chronometer")) {
					chronometers.add(fieldName);
				}
			}

			String section = path.split("\\.")[0];
			if (relationFieldsMap.containsKey(section) && relationFieldsMap.get(section) != null) {
				Document field = relationFieldsMap.get(section);
				String relationModuleId = field.getString("MODULE");
				Document relationModule = modulesCollection.find(Filters.eq("_id", new ObjectId(relationModuleId)))
						.first();
				if (!inputMessage.containsKey(section) || inputMessage.get(section) == null) {
					return "";
				}

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
							return getValue(newMap, path.split(section + "\\.")[1]);
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
					String body = "<p style=\"color:#b5b5b5; font-family: Lucida Grande, Verdana, Arial, sans-serif, serif, EmojiFont; font-size: 12px;\">##-- "
							+ global.getTranslation(company.getString("LANGUAGE"), "REPLY_ABOVE") + " --##</p>";
					List<Map<String, Object>> messages = (List<Map<String, Object>>) inputMessage.get(section);
					MongoCollection<Document> attachmentsCollection = mongoTemplate
							.getCollection("attachments_" + companyId);
					mongoTemplate.getCollection("modules_" + companyId);

					for (int i = messages.size() - 1; i >= 0; i--) {

						Map<String, Object> message = messages.get(i);
						Map<String, Object> senderMap = (Map<String, Object>) message.get("SENDER");
						String messageRoleId = senderMap.get("ROLE").toString();
						String messageType = message.get("MESSAGE_TYPE").toString();
						// IGNORING META_DATA FROM BEING SENT
						if (!(messageType.equalsIgnoreCase("META_DATA"))) {

							String messageBody = message.get("MESSAGE").toString();
							Pattern pattern = Pattern.compile("<body>(.*?)<\\/body>");
							Matcher matcher = pattern.matcher(messageBody);

							if (matcher.find()) {
								String match = matcher.group(1);
								match = match.replaceAll("\n", "<br/>");

								messageBody = messageBody.replaceAll("<body>(.*?)<\\/body>",
										Matcher.quoteReplacement("<body>" + match + "</body>"));
							}

							String messageId = message.get("MESSAGE_ID").toString();
							String senderFirstName = senderMap.get("FIRST_NAME").toString();

							String senderLastName = null;
							if (senderMap.containsKey("LAST_NAME")) {
								senderLastName = senderMap.get("LAST_NAME").toString();
							}

							// FOR DATE IN DIFFERENT FORMAT HARD CODE FIX
							// TODO: REVISIT
							Date parsedDate = new Date((long) message.get("DATE_CREATED"));

							Timestamp dateCreated = new Timestamp(parsedDate.getTime());

							String formattedDateCreated = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss")
									.format(dateCreated);
							String messageHTML = global.getFile("email_message.html");
							if (messageType.equalsIgnoreCase("INTERNAL_COMMENT")) {
								messageHTML = global.getFile("email_internal_comment.html");
							}

							messageHTML = messageHTML.replaceAll("FIRST_NAME", senderFirstName);
							messageHTML = messageHTML.replaceAll("LAST_NAME", senderLastName);
							messageHTML = messageHTML.replaceAll("COMPANY_NAME", Matcher.quoteReplacement(companyName));
							messageHTML = messageHTML.replaceAll("DATE_AND_TIME", formattedDateCreated);
							messageHTML = messageHTML.replaceAll("MESSAGE_REPLACE",
									Matcher.quoteReplacement(messageBody));

							body += messageHTML;
							List<Document> allAttachments = new ArrayList<Document>();

							if (message.containsKey("ATTACHMENTS") && message.get("ATTACHMENTS") != null) {
								List<Map<String, Object>> attachments = (List<Map<String, Object>>) message
										.get("ATTACHMENTS");
								for (Map<String, Object> attachment : attachments) {
									Document actualAttachment = attachmentsCollection
											.find(Filters.eq("HASH", attachment.get("HASH").toString())).first();
									if (actualAttachment != null) {
										actualAttachment.put("FILE_NAME", attachment.get("FILE_NAME").toString());
										allAttachments.add(actualAttachment);
									}
								}
							}

							if (allAttachments.size() > 0) {
								body += "<br/>";
								body += "Attachments: <br/>";
							}

							for (Document attachment : allAttachments) {
								String uuid = attachment.get("ATTACHMENT_UUID").toString();
								String filename = attachment.get("FILE_NAME").toString();
								body += "<a target=\"_blank\" href=\"https://" + subdomain
										+ ".ngdesk.com/ngdesk-rest/ngdesk/attachments?attachment_uuid=" + uuid
										+ "&message_id=" + messageId + "&entry_id=" + dataId + "&module_id=" + moduleId
										+ "\">" + filename + "</a><br/>";

							}

							body += "<br/><hr/>";

							if (i == 0) {
								// TODO: NOT NEEDED
								String ticketLink = "<a href=\"https://" + company.getString("COMPANY_SUBDOMAIN")
										+ ".ngdesk.com/render/" + inputMessage.get("MODULE").toString() + "/edit/"
										+ inputMessage.get("DATA_ID").toString() + "\"> View it on ngdesk </a>";
								body += ticketLink;
								body += "<br/><br/>";
								body += "*****************************************************************";
								body += "<br/>" + "Do not modify the following content: <br/>" + "ENTRY_ID: "
										+ inputMessage.get("DATA_ID").toString() + "<br/>" + "MODULE_ID: "
										+ inputMessage.get("MODULE").toString() + "<br/>" + "COMPANY_SUBDOMAIN: "
										+ subdomain;
							}
							if (isLatest) {
								break;
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
					return getValue(newMap, path.split(section + "\\.")[1]);
				} else {
					if (inputMessage.get(section) != null) {
						SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
						if (phoneDatatypes.contains(section) && inputMessage.get(section) != null) {
							Map<String, Object> phoneMap = (Map<String, Object>) inputMessage.get(section);
							String number = "";
							if (phoneMap.get("DIAL_CODE") != null && phoneMap.get("PHONE_NUMBER") != null) {
								number = phoneMap.get("DIAL_CODE").toString() + phoneMap.get("PHONE_NUMBER").toString();
							}
							return number;
						} else if (dateTimeDatatypes.contains(section) && inputMessage.get(section) != null) {
							// CHANGE DATE FORMAT IF FIELD IS DATE/TIME
							Date parsedDate = dateFormat.parse(inputMessage.get(section).toString());
							Timestamp currentValue = new Timestamp(parsedDate.getTime());
							String formattedValue = new SimpleDateFormat("MMMM dd, yyyy HH:mm a").format(currentValue);
							return formattedValue;
						} else if (dateDatatypes.contains(section) && inputMessage.get(section) != null) {
							// CHANGE DATE FORMAT IF FIELD IS DATE
							Date parsedDate = dateFormat.parse(inputMessage.get(section).toString());
							Timestamp currentValue = new Timestamp(parsedDate.getTime());
							String formattedValue = new SimpleDateFormat("MMMM dd, yyyy").format(currentValue);
							return formattedValue;
						} else if (timeDatatypes.contains(section) && inputMessage.get(section) != null) {
							// CHANGE DATE FORMAT IF FIELD IS TIME
							Date parsedDate = dateFormat.parse(inputMessage.get(section).toString());
							Timestamp currentValue = new Timestamp(parsedDate.getTime());
							String formattedValue = new SimpleDateFormat("HH:mm a").format(currentValue);
							return formattedValue;
						} else if (chronometers.contains(section) && inputMessage.get(section) != null) {
							// CHANGE DATE FORMAT IF FIELD IS TIME
							String chronometerValueInSecond = global
									.chronometerFormatTransform((int) inputMessage.get(section), "");
							return chronometerValueInSecond;
						} else {
							return inputMessage.get(section).toString();
						}
					} else {
						return "";
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
