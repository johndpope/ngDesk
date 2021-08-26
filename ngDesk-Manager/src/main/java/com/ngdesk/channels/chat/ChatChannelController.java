package com.ngdesk.channels.chat;

import java.io.IOException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.joda.time.LocalTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.ngdesk.Authentication;
import com.ngdesk.Global;
import com.ngdesk.createuser.CreateUserController;
import com.ngdesk.discussion.DiscussionController;
import com.ngdesk.discussion.DiscussionMessage;
import com.ngdesk.flowmanager.InputMessage;
import com.ngdesk.nodes.HttpRequestNode;
import com.ngdesk.nodes.ParentNode;
import com.ngdesk.nodes.PushNotification;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

public class ChatChannelController {

	private final Logger log = LoggerFactory.getLogger(ChatChannelController.class);

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private HttpRequestNode httpRequestNode;

	@Autowired
	Global global;

	@Autowired
	private Environment env;

	@Autowired
	Environment environment;

	@Autowired
	private SimpMessagingTemplate template;

	@Autowired
	CreateUserController createUserController;

	@Autowired
	RedissonClient redisson;

	@Autowired
	private Authentication auth;

	@Autowired
	private ParentNode parentNode;

	@Autowired
	PushNotification browserNotification;

	@Autowired
	DiscussionController discussionController;

	@MessageMapping("/channel/chat")
	public void save(PageLoad message) {
		try {
			String pageLoadJson = new ObjectMapper().writeValueAsString(message);
			log.trace("Enter PageLoadController.save() message: " + pageLoadJson);
			Document company = global.getCompanyFromUUID(message.getCompanyUUID());
			boolean trackChat = false;
			if (company != null) {
				String companyId = company.getObjectId("_id").toString();
				String subdomain = company.getString("COMPANY_SUBDOMAIN");
				int maxChatsPerAgent = company.getInteger("MAX_CHATS_PER_AGENT");
				MongoCollection<Document> chatCollection = mongoTemplate.getCollection("Chat_" + companyId);

				List<Document> chats = (List<Document>) chatCollection.find(Filters.ne("CHAT_ID", 1))
						.into(new ArrayList<Document>());
				if (chats.size() == 0) {
					trackChat = true;
				}

				MongoCollection<Document> chatChannelCollection = mongoTemplate
						.getCollection("channels_chat_" + companyId);

				if (new ObjectId().isValid(message.getWidgetId())) {

					Document chatChannel = chatChannelCollection
							.find(Filters.eq("_id", new ObjectId(message.getWidgetId()))).first();

					if (chatChannel != null) {
						String channelId = chatChannel.getObjectId("_id").toString();
						String channelName = chatChannel.getString("NAME");
						if (message.getSessionUUID() != null) {

							MongoCollection<Document> modulesCollection = mongoTemplate
									.getCollection("modules_" + companyId);

							String moduleId = chatChannel.getString("MODULE");

							MongoCollection<Document> usersCollection = mongoTemplate
									.getCollection("Users_" + companyId);

							if (new ObjectId().isValid(moduleId)) {

								Document module = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId)))
										.first();

								if (module != null) {
									String moduleName = module.getString("NAME");

									MongoCollection<Document> entriesCollection = mongoTemplate
											.getCollection(moduleName.replaceAll("\\s+", "_") + "_" + companyId);

									Document entry = entriesCollection
											.find(Filters.and(Filters.eq("SESSION_UUID", message.getSessionUUID()),
													Filters.eq("DELETED", false),
													Filters.or(Filters.eq("EFFECTIVE_TO", null),
															Filters.exists("EFFECTIVE_TO", false))))
											.first();

									log.trace("entry: " + entry);
									// CHECK IF AN AGENT IS AVAILABLE AND PUBLISH
									String topic = "topic/agents-available/" + subdomain;
									JSONObject agentMessage = new JSONObject();
									agentMessage.put("AGENTS_AVAILABLE", false);
									Document settings = (Document) chatChannel.get("SETTINGS");
									Document businessRules = (Document) settings.get("BUSINESS_RULES");
									boolean active = businessRules.getBoolean("ACTIVE");
									if (active) {
										if (checkIfAgentExists(subdomain, companyId, module, maxChatsPerAgent)
												&& assignAgent(companyId, businessRules)) {
											agentMessage.put("AGENTS_AVAILABLE", true);
										} else {
											agentMessage.put("AGENTS_AVAILABLE", false);
										}

									} else {
										if (checkIfAgentExists(subdomain, companyId, module, maxChatsPerAgent)) {
											agentMessage.put("AGENTS_AVAILABLE", true);
										}
									}
									log.trace(
											"Send message to topic: " + topic + " message: " + agentMessage.toString());
									this.template.convertAndSend(topic, agentMessage.toString());

									boolean preChatEnabled = settings.getBoolean("PRE_SURVEY_REQUIRED");

									boolean botEnabled = false;
									if (settings.containsKey("BOT_SETTINGS")) {
										Document botSettings = (Document) settings.get("BOT_SETTINGS");
										if (botSettings.containsKey("BOT_ENABLED")
												&& botSettings.get("BOT_ENABLED") != null) {
											botEnabled = botSettings.getBoolean("BOT_ENABLED");
										}
									}

									if (entry == null) {
										Document customerDocument = createVisitor(companyId, subdomain, company,
												message.getSessionUUID());

										Map<String, Object> inputMessage = new HashMap<String, Object>();
										inputMessage = new ObjectMapper().readValue(pageLoadJson, Map.class);
										inputMessage.put("TYPE", "chat");
										inputMessage.put("CHANNEL_NAME", channelName);
										inputMessage.put("CHANNEL_ID", channelId);
										inputMessage.put("MODULE", moduleId);
										inputMessage.put("REQUESTOR", customerDocument.getObjectId("_id").toString());
										inputMessage.put("USER_UUID", customerDocument.getString("USER_UUID"));
										inputMessage.put("IS_BOT_ENABLED", botEnabled);
										inputMessage.put("PRECHAT_SURVEY_ENABLED", preChatEnabled);

										startChatChannelWorkflow(chatChannel, inputMessage);

									} else {
										String entryId = entry.remove("_id").toString();
										Map<String, Object> inputMap = new ObjectMapper().readValue(entry.toJson(),
												Map.class);

										inputMap.put("START_CHAT", message.isStarted());
										inputMap.put("COMPANY_UUID", message.getCompanyUUID());
										inputMap.put("LOCATION", message.getLocation());

										String userTopic = "topic/notify/" + message.getSessionUUID();
										JSONObject notifyMessage = new JSONObject();
										notifyMessage.put("DATE_CREATED",
												global.getFormattedDate(new Timestamp(new Date().getTime())));
										notifyMessage.put("DATA_ID", entryId);
										notifyMessage.put("MODULE_ID", moduleId);
										notifyMessage.put("MODULE_NAME", module.getString("NAME"));

										// GET VISITOR FROM CHAT ENTRY
										String customerId = entry.getString("REQUESTOR");
										Document customerDocument = usersCollection
												.find(Filters.eq("_id", new ObjectId(customerId))).first();

										Document messageEmailUserDoc = usersCollection
												.find(Filters.eq("EMAIL_ADDRESS", message.getEmailAddress())).first();
										if (preChatEnabled) {
											if (messageEmailUserDoc == null) {
												inputMap.put("FIRST_NAME", message.getFirstName());
												inputMap.put("LAST_NAME", message.getLastName());
												inputMap.put("EMAIL_ADDRESS", message.getEmailAddress());

												// UPDATE ACCOUNT and TEAM
												MongoCollection<Document> teamsCollection = mongoTemplate
														.getCollection("Teams_" + companyId);
												String globalTeamId = teamsCollection
														.find(Filters.and(Filters.eq("NAME", "Global"),
																Filters.or(Filters.eq("EFFECTIVE_TO", null),
																		Filters.exists("EFFECTIVE_TO", false))))
														.first().getObjectId("_id").toString();

												List<String> customerTeams = (List<String>) customerDocument
														.get("TEAMS");
												Document personalTeam = null;
												for (String customerTeam : customerTeams) {
													personalTeam = teamsCollection
															.find(Filters.and(
																	Filters.eq("_id", new ObjectId(customerTeam)),
																	Filters.eq("NAME",
																			customerDocument.getString("FIRST_NAME")
																					+ " "
																					+ customerDocument
																							.getString("LAST_NAME"))))
															.first();
													if (personalTeam != null) {
														break;
													}
												}
												if (personalTeam != null) {
													teamsCollection.findOneAndUpdate(
															Filters.eq("_id",
																	new ObjectId(personalTeam.getObjectId("_id")
																			.toString())),
															Updates.combine(
																	Updates.set("NAME",
																			message.getFirstName() + " "
																					+ message.getLastName()),
																	Updates.set("DESCRIPTION",
																			"Personal Team for "
																					+ message.getFirstName() + " "
																					+ message.getLastName())));
												}

												String accountDomain = message.getEmailAddress().split("@")[1];
												MongoCollection<Document> accountsCollection = mongoTemplate
														.getCollection("Accounts_" + companyId);
												Document account = accountsCollection
														.find(Filters.eq("ACCOUNT_NAME", accountDomain)).first();
												if (account == null) {
													JSONObject newAccount = new JSONObject();
													List<String> teams = new ArrayList<String>();
													teams.add(globalTeamId);
													newAccount.put("ACCOUNT_NAME", accountDomain);
													newAccount.put("DATE_CREATED", new Date());
													newAccount.put("TEAMS", teams);
													newAccount.put("DELETED", false);
													account = Document.parse(newAccount.toString());
													accountsCollection.insertOne(account);
												}
												usersCollection.findOneAndUpdate(
														Filters.eq("_id", new ObjectId(customerId)),
														Updates.set("ACCOUNT", account.getObjectId("_id").toString()));
											} else {
												entriesCollection.findOneAndUpdate(
														Filters.eq("_id", new ObjectId(entryId)),
														Updates.set("REQUESTOR",
																messageEmailUserDoc.getObjectId("_id").toString()));
											}

										} else {
											inputMap.put("FIRST_NAME", customerDocument.getString("FIRST_NAME"));
											inputMap.put("LAST_NAME", customerDocument.getString("LAST_NAME"));
											inputMap.put("EMAIL_ADDRESS", customerDocument.getString("EMAIL_ADDRESS"));
										}
										notifyMessage.put("MESSAGE", customerDocument.toJson());
										notifyMessage.put("MESSAGE_TYPE", "USER_DETAILS");
										log.trace("Send message to topic: " + userTopic + " message: "
												+ notifyMessage.toString());
										this.template.convertAndSend(userTopic, notifyMessage.toString());

										customerDocument.remove("_id");
										Map<String, Object> userDetails = new ObjectMapper()
												.readValue(customerDocument.toJson(), Map.class);

										String role = customerDocument.getString("ROLE");
										MongoCollection<Document> rolesCollection = mongoTemplate
												.getCollection("roles_" + companyId);
										Document roleDocument = rolesCollection
												.find(Filters.eq("_id", new ObjectId(role))).first();

										userDetails.put("ROLE_NAME", roleDocument.getString("NAME"));

										inputMap.put("SENDER", userDetails);

										String discussionFieldName = null;
										List<Document> fields = (List<Document>) module.get("FIELDS");
										for (Document field : fields) {
											Document datatype = (Document) field.get("DATA_TYPE");
											if (datatype.getString("DISPLAY").equals("Discussion")) {
												discussionFieldName = field.getString("NAME");
												break;
											}
										}
										if (discussionFieldName != null && entry.get(discussionFieldName) != null) {
											List<Document> messages = (List<Document>) entry.get(discussionFieldName);
											for (Document messageDocument : messages) {
												if (messageDocument.containsKey("ATTACHMENTS")
														&& messageDocument.get("ATTACHMENTS") != null) {

													MongoCollection<Document> attachmentsCollection = mongoTemplate
															.getCollection("attachments_" + companyId);
													List<Document> attachments = (List<Document>) messageDocument
															.get("ATTACHMENTS");

													for (Document attachment : attachments) {
														String hash = attachment.getString("HASH");
														Document actualAttachment = attachmentsCollection
																.find(Filters.eq("HASH", hash)).first();

														if (actualAttachment != null) {
															attachment.put("ATTACHMENT_UUID",
																	actualAttachment.getString("ATTACHMENT_UUID"));
														}
													}
												}
											}
										}

										// NOTIFY END USER
										notifyMessage.put("MESSAGE", entry.toJson());
										notifyMessage.put("MESSAGE_TYPE", "EXISTING_ENTRY");
										log.trace("Send message to topic: " + userTopic + " message: "
												+ notifyMessage.toString());
										this.template.convertAndSend(userTopic, notifyMessage.toString());

										inputMap.put("DATA_ID", entryId);
										inputMap.put("TYPE", "chat");
										inputMap.put("CHANNEL_NAME", channelName);
										inputMap.put("MODULE", moduleId);
										inputMap.put("USER_UUID", customerDocument.getString("USER_UUID"));
										inputMap.put("IS_BOT_ENABLED", botEnabled);
										inputMap.put("CHANNEL_ID", channelId);
										inputMap.put("PRECHAT_SURVEY_ENABLED", preChatEnabled);
										startChatChannelWorkflow(chatChannel, inputMap);

									}
								}
							}
						}
					}
				}
				if (trackChat) {
					String url = "http://" + env.getProperty("rest.host") + ":9080/ngdesk/companies/track/user/event";

					String payload = new JSONObject(global.getFile("chat_event_tracking.json")).toString();

					httpRequestNode.request(url, payload, "POST", null);
				}
			}

//			ObjectMapper mapper = new ObjectMapper();
//			Map<String, Object> inputMessage = mapper.convertValue(message, Map.class);
//			triggers.execute(inputMessage);
		} catch (

		Exception e) {
			e.printStackTrace();
		}

		log.trace("Exit PageLoadController.save()");
	}

	@SuppressWarnings("deprecation")
	public boolean assignAgent(String companyId, Document businessRules) {

		boolean agentAvailable = false;
		String companyTimezone = "UTC";
		if (businessRules.getString("TIMEZONE") != null) {
			companyTimezone = businessRules.getString("TIMEZONE");
		}
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		Instant instant = Instant.now();
		ZoneId z = ZoneId.of(companyTimezone);
		ZoneOffset currentOffsetForMyZone = z.getRules().getOffset(instant);
		int secondsOffset = currentOffsetForMyZone.getTotalSeconds();
		List<Document> restrictions = (List<Document>) businessRules.get("RESTRICTIONS");
		String restrictionType = businessRules.getString("RESTRICTION_TYPE");
		try {
			for (Document restriction : restrictions) {
				String startTime = restriction.getString("START_TIME");
				String endTime = restriction.getString("END_TIME");
				DateFormat formatter = new SimpleDateFormat("HH:mm");
				Time start_Time = new Time(formatter.parse(startTime).getTime() - secondsOffset * 1000);
				Time end_Time = new Time(formatter.parse(endTime).getTime() - secondsOffset * 1000);
				Timestamp startTimeStamp = getStartTime(companyTimezone, startTime);
				Timestamp endTimeStamp = getEndTime(companyTimezone, endTime);
				Timestamp UTCtimestamp = getUTC(companyTimezone);
				if (restrictionType.equals("Day")) {
					if (start_Time.before(end_Time)) {
						if (UTCtimestamp.after(startTimeStamp) && UTCtimestamp.before(endTimeStamp)) {
							agentAvailable = true;
							return agentAvailable;
						}
						if (UTCtimestamp.equals(startTimeStamp) || UTCtimestamp.equals(endTimeStamp)) {
							agentAvailable = true;
							return agentAvailable;
						}
					} else {
						endTimeStamp.setDate(endTimeStamp.getDate() + 1);
						if (UTCtimestamp.after(startTimeStamp) && UTCtimestamp.before(endTimeStamp)) {
							agentAvailable = true;
							return agentAvailable;
						}
						if (UTCtimestamp.equals(startTimeStamp) || UTCtimestamp.equals(endTimeStamp)) {
							agentAvailable = true;
							return agentAvailable;
						}
					}
				} else if (restrictionType.equals("Week")) {
					String startDay = restriction.getString("START_DAY");
					String endDay = restriction.getString("END_DAY");
					int day_UTC = UTCtimestamp.getDay();
					int start = getDay(startDay);
					int end = getDay(endDay);
					Timestamp currentTimestamp = getTimestampAtTimezone(companyTimezone);

					Double currTime = (currentTimestamp.getHours() + ((double) currentTimestamp.getMinutes() / 60));
					Double stTime = Integer.parseInt(startTime.split(":")[0])
							+ ((double) Integer.parseInt(startTime.split(":")[1]) / 60);
					Double etTime = Integer.parseInt(endTime.split(":")[0])
							+ ((double) Integer.parseInt(endTime.split(":")[1]) / 60);

					if (start > end || (start == end && stTime > etTime)) {
						if (day_UTC <= end) {
							day_UTC = day_UTC + 7;
						}
						end = end + 7;
					}

					if (day_UTC == start && day_UTC == end) {
						if (stTime <= currTime && currTime < etTime) {
							agentAvailable = true;
							return agentAvailable;
						}
					} else if (day_UTC >= start && day_UTC <= end) {
						if (day_UTC >= 7 && day_UTC == end && start + 7 == end
								&& (currTime < etTime || currTime >= stTime)) {
							agentAvailable = true;
							return agentAvailable;
						} else if (day_UTC == start) {
							if (currTime >= stTime) {
								agentAvailable = true;
								return agentAvailable;
							}
						} else if (day_UTC == end) {
							if (currTime < etTime) {
								agentAvailable = true;
								return agentAvailable;
							}
						} else {
							agentAvailable = true;
							return agentAvailable;
						}
					}
				}
			}

		} catch (Exception e) {

		}
		return agentAvailable;
	}

	private Timestamp getTimestampAtTimezone(String timeZone) {
		DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		Timestamp currentTimezoneTimestamp = null;

		try {
			Timestamp currentTimestamp = new Timestamp(new Date().getTime());

			ZonedDateTime z = currentTimestamp.toInstant().atZone(ZoneId.of(timeZone));
			String currentTime = z.format(fmt);

			Date currentparsedDate = dateFormat.parse(currentTime);
			currentTimezoneTimestamp = new Timestamp(currentparsedDate.getTime());
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return currentTimezoneTimestamp;
	}

	private Timestamp getUTC(String timeZone) {

		log.trace("Enter OnCallUser.getUTC() timeZone: " + timeZone);
		DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		Timestamp currentTimezoneTimestamp = null;
		try {
			Timestamp currentTimestamp = new Timestamp(new Date().getTime());

			ZonedDateTime z = currentTimestamp.toInstant().atZone(ZoneId.of(timeZone));
			String currentTime = z.format(fmt);

			Date currentparsedDate = dateFormat.parse(currentTime);
			currentTimezoneTimestamp = new Timestamp(currentparsedDate.getTime());

			TimeZone tz = TimeZone.getTimeZone(timeZone);
			int offset = tz.getOffset(new Date().getTime());
			currentTimezoneTimestamp.setTime(currentparsedDate.getTime() - offset);

		} catch (ParseException e) {
			e.printStackTrace();
		}
		log.trace("Exit OnCallUser.getUTC() timeZone: " + timeZone);
		return currentTimezoneTimestamp;
	}

	private Timestamp getStartTime(String timeZone, String startTime) throws ParseException {
		log.trace("Enter OnCallUser.getstartTime() timeZone: " + timeZone + ", startTime: " + startTime);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
		SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm");

		Timestamp currentTimestamp = new Timestamp(new Date().getTime());
		ZonedDateTime z = currentTimestamp.toInstant().atZone(ZoneId.of(timeZone));
		String currentTime = z.format(fmt);
		Date currentparsedDate = dateFormat.parse(currentTime);
		Timestamp startTimestamp = new Timestamp(currentparsedDate.getTime());

		Date startparsedDate = hourFormat.parse(startTime);

		startTimestamp.setHours(startparsedDate.getHours());
		startTimestamp.setMinutes(startparsedDate.getMinutes());
		startTimestamp.setSeconds(startparsedDate.getSeconds());
		Date parsedDate = dateFormat.parse(startTimestamp.toString());

		TimeZone tz = TimeZone.getTimeZone(timeZone);
		int offset = tz.getOffset(new Date().getTime());
		startTimestamp.setTime(parsedDate.getTime() - offset);
		log.trace("Exit OnCallUser.getstartTime() timeZone: " + timeZone + ", startTime: " + startTime);
		return startTimestamp;
	}

	private Timestamp getEndTime(String timeZone, String endTime) throws ParseException {
		log.trace("Enter OnCallUser.getstartTime() timeZone: " + timeZone + ", endTime: " + endTime);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
		SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm");

		Timestamp currentTimestamp = new Timestamp(new Date().getTime());
		ZonedDateTime z = currentTimestamp.toInstant().atZone(ZoneId.of(timeZone));
		String currentTime = z.format(fmt);
		Date currentparsedDate = dateFormat.parse(currentTime);
		Timestamp endTimestamp = new Timestamp(currentparsedDate.getTime());

		Date endparsedDate = hourFormat.parse(endTime);
		endTimestamp.setHours(endparsedDate.getHours());
		endTimestamp.setMinutes(endparsedDate.getMinutes());
		endTimestamp.setSeconds(endparsedDate.getSeconds());
		Date parsedDate = dateFormat.parse(endTimestamp.toString());
		TimeZone tz = TimeZone.getTimeZone(timeZone);
		int offset = tz.getOffset(new Date().getTime());
		endTimestamp.setTime(parsedDate.getTime() - offset);
		log.trace("Exit OnCallUser.getstartTime() timeZone: " + timeZone + ", endTime: " + endTime);
		return endTimestamp;
	}

	public int getDay(String day) {
		if (day.equals("Sun")) {
			return 0;
		}
		if (day.equals("Mon")) {
			return 1;
		}
		if (day.equals("Tue")) {
			return 2;
		}
		if (day.equals("Wed")) {
			return 3;
		}
		if (day.equals("Thu")) {
			return 4;
		}
		if (day.equals("Fri")) {
			return 5;
		}
		if (day.equals("Sat")) {
			return 6;
		}
		return -1;
	}

	public boolean startChatChannelWorkflow(Document chatChannel, Map<String, Object> inputMap) {
		try {
			Document workflowDocument = (Document) chatChannel.get("WORKFLOW");
			if (workflowDocument != null) {
				if (workflowDocument.containsKey("NODES")) {
					ArrayList<Document> nodeDocuments = (ArrayList<Document>) workflowDocument.get("NODES");
					if (nodeDocuments != null && nodeDocuments.size() > 0) {
						Document firstNode = nodeDocuments.get(0);
						if ("Start".equals(firstNode.getString("TYPE"))) {
							log.trace("parentNode.executeWorkflow()");
							parentNode.executeWorkflow(firstNode, nodeDocuments, inputMap);
							return true;
						}
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean checkIfAgentExists(String subdomain, String companyId, Document module,
			int maxNoOfChatsAllowedPerAgent) {
		try {

			List<String> teamsWhoCanChat = new ArrayList<String>();
			if (module.containsKey("SETTINGS")) {
				Document settings = (Document) module.get("SETTINGS");
				Document permissions = (Document) settings.get("PERMISSIONS");
				teamsWhoCanChat = (List<String>) permissions.get("CHAT");
			}

			MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);

			RMap<String, Map<String, Map<String, Object>>> companiesMap = redisson.getMap("companiesUsers");
			if (companiesMap.containsKey(subdomain)) {
				Map<String, Map<String, Object>> usersMap = companiesMap.get(subdomain);
				outer: for (String userId : usersMap.keySet()) {

					Map<String, Object> uMap = usersMap.get(userId);

					int noOfChats = (int) uMap.get("NO_OF_CHATS");
					if (uMap.get("STATUS").toString().equalsIgnoreCase("Online")
							&& (boolean) uMap.get("ACCEPTING_CHATS") && noOfChats < maxNoOfChatsAllowedPerAgent) {
						Document user = usersCollection.find(
								Filters.and(Filters.eq("_id", new ObjectId(userId)), Filters.eq("DELETED", false)))
								.first();
						if (user != null) {
							List<String> userTeams = (List<String>) user.get("TEAMS");
							for (String teamId : userTeams) {
								if (teamsWhoCanChat.contains(teamId)) {
									return true;
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public Document createVisitor(String companyId, String subdomain, Document company, String sessionUUID) {
		try {

			MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);

			Random rand = new Random();
			int randomInteger = rand.nextInt(100000);
			InputMessage inMessage = new InputMessage();
			inMessage.setFirstName("Visitor" + randomInteger);
			inMessage.setLastName("");
			inMessage.setEmailAddress("visitor" + randomInteger + "@" + subdomain + ".ngdesk.com");

			Document phone = (Document) company.get("PHONE");
			phone.put("PHONE_NUMBER", "");

			Document customerDocument = createUserController.createOrGetUser(companyId, inMessage, company, phone);
			return customerDocument;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@MessageMapping("/transfer/chat")
	public void chatTransfer(ChatTransferParameters chatTransferParam) {
		try {
			String moduleId = chatTransferParam.getModuleId();
			String dataId = chatTransferParam.getDataId();
			String subdomain = chatTransferParam.getSubdomain();

			MongoCollection<Document> companiesCollection = mongoTemplate.getCollection("companies");
			Document companyDocument = companiesCollection.find(Filters.eq("COMPANY_SUBDOMAIN", subdomain)).first();
			String companyId = companyDocument.getObjectId("_id").toString();

			transferChat(subdomain, null, moduleId, dataId, companyId);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void transferChat(String subdomain, String uuid, String moduleId, String dataId, String companyId) {
		try {
			String userId = null;
//			JSONObject user = null;
			Document companyDocument = null;
			MongoCollection<Document> companiesCollection = mongoTemplate.getCollection("companies");
			companyDocument = companiesCollection.find(Filters.eq("_id", new ObjectId(companyId))).first();
			companyId = companyDocument.getObjectId("_id").toString();

			if (companyId != null) {
				String companyUUID = companyDocument.getString("COMPANY_UUID");

				MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
				Document module = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();

				MongoCollection<Document> entriesCollection = mongoTemplate.getCollection("Chat_" + companyId);
				Document entry = entriesCollection.find(Filters.eq("_id", new ObjectId(dataId))).first();
				String entryId = entry.getObjectId("_id").toString();

				List<String> agentList = (List<String>) entry.get("AGENTS");
				userId = agentList.get(agentList.size() - 1);
				// CHECK IF ANY OTHER AGENT IS AVAILABLE
				boolean agentExists = checkIfAgentExists(subdomain, companyId, module, userId);
				String topic = "topic/chat-transfer-status";
				if (agentExists) {
					// In ORDER TO PREVENT CURRENT USER TO ASSIGN CHAT
					RMap<String, Map<String, Map<String, Object>>> companiesMap = redisson.getMap("companiesUsers");
					Map<String, Map<String, Object>> usersMap = companiesMap.get(subdomain);
					usersMap.get(userId).put("ACCEPTING_CHATS", false);
					companiesMap.put(subdomain, usersMap);

					if (entry != null) {
						// SEND NOTIFICATION THAT THE AGENT WAS UNASSIGNED
						JSONObject notifyMessage = new JSONObject();
						notifyMessage.put("MESSAGE_TYPE", "NOTIFICATION");
						notifyMessage.put("DATE_CREATED", global.getFormattedDate(new Timestamp(new Date().getTime())));
						notifyMessage.put("DATA_ID", entryId);
						notifyMessage.put("MODULE_ID", moduleId);
						notifyMessage.put("MODULE_NAME", module.getString("NAME"));

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

						// ADDING META DATA

						String message = global.getFile("metadata_chat_agent_left.html");
						Map<String, Object> metaDataMessage = new HashMap<String, Object>();
						String systemUserUUID = global.getSystemUser(companyId);

						MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);
						Document userDoc = usersCollection.find(Filters.eq("_id", new ObjectId(userId))).first();

						String firstName = userDoc.getString("FIRST_NAME");
						String lastName = userDoc.getString("LAST_NAME");
						// NOTIFY END USER
						notifyMessage.put("MESSAGE", firstName + " " + lastName + " has left the chat");

						message = message.replace("FIRST_NAME_REPLACE", userDoc.getString("FIRST_NAME"));
						message = message.replace("LAST_NAME_REPLACE", userDoc.getString("LAST_NAME"));

						metaDataMessage.put("COMPANY_UUID", companyUUID);
						metaDataMessage.put("MESSAGE_ID", UUID.randomUUID().toString());
						metaDataMessage.put("USER_UUID", systemUserUUID);

						List<Map<String, Object>> builtMessage = global.buildDiscussionPayload(metaDataMessage, message,
								"META_DATA");
						Document messageDoc = Document
								.parse(new ObjectMapper().writeValueAsString(builtMessage.get(0)));
						if (discussionFieldName != null) {
							// INSERT META_DATA INTO MESSAGES USING DISCUSSION CONTROLLER
							builtMessage.get(0).remove("DATE_CREATED");
							DiscussionMessage discussionMessage = new ObjectMapper().readValue(
									new ObjectMapper().writeValueAsString(builtMessage.get(0)).toString(),
									DiscussionMessage.class);
							discussionMessage.setSubdomain(subdomain);
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

						List<String> agents = (List<String>) entry.get("AGENTS");

						for (String agentUserId : agents) {
							String agentTopic = "topic/notify/" + userId;
							notifyMessage.put("READ", false);
							notifyMessage.put("NOTIFICATION_UUID", UUID.randomUUID().toString());
							notifyMessage.put("RECEPIENT", agentUserId);
							notificationsCollection.insertOne(Document.parse(notifyMessage.toString()));
							this.template.convertAndSend(agentTopic, notifyMessage.toString());

							// FIREBASE PUSH NOTIFICATION
							MongoCollection<Document> tokenCollection = mongoTemplate
									.getCollection("user_tokens_" + companyId);
							Document agentDoc = usersCollection.find(Filters.eq("_id", new ObjectId(agentUserId)))
									.first();
							Document userDocument = tokenCollection
									.find(Filters.eq("USER_UUID", agentDoc.getString("USER_UUID"))).first();
							String url = "https://" + subdomain + ".ngdesk.com/render/" + moduleId + "/edit/" + entryId
									+ "";
							browserNotification.sendWebNotifications(userDocument,
									firstName + " " + lastName + " has left the chat", subdomain, url);
						}

						// IF ANY OTHER AGENT IS AVAILABLE, START THE WORKFLOW
						String transferChatWorkflowFile = global.getFile("TransferChatWorkflow.json");
						transferChatWorkflowFile = transferChatWorkflowFile.replaceAll("MODULE_ID", moduleId);
						Document transferChatWorkflowDocument = Document.parse(transferChatWorkflowFile);

						// FETCHING CHANNEL NAME

						String channelId = entry.getString("CHANNEL");
						MongoCollection<Document> chatChannelCollection = mongoTemplate
								.getCollection("channels_chat_" + companyId);
						Document chatChannel = chatChannelCollection.find(Filters.eq("_id", new ObjectId(channelId)))
								.first();
						String channelName = chatChannel.getString("NAME");

						// INPUT MAP
						entry.remove("_id");
						Map<String, Object> inputMap = new ObjectMapper().readValue(entry.toJson(), Map.class);
						inputMap.put("DATA_ID", entryId);
						inputMap.put("TYPE", "chat");
						inputMap.put("CHANNEL_NAME", channelName);
						inputMap.put("MODULE", moduleId);
						inputMap.put("COMPANY_UUID", companyUUID);
						inputMap.put("USER_UUID", userDoc.get("USER_UUID"));

						// TRIGGER THE WORKFLOW TO TRANSFER CHAT TO ANOTHER AGENT
						if (transferChatWorkflowDocument != null) {
							Document transferChatWorkflow = (Document) transferChatWorkflowDocument.get("WORKFLOW");
							if (transferChatWorkflow.containsKey("NODES")) {
								ArrayList<Document> nodeDocuments = (ArrayList<Document>) transferChatWorkflow
										.get("NODES");
								if (nodeDocuments != null && nodeDocuments.size() > 0) {
									Document firstNode = nodeDocuments.get(0);
									if ("Start".equals(firstNode.getString("TYPE"))) {
										parentNode.executeWorkflow(firstNode, nodeDocuments, inputMap);
									}
								}
							}
						}
					}
					this.template.convertAndSend(topic, "chat-transfer");

				} else {
					this.template.convertAndSend(topic, "Agent not found");
				}
			}
		} catch (JSONException | IOException e) {
			e.printStackTrace();
		}
	}

	// CHECK IF AGENTS ARE AVAILABLE OR NOT
	public boolean checkIfAgentExists(String subdomain, String companyId, Document module, String currentUserId) {
		try {
			List<String> teamsWhoCanChat = new ArrayList<String>();
			if (module.containsKey("SETTINGS")) {
				Document settings = (Document) module.get("SETTINGS");
				Document permissions = (Document) settings.get("PERMISSIONS");
				teamsWhoCanChat = (List<String>) permissions.get("CHAT");
			}

			MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);
			RMap<String, Map<String, Map<String, Object>>> companiesMap = redisson.getMap("companiesUsers");
			if (companiesMap.containsKey(subdomain)) {
				Map<String, Map<String, Object>> usersMap = companiesMap.get(subdomain);
				outer: for (String userId : usersMap.keySet()) {

					// THIS CONDITION IS WRITTEN TO EXCLUDE THE USER WHO IS REQUESTING THE TRANSFER
					if (!userId.equals(currentUserId)) {
						Map<String, Object> uMap = usersMap.get(userId);
						if (uMap.get("STATUS").toString().equalsIgnoreCase("Online")
								&& (boolean) uMap.get("ACCEPTING_CHATS")) {
							Document user = usersCollection.find(
									Filters.and(Filters.eq("_id", new ObjectId(userId)), Filters.eq("DELETED", false)))
									.first();
							if (user != null) {
								List<String> userTeams = (List<String>) user.get("TEAMS");
								for (String teamId : userTeams) {
									if (teamsWhoCanChat.contains(teamId)) {
										return true;
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
		return false;
	}
}