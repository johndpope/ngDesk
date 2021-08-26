package com.ngdesk.nodes;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.ngdesk.Global;
import com.ngdesk.channels.chat.ChatChannelController;
import com.ngdesk.channels.chat.EndChatController;
import com.ngdesk.discussion.DiscussionController;
import com.ngdesk.discussion.DiscussionMessage;
import com.ngdesk.discussion.EndChat;

import org.apache.commons.validator.routines.EmailValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

//@Component
public class ChatBot extends Node {

	@Autowired
	Global global;

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	ParentNode parent;

	@Autowired
	DiscussionController discussionController;

	@Autowired
	private SimpMessagingTemplate template;

	@Autowired
	private HttpRequestNode httpRequestNode;

	@Autowired
	private EndChatController endChatController;

	@Autowired
	private ChatChannelController chatChannelController;

	@Autowired
	private FindAgentAndAssign findAgentAndAssign;

	@Autowired
	private Environment env;

	private static final Logger logger = LogManager.getLogger(ChatBot.class);

	@Override
	public Map<String, Object> executeNode(Document node, Map<String, Object> inputMessage) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		boolean completedChatBot = false;
		try {
			logger.trace("Enter ChatBot.executeNode()");

			// GET COMPANY
			Document company = global.getCompanyFromUUID(inputMessage.get("COMPANY_UUID").toString());

			String companyId = company.getObjectId("_id").toString();

			// GET CHANNEL
			MongoCollection<Document> chatChannelCollection = mongoTemplate.getCollection("channels_chat_" + companyId);
			Document chatChannel = chatChannelCollection
					.find(Filters.eq("_id", new ObjectId(inputMessage.get("CHANNEL_ID").toString()))).first();

			MongoCollection<Document> teamsCollection = mongoTemplate.getCollection("Teams_" + companyId);
			String moduleId = chatChannel.getString("MODULE");

			String chatBotId = null;
			Document settings = (Document) chatChannel.get("SETTINGS");

			// GET BOT
			if (settings.containsKey("BOT_SETTINGS")) {
				Document botSettings = (Document) settings.get("BOT_SETTINGS");
				if (botSettings.containsKey("BOT_ENABLED") && botSettings.getBoolean("BOT_ENABLED")) {
					chatBotId = botSettings.getString("CHAT_BOT");
				}
			}

			int maxChatsPerAgent = company.getInteger("MAX_CHATS_PER_AGENT");
			// GET SYSTEM USER FOR ENDING THE CHAT

			String systemUserUUID = global.getSystemUser(companyId);

			// GET WORKFLOW
			Document workflow = null;
			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			Document module = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();

			if (module.containsKey("CHAT_BOTS") && chatBotId != null) {
				List<Document> chatBots = (List<Document>) module.get("CHAT_BOTS");
				if (chatBots.size() > 0) {
					for (Document chatBot : chatBots) {
						if (chatBot.getString("CHAT_BOT_ID").equals(chatBotId)) {
							workflow = (Document) chatBot.get("WORKFLOW");
							break;
						}
					}
				}
			}

			if (workflow != null) {

				List<Document> botNodes = (List<Document>) workflow.get("NODES");
				String moduleName = module.getString("NAME");

				// GET CHAT ENTRY
				MongoCollection<Document> entryCollection = mongoTemplate
						.getCollection(moduleName.replaceAll("\\s+", "_") + "_" + companyId);
				Document entry = entryCollection
						.find(Filters.eq("_id", new ObjectId(inputMessage.get("DATA_ID").toString()))).first();

				String entryId = entry.remove("_id").toString();

				// BUILD DEFAULT META_DATA
				Document botMetaData = new Document();
				botMetaData.put("CURRENT_NODE", null);
				botMetaData.put("CURRENT_STATE", null);
				botMetaData.put("IS_BEING_SERVED_BY_BOT", true);

				if (!entry.containsKey("META_DATA")) {
					entryCollection.findOneAndUpdate(Filters.eq("_id", new ObjectId(entryId)),
							Updates.set("META_DATA", botMetaData));
				} else {
					botMetaData = (Document) entry.get("META_DATA");
				}

				if (botMetaData.containsKey("IS_BEING_SERVED_BY_BOT")
						&& botMetaData.getBoolean("IS_BEING_SERVED_BY_BOT")) {
					String currentNode = botNodes.get(0).getString("ID");

					// FETCH THE CURRENT NODE FROM THE ENTRY
					if (botMetaData.get("CURRENT_NODE") != null) {
						currentNode = botMetaData.getString("CURRENT_NODE");
					}
					for (Document botNode : botNodes) {
						// NAVIGATE TO CURRENT NODE
						if (botNode.getString("ID").equalsIgnoreCase(currentNode)) {
							// ACTION BY NODE TYPE (SEND MESSAGE)
							if (botNode.getString("TYPE").equalsIgnoreCase("SendMessage")) {
								Document nodeValues = (Document) botNode.get("VALUES");

								// UPDATE META_DATA ON THE ENTRY
								botMetaData.put("CURRENT_NODE", currentNode);
								botMetaData.put("CURRENT_STATE", "MESSAGE_SENT");
								botMetaData.put("IS_BEING_SERVED_BY_BOT", true);
								entryCollection.findOneAndUpdate(Filters.eq("_id", new ObjectId(entryId)),
										Updates.set("META_DATA", botMetaData));

								String message = nodeValues.getString("MESSAGE");
								JSONObject payload = new JSONObject();

								payload.put("MESSAGE", "<html> <head></head> <body>" + message + "</body> </html>");
								payload.put("EMAIL_ADDRESS", "system@ngdesk.com");
								payload.put("ATTACHMENTS", new JSONArray());
								payload.put("COMPANY_SUBDOMAIN", company.getString("COMPANY_SUBDOMAIN"));
								payload.put("WIDGET_ID", entry.getString("CHANNEL"));
								payload.put("SESSION_UUID", entry.get("SESSION_UUID"));

								DiscussionMessage sendMessage = new ObjectMapper().readValue(payload.toString(),
										DiscussionMessage.class);
								discussionController.post(sendMessage);

								if (botNode.get("CONNECTS_TO") != null && !botNode.getString("CONNECTS_TO").isEmpty()) {
									currentNode = botNode.getString("CONNECTS_TO");
								} else {

									// BUILD END CHAT OBJECT
									EndChat endChatObject = new EndChat();
									endChatObject.setChannel(entry.getString("CHANNEL"));
									endChatObject.setSessionUuid(entry.getString("SESSION_UUID"));
									endChatObject.setSubdomain(company.getString("COMPANY_SUBDOMAIN"));
									endChatObject.setUserUuid(systemUserUUID);

									endChatController.endChat(endChatObject);

									botMetaData.put("CURRENT_NODE", null);
									botMetaData.put("CURRENT_STATE", null);
									botMetaData.put("IS_BEING_SERVED_BY_BOT", false);
									completedChatBot = true;
									break;
								}
							} else if (botNode.getString("TYPE").equalsIgnoreCase("QuestionNode")) {
								// ACTION BY NODE TYPE (QuestionNode)
								Document nodeValues = (Document) botNode.get("VALUES");

								// SEND FIRST QUESTION & NEW QUESTION IF PREVIOUS QUESTION IS ANSWERED
								if (botMetaData.get("CURRENT_STATE") == null
										|| botMetaData.getString("CURRENT_STATE").equals("QUESTION_ANSWERED")
										|| botMetaData.getString("CURRENT_STATE").equals("MESSAGE_SENT")) {

									// UPDATE META_DATA ON THE ENTRY
									entryCollection.findOneAndUpdate(Filters.eq("_id", new ObjectId(entryId)),
											Updates.set("META_DATA", botMetaData));

									String message = nodeValues.getString("MESSAGE");
									askQuestion(message, entry.getString("CHANNEL"),
											company.getString("COMPANY_SUBDOMAIN"), entry.getString("SESSION_UUID"));

									// SUB TYPE BUTTON
									if (botNode.containsKey("SUB_TYPE")
											&& botNode.getString("SUB_TYPE").equalsIgnoreCase("BUTTONS")
											&& nodeValues.containsKey("OPTIONS")) {
										JSONObject buttonsArray = new JSONObject();
										JSONArray arr = new JSONArray();
										for (Document option : (List<Document>) nodeValues.get("OPTIONS"))
											arr.put(option.getString("OPTION"));
										buttonsArray.put("BUTTONSARRAY", arr);
										buttonsArray.put("DISABLE_TEXT_INPUT", nodeValues.get("DISABLE_TEXT_INPUT"));
										String confirmationTopic = "topic/chat/" + entry.getString("SESSION_UUID");
										this.template.convertAndSend(confirmationTopic, buttonsArray.toString());
									}
									botMetaData.put("CURRENT_NODE", currentNode);
									botMetaData.put("CURRENT_STATE", "QUESTION_ASKED");
									break;
								} else if (botMetaData.getString("CURRENT_STATE").equals("QUESTION_ASKED")) {
									// GETS ANSWER FROM THE INPUT MESSAGE
									if (!inputMessage.containsKey("ANSWER")) {
										break;
									}
									// TAKE ANSWER FROM INPUT MESSAGE AND DELETE THE KEY
									String answer = inputMessage.get("ANSWER").toString();
									inputMessage.remove("ANSWER");

									String subType = botNode.getString("SUB_TYPE");
									if (botNode.containsKey("CONNECTS_TO") && botNode.get("CONNECTS_TO") != null
											&& !botNode.getString("CONNECTS_TO").isEmpty()) {
										if (subType.equals("Phone")) {
											// FETCH THE MODULE AND USER ENTRY

											Document usersModule = modulesCollection.find(Filters.eq("NAME", "Users"))
													.first();
											String userModuleId = usersModule.getObjectId("_id").toString();
											MongoCollection<Document> usersCollection = mongoTemplate
													.getCollection("Users_" + companyId);
											Document user = usersCollection
													.find(Filters.eq("_id", new ObjectId(entry.getString("REQUESTOR"))))
													.first();
											String dataId = user.remove("_id").toString();

											// UPDATE PHONE NUMBER IN PHONE OBJECT
											Document phone = new Document();

											phone.put("PHONE_NUMBER", answer);
											phone.put("COUNTRY_CODE", "US");
											phone.put("DIAL_CODE", "+1");
											phone.put("COUNTRY_FLAG", "us.svg");

											user.put("DATA_ID", dataId);
											user.put("PHONE_NUMBER", phone);

											// UPDATE THE PHONE NUMBER
											String url = "http://" + env.getProperty("rest.host")
													+ ":9080/ngdesk/modules/" + userModuleId + "/data/" + dataId
													+ "?company_id=" + companyId + "&user_uuid="
													+ user.getString("USER_UUID") + "&is_trigger=" + true;
											String payload = user.toJson();

											int responseCode = 200;
											JSONObject httpResponse = httpRequestNode.request(url, payload, "PUT",
													null);
											if (httpResponse.has("RESPONSE_CODE")) {
												responseCode = httpResponse.getInt("RESPONSE_CODE");
											}

											if (responseCode != 200) {
												botMetaData.put("CURRENT_STATE", "INVALID_ANSWER");
												entryCollection.findOneAndUpdate(
														Filters.eq("_id", new ObjectId(entryId)),
														Updates.set("META_DATA", botMetaData));

												String errorMessage = "Not a valid phone number.<br>For ex: +1 1234567890 is a valid phone number.<br>";
												String message = nodeValues.getString("MESSAGE");

												// ASK QUESTION
												askQuestion(errorMessage + message, entry.getString("CHANNEL"),
														company.getString("COMPANY_SUBDOMAIN"),
														entry.getString("SESSION_UUID"));

												botMetaData.put("CURRENT_NODE", currentNode);
												botMetaData.put("CURRENT_STATE", "QUESTION_ASKED");
												break;
											}

											// GO TO NEXT NODE
											botMetaData.put("CURRENT_STATE", "QUESTION_ANSWERED");
											currentNode = botNode.getString("CONNECTS_TO");
											botMetaData.put("CURRENT_NODE", currentNode);
											continue;
										} else if (subType.equals("Email")) {

											Document usersModule = modulesCollection.find(Filters.eq("NAME", "Users"))
													.first();
											String userModuleId = usersModule.getObjectId("_id").toString();
											MongoCollection<Document> usersCollection = mongoTemplate
													.getCollection("Users_" + companyId);
											Document user = usersCollection
													.find(Filters.eq("_id", new ObjectId(entry.getString("REQUESTOR"))))
													.first();
											Document answerDoc = usersCollection
													.find(Filters
															.and(Filters.eq("EMAIL_ADDRESS", answer),
																	Filters.or(Filters.eq("EFFECTIVE_TO", null),
																			Filters.exists("EFFECTIVE_TO", false))))
													.first();
											String dataId = "";

											if (answerDoc == null) {
												dataId = user.remove("_id").toString();
												user.put("EMAIL_ADDRESS", answer);
												user.put("DATA_ID", dataId);
												String url = "http://" + env.getProperty("rest.host")
														+ ":9080/ngdesk/modules/" + userModuleId + "/data/" + dataId
														+ "?company_id=" + companyId + "&user_uuid="
														+ user.getString("USER_UUID") + "&is_trigger=" + true;
												String payload = user.toJson();
												int responseCode = 200;
												JSONObject httpResponse = httpRequestNode.request(url, payload, "PUT",
														null);
												if (httpResponse.has("RESPONSE_CODE")) {
													responseCode = httpResponse.getInt("RESPONSE_CODE");
												}

												if (responseCode != 200) {
													botMetaData.put("CURRENT_STATE", "INVALID_ANSWER");
													entryCollection.findOneAndUpdate(
															Filters.eq("_id", new ObjectId(entryId)),
															Updates.set("META_DATA", botMetaData));

													String errorMessage = "Not a valid email address.<br>";
													String message = nodeValues.getString("MESSAGE");

													// ASK QUESTION
													askQuestion(errorMessage + message, entry.getString("CHANNEL"),
															company.getString("COMPANY_SUBDOMAIN"),
															entry.getString("SESSION_UUID"));

													botMetaData.put("CURRENT_NODE", currentNode);
													botMetaData.put("CURRENT_STATE", "QUESTION_ASKED");
													break;
												} else {
													// UPDATE ACCOUNT INFO
													MongoCollection<Document> accountsCollection = mongoTemplate
															.getCollection("Accounts_" + companyId);
													Document account = accountsCollection
															.find(Filters.eq("ACCOUNT_NAME", answer)).first();

													String globalTeamId = teamsCollection
															.find(Filters.and(Filters.eq("NAME", "Global"),
																	Filters.or(Filters.eq("EFFECTIVE_TO", null),
																			Filters.exists("EFFECTIVE_TO", false))))
															.first().getObjectId("_id").toString();

													if (account == null) {
														JSONObject newAccount = new JSONObject();
														List<String> teams = new ArrayList<String>();
														teams.add(globalTeamId);
														newAccount.put("ACCOUNT_NAME", answer);
														newAccount.put("DATE_CREATED", global
																.getFormattedDate(new Timestamp(new Date().getTime())));
														newAccount.put("TEAMS", teams);
														newAccount.put("DELETED", false);
														account = Document.parse(newAccount.toString());
														accountsCollection.insertOne(account);
													}
													usersCollection.findOneAndUpdate(
															Filters.eq("_id", new ObjectId(dataId)), Updates.set(
																	"ACCOUNT", account.getObjectId("_id").toString()));

												}
											} else {
												Document chatEntry = entryCollection
														.find(Filters.eq("_id", new ObjectId(entryId))).first();
												entryCollection.findOneAndUpdate(
														Filters.eq("_id", new ObjectId(entryId)), Updates.set(
																"REQUESTOR", answerDoc.getObjectId("_id").toString()));
											}

											botMetaData.put("CURRENT_STATE", "QUESTION_ANSWERED");
											currentNode = botNode.getString("CONNECTS_TO");
											botMetaData.put("CURRENT_NODE", currentNode);
											continue;

										} else if (subType.equals("Name")) {

											// NO VALIDATION FOR NAME
											Document usersModule = modulesCollection.find(Filters.eq("NAME", "Users"))
													.first();
											String userModuleId = usersModule.getObjectId("_id").toString();
											MongoCollection<Document> usersCollection = mongoTemplate
													.getCollection("Users_" + companyId);
											Document user = usersCollection
													.find(Filters.eq("_id", new ObjectId(entry.getString("REQUESTOR"))))
													.first();
											String dataId = user.remove("_id").toString();

											// UPDATE TEAM
											List<String> customerTeams = (List<String>) user.get("TEAMS");
											Document personalTeam = null;
											for (String customerTeam : customerTeams) {
												personalTeam = teamsCollection
														.find(Filters.and(Filters.eq("_id", new ObjectId(customerTeam)),
																Filters.eq("NAME",
																		user.getString("FIRST_NAME") + " "
																				+ user.getString("LAST_NAME"))))
														.first();
												if (personalTeam != null) {
													break;
												}
											}
											if (personalTeam != null) {
												teamsCollection
														.findOneAndUpdate(
																Filters.eq("_id",
																		new ObjectId(personalTeam.getObjectId("_id")
																				.toString())),
																Updates.combine(Updates.set("NAME", answer),
																		Updates.set("DESCRIPTION",
																				"Personal Team for " + answer)));
											}

											user.put("FIRST_NAME", answer);
											user.put("DATA_ID", dataId);

											String url = "http://" + env.getProperty("rest.host")
													+ ":9080/ngdesk/modules/" + userModuleId + "/data/" + dataId
													+ "?company_id=" + companyId + "&user_uuid="
													+ user.getString("USER_UUID") + "&is_trigger=" + true;
											String payload = user.toJson();

											int responseCode = 200;
											JSONObject httpResponse = httpRequestNode.request(url, payload, "PUT",
													null);
											if (httpResponse.has("RESPONSE_CODE")) {
												responseCode = httpResponse.getInt("RESPONSE_CODE");
											}

											if (responseCode != 200) {
												botMetaData.put("CURRENT_STATE", "INVALID_ANSWER");
												entryCollection.findOneAndUpdate(
														Filters.eq("_id", new ObjectId(entryId)),
														Updates.set("META_DATA", botMetaData));

												String message = nodeValues.getString("MESSAGE");

												// ASK QUESTION
												askQuestion(message, entry.getString("CHANNEL"),
														company.getString("COMPANY_SUBDOMAIN"),
														entry.getString("SESSION_UUID"));

												botMetaData.put("CURRENT_NODE", currentNode);
												botMetaData.put("CURRENT_STATE", "QUESTION_ASKED");
												break;
											}

											botMetaData.put("CURRENT_STATE", "QUESTION_ANSWERED");
											currentNode = botNode.getString("CONNECTS_TO");
											botMetaData.put("CURRENT_NODE", currentNode);
											continue;
										} else {
											// FREE TEXT QUESTION
											botMetaData.put("CURRENT_STATE", "QUESTION_ANSWERED");

											if (nodeValues.containsKey("MAPPING")
													&& !nodeValues.getString("MAPPING").isEmpty()) {

												Document field = global.getFieldFromId(nodeValues.getString("MAPPING"),
														module.getString("NAME"), companyId);

												entry.put(field.getString("NAME"), answer);
												entry.put("DATA_ID", entryId);

												String url = "http://" + env.getProperty("rest.host")
														+ ":9080/ngdesk/modules/" + moduleId + "/data/" + entryId
														+ "?company_id=" + companyId + "&user_uuid="
														+ inputMessage.get("USER_UUID").toString() + "&is_trigger="
														+ true;
												String payload = entry.toJson();

												int responseCode = 200;
												JSONObject httpResponse = httpRequestNode.request(url, payload, "PUT",
														null);
												if (httpResponse.has("RESPONSE_CODE")) {
													responseCode = httpResponse.getInt("RESPONSE_CODE");
												}

												if (responseCode != 200) {
													botMetaData.put("CURRENT_STATE", "INVALID_ANSWER");
													entryCollection.findOneAndUpdate(
															Filters.eq("_id", new ObjectId(entryId)),
															Updates.set("META_DATA", botMetaData));

													String message = nodeValues.getString("MESSAGE");

													// ASK QUESTION
													askQuestion(message, entry.getString("CHANNEL"),
															company.getString("COMPANY_SUBDOMAIN"),
															entry.getString("SESSION_UUID"));

													botMetaData.put("CURRENT_NODE", currentNode);
													botMetaData.put("CURRENT_STATE", "QUESTION_ASKED");
													break;
												}
											}
											botMetaData.put("CURRENT_STATE", "QUESTION_ANSWERED");
											currentNode = botNode.getString("CONNECTS_TO");
											botMetaData.put("CURRENT_NODE", currentNode);
											continue;
										}
									} else if (subType.equalsIgnoreCase("BUTTONS")) {
										for (Document option : (List<Document>) nodeValues.get("OPTIONS")) {
											if (option.getString("OPTION").equals(answer)) {
												botMetaData.put("CURRENT_STATE", "QUESTION_ANSWERED");
												currentNode = option.getString("CONNECTS_TO");
												botMetaData.put("CURRENT_NODE", currentNode);
											}
											entry.put("META_DATA", botMetaData);
											if (nodeValues.containsKey("MAPPING")
													&& !nodeValues.getString("MAPPING").isEmpty()) {

												Document field = global.getFieldFromId(nodeValues.getString("MAPPING"),
														module.getString("NAME"), companyId);

												entry.put(field.getString("NAME"), answer);
												entry.put("DATA_ID", entryId);

												String url = "http://" + env.getProperty("rest.host")
														+ ":9080/ngdesk/modules/" + moduleId + "/data/" + entryId
														+ "?company_id=" + companyId + "&user_uuid="
														+ inputMessage.get("USER_UUID").toString() + "&is_trigger="
														+ true;
												String payload = entry.toJson();

												String response = null;
												JSONObject httpResponse = httpRequestNode.request(url, payload, "PUT",
														null);
												if (httpResponse.has("RESPONSE")) {
													response = httpResponse.getString("RESPONSE");
												}
											}
										}
										continue;
									} else {
										// BUILD END CHAT OBJECT
										EndChat endChatObject = new EndChat();
										endChatObject.setChannel(entry.getString("CHANNEL"));
										endChatObject.setSessionUuid(entry.getString("SESSION_UUID"));
										endChatObject.setSubdomain(company.getString("COMPANY_SUBDOMAIN"));
										endChatObject.setUserUuid(systemUserUUID);

										endChatController.endChat(endChatObject);

										botMetaData.put("CURRENT_NODE", null);
										botMetaData.put("CURRENT_STATE", null);
										botMetaData.put("IS_BEING_SERVED_BY_BOT", false);
										completedChatBot = true;
										break;
									}
								}
							} else if (botNode.getString("TYPE").equalsIgnoreCase("TransferToAgent")) {

								Document businessRules = (Document) settings.get("BUSINESS_RULES");
								boolean active = businessRules.getBoolean("ACTIVE");
								inputMessage.put("TYPE", "CHAT");

								if (active) {
									if (chatChannelController.checkIfAgentExists(company.getString("COMPANY_SUBDOMAIN"),
											companyId, module, maxChatsPerAgent)
											&& chatChannelController.assignAgent(companyId, businessRules)) {
										Map<String, Object> result = findAgentAndAssign.executeNode(botNode,
												inputMessage);
										inputMessage = (Map<String, Object>) result.get("INPUT_MESSAGE");

									} else {
										EndChat endChatObject = new EndChat();
										endChatObject.setChannel(entry.getString("CHANNEL"));
										endChatObject.setSessionUuid(entry.getString("SESSION_UUID"));
										endChatObject.setSubdomain(company.getString("COMPANY_SUBDOMAIN"));
										endChatObject.setUserUuid(systemUserUUID);

										endChatController.endChat(endChatObject);
									}
								} else if (chatChannelController.checkIfAgentExists(
										company.getString("COMPANY_SUBDOMAIN"), companyId, module, maxChatsPerAgent)) {
									Map<String, Object> result = findAgentAndAssign.executeNode(botNode, inputMessage);
									inputMessage = (Map<String, Object>) result.get("INPUT_MESSAGE");
								} else {
									EndChat endChatObject = new EndChat();
									endChatObject.setChannel(entry.getString("CHANNEL"));
									endChatObject.setSessionUuid(entry.getString("SESSION_UUID"));
									endChatObject.setSubdomain(company.getString("COMPANY_SUBDOMAIN"));
									endChatObject.setUserUuid(systemUserUUID);

									endChatController.endChat(endChatObject);
								}

								botMetaData.put("CURRENT_NODE", null);
								botMetaData.put("CURRENT_STATE", null);
								botMetaData.put("IS_BEING_SERVED_BY_BOT", false);
								completedChatBot = true;
								break;
							}
						}
					}

					// UPDATE THE META_DATA FOR EACH AND EVERY HIT
					entryCollection.findOneAndUpdate(Filters.eq("_id", new ObjectId(entryId)),
							Updates.set("META_DATA", botMetaData));
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		// IF BOT NODE HAS SOME CONNECTION
		if (completedChatBot) {
			ArrayList<Document> connections = (ArrayList<Document>) node.get("CONNECTIONS_TO");
			if (connections.size() == 1) {
				Document connection = connections.get(0);
				resultMap.put("NODE_ID", connection.getString("TO_NODE"));
			}
		}
		resultMap.put("INPUT_MESSAGE", inputMessage);
		logger.trace("Exit ChatBot.executeNode()");

		return resultMap;
	}

	public void askQuestion(String message, String channel, String subdomain, String sessionUUID) {
		try {
			JSONObject payload = new JSONObject();
			payload.put("MESSAGE", "<html> <head></head> <body>" + message + "</body> </html>");
			payload.put("EMAIL_ADDRESS", "system@ngdesk.com");
			payload.put("ATTACHMENTS", new JSONArray());
			payload.put("COMPANY_SUBDOMAIN", subdomain);
			payload.put("WIDGET_ID", channel);
			payload.put("SESSION_UUID", sessionUUID);

			DiscussionMessage sendMessage = new ObjectMapper().readValue(payload.toString(), DiscussionMessage.class);
			// POST MESSAGE
			discussionController.post(sendMessage);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}