package com.ngdesk.triggers;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.ngdesk.Global;
import com.ngdesk.channels.chat.PageLoad;
import com.ngdesk.discussion.DiscussionMessage;
import com.ngdesk.discussion.Sender;

@Component
public class ChatTriggers {

	@Autowired
	private Global global;

	@Autowired
	private Environment env;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private SimpMessagingTemplate template;

	private static final Logger logger = LoggerFactory.getLogger(ChatTriggers.class);

	public void execute(Map<String, Object> message) {
		try {

			logger.trace("Enter ChatTriggers.execute()");
			if (message.containsKey("COMPANY_UUID") && message.get("COMPANY_UUID") != null) {
				String companyUUID = message.get("COMPANY_UUID").toString();
				String companyId = global.getCompanyId(companyUUID);
				if (message.containsKey("WIDGET_ID") && message.get("WIDGET_ID") != null) {
					String widgetId = message.get("WIDGET_ID").toString();

					if (message.containsKey("SESSION_UUID") && message.get("SESSION_UUID") != null) {
						String sessionUUID = message.get("SESSION_UUID").toString();
						if (message.containsKey("SESSION_PASSWORD") && message.get("SESSION_PASSWORD") != null) {
							String sessionPassword = message.get("SESSION_PASSWORD").toString();

							String collectionName = "channels_chat_" + companyId;
							MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
							Document chatChannel = collection.find(Filters.eq("_id", new ObjectId(widgetId))).first();
							boolean check = false;
							if (chatChannel.get("CHAT_TRIGGERS") != null) {
								ArrayList<Document> triggerDocuments = (ArrayList) chatChannel.get("CHAT_TRIGGERS");
								for (Document triggerDocument : triggerDocuments) {
									if (triggerDocument.get("CONDITIONS") != null) {
										ArrayList<Document> triggerConditions = (ArrayList) triggerDocument
												.get("CONDITIONS");
										check = evaluateConditions(message, triggerConditions, companyId);
									}
									if (check) {
										executeActions(triggerDocument, companyUUID, widgetId, companyId, sessionUUID,
												sessionPassword);
									}
								}

							}
						}
					}
				}
			}
			logger.trace("Exit ChatTriggers.execute()");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean evaluateConditions(Map<String, Object> message, ArrayList<Document> triggerConditions,
			String companyId) {
		try {

			logger.trace("Enter ChatTriggers.evaluateConditions()");
			String messageJson = new ObjectMapper().writeValueAsString(message);
			String expressionAnd = "";
			String expressionOr = "";
			String dummyCondition = "1 == 1";

			String javascript = "";
			javascript += System.lineSeparator();
			javascript += "var inputMessage = " + messageJson + ";";
			javascript += System.lineSeparator();

			for (Document condition : triggerConditions) {
				String var = condition.getString("CONDITION");

				String operator = condition.getString("OPERATOR");
				String value = condition.getString("CONDITION_VALUE");
				String requirementType = condition.getString("REQUIREMENT_TYPE");
				String dataType = condition.getString("DATA_TYPE");
				String statement = null;
				statement = generateStatement(operator, var, value, dataType);

				if (requirementType.equalsIgnoreCase("All")) {
					expressionAnd += " && " + statement;
				} else if (requirementType.equalsIgnoreCase("Any")) {
					dummyCondition = " 1 == 2";
					expressionOr += "|| " + statement;
				}
			}
			String expression = "1 == 1 " + expressionAnd + " && (" + dummyCondition + " " + expressionOr + ")";
			ScriptEngineManager factory = new ScriptEngineManager();
			ScriptEngine engine = factory.getEngineByName("JavaScript");
			engine.eval(javascript);
			Boolean result = (Boolean) engine.eval(expression);
			logger.trace("Exit ChatTriggers.evaluateConditions()");
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.trace("Exit ChatTriggers.evaluateConditions()");
		return false;
	}

	private String generateStatement(String operator, String var, String value, String dataType) {
		String statement = "";
//		try {
//			logger.trace("Enter ChatTriggers.generateStatement()");
//			if (operator.equalsIgnoreCase("equals to")) {
//				if (dataType.equalsIgnoreCase("string")) {
//					return "inputMessage." + var + "== '" + value + "'";
//				} else if (dataType.equalsIgnoreCase("integer")) {
//					return "inputMessage." + var + " == " + value;
//				} else if (dataType.equalsIgnoreCase("boolean")) {
//					return "inputMessage." + var + "== " + value;
//				}
//			} else if (operator.equalsIgnoreCase("not equals")) {
//				if (dataType.equalsIgnoreCase("string")) {
//					return "inputMessage." + var + "!= " + "'" + value + "'";
//				} else if (dataType.equalsIgnoreCase("integer")) {
//					return "inputMessage." + var + " != " + value;
//				} else if (dataType.equalsIgnoreCase("boolean")) {
//					return "inputMessage." + var + "!= " + value;
//				}
//			} else if (operator.equalsIgnoreCase("is")) {
//				return "inputMessage." + var + " == '" + value + "'";
//			} else if (operator.equalsIgnoreCase("regex")) {
//				return "inputMessage." + var + ".match('" + value + "') != null";
//			} else if (operator.equalsIgnoreCase("contains")) {
//				return "inputMessage." + var + ".indexOf('" + value + "') != -1";
//			} else if (operator.equalsIgnoreCase("does not contain")) {
//				return "inputMessage." + var + ".indexOf('" + value + "') == -1";
//			} else if (operator.equalsIgnoreCase("less than")) {
//				if (dataType.equalsIgnoreCase("string")) {
//					return "inputMessage." + var + ".length < '" + value.length() + "'";
//				} else if (dataType.equalsIgnoreCase("integer")) {
//					return "inputMessage." + var + " < " + value;
//				}
//			} else if (operator.equalsIgnoreCase("greater than")) {
//				if (dataType.equalsIgnoreCase("string")) {
//					return "inputMessage." + var + ".length > '" + value.length() + "'";
//				} else if (dataType.equalsIgnoreCase("integer")) {
//					return "inputMessage." + var + " > " + value;
//				}
//			}
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		logger.trace("Exit ChatTriggers.generateStatement()");
		return statement;
	}

	public void executeActions(Document triggerDocument, String companyUUID, String chatId, String companyId,
			String sessionUUID, String sessionPassword) {
//		try {
//			logger.trace("Enter ChatTriggers.executeActions() companyUUID:  " + companyUUID + ", chatId: " + chatId
//					+ ",companyId:  " + companyId + ", sessionUUID:" + sessionUUID + ", sessionPassword"
//					+ sessionPassword);
//			if (triggerDocument.get("ACTIONS") != null) {
//				ArrayList<Document> triggerActions = (ArrayList) triggerDocument.get("ACTIONS");
//				if (triggerActions.size() > 0) {
//					for (Document triggerAction : triggerActions) {
//
//						MongoCollection<Document> roles = mongoTemplate.getCollection("roles_" + companyId);
//						Document agentRole = roles.find(Filters.eq("NAME", "Agent")).first();
//						String agentRoleId = agentRole.getObjectId("_id").toString();
//
//						JSONObject actionObj = new JSONObject(triggerAction);
//						String action = actionObj.getString("ACTION");
//						JSONArray values = actionObj.getJSONArray("VALUES");
//						if (action.equalsIgnoreCase("Send message to visitor")) {
//							JSONObject value1 = values.getJSONObject(0);
//							JSONObject value2 = values.getJSONObject(1);
//							String name = value1.getString("VALUE");
//							String message = value2.getString("VALUE");
//							DiscussionMessage msg = new DiscussionMessage();
//							Sender sender = new Sender();
//							sender.setRole(agentRoleId);
//							sender.setFirstName(name);
//							msg.setMessageType("trigger-message-visitor");
//							msg.setMessage(message);
//							msg.setCompanyUUID(companyUUID);
//							msg.setChatId(chatId);
//							msg.setDateCreated(new Timestamp(new Date().getTime()));
//							msg.setSender(sender);
//							String Topic = "topic/" + sessionUUID + "/" + sessionPassword;
//							this.template.convertAndSend(Topic, msg);
//						}
//					}
//				}
//			}
//
//			logger.trace("Exit ChatTriggers.executeActions() companyUUID:  " + companyUUID + ", chatId: " + chatId
//					+ ",companyId:  " + companyId + ", sessionUUID:" + sessionUUID + ", sessionPassword"
//					+ sessionPassword);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

}
