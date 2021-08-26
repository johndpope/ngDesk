package com.ngdesk.workflow.dao;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.Global;
import com.ngdesk.data.dao.DiscussionMessage;
import com.ngdesk.data.dao.PublishDiscussionMessage;
import com.ngdesk.data.dao.WorkflowExecutionInstance;
import com.ngdesk.repositories.ModuleEntryRepository;
import com.ngdesk.repositories.ModulesRepository;
import com.ngdesk.repositories.NotificationUserRepository;
import com.ngdesk.workflow.company.dao.Company;
import com.ngdesk.workflow.executor.dao.NodeOperations;
import com.ngdesk.workflow.module.dao.DataType;
import com.ngdesk.workflow.module.dao.Module;
import com.ngdesk.workflow.module.dao.ModuleField;
import com.ngdesk.workflow.notification.dao.Notification;
import com.ngdesk.workflow.notification.dao.NotificationUser;

@Component
public class FindAgentAndAssignNode extends Node {

	@Autowired
	Environment env;

	@Autowired
	Global global;

	private SimpMessagingTemplate template;

//	@Autowired
//	PushNotification browserNotification;

	@Autowired
	RedissonClient redisson;

	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	ModuleEntryRepository entryRepository;

	@Autowired
	NodeOperations nodeOperations;

	@Autowired
	RedisTemplate<String, Notification> redisTemplate;

	@Autowired
	NotificationUserRepository notificationUserRepository;

	@Autowired
	RabbitTemplate rabbitTemplate;

	// TODO: Improve assignment of chat to agent this is basic
	@Override
	public void execute(WorkflowExecutionInstance instance) {
		try {
			Map<String, Object> inputMessage = instance.getEntry();

			Company company = instance.getCompany();
			if (company != null) {
				RMap<String, Map<String, Map<String, Object>>> companiesMap = redisson.getMap("companiesUsers");
				String subdomain = company.getCompanySubdomain();
				String companyId = company.getCompanyId();
				String companyUUID = company.getCompanyUuid();
				int maxNoOfChatsPerAgent = company.getMaxChatPerAgent();

				List<String> rolesWhoCanChat = company.getRolesWithChat();

				String moduleId = instance.getModule().getModuleId();

				if (new ObjectId().isValid(moduleId)) {

					Optional<Module> optionalModule = modulesRepository.findById(moduleId,
							"modules_" + instance.getCompany().getCompanyId());
					Module module = optionalModule.get();

//					if (inputMessage.get("TYPE").toString().equalsIgnoreCase("CHAT")) {

					Map<String, Object> userEntry = entryRepository.findById(instance.getUserId(), "Users_" + companyId)
							.orElse(null);
					if (userEntry != null) {

						if (rolesWhoCanChat.contains(userEntry.get("ROLE").toString())) {
							Map<String, List<String>> userTeamsMap = new HashMap<String, List<String>>();
							Map<String, String> userNamesMap = new HashMap<String, String>();
							Map<String, Object> userContact = entryRepository
									.findById(userEntry.get("CONTACT").toString(), "Contacts_" + companyId)
									.orElse(null);
							if (userContact != null) {

								String firstName = userContact.get("FIRST_NAME").toString();
								List<String> teamIds = (List<String>) userEntry.get("TEAMS");

								userNamesMap.put(instance.getUserId(), firstName);
								userTeamsMap.put(instance.getUserId(), teamIds);
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
										List<ModuleField> fields = module.getFields();
										for (ModuleField field : fields) {
											DataType dataType = field.getDataType();
											if (dataType.getDisplay().equalsIgnoreCase("Discussion")) {
												discussionFieldName = field.getName();
												discussionFieldId = field.getFieldId();
											}
										}

										String message = global.getFile("metadata_chat_agent_join.html");
										Map<String, Object> metaDataMessage = new HashMap<String, Object>();

										message = message.replace("NAME_REPLACE", userNamesMap.get(agentAssignedId));
										message = message.replace("DATE_TIME_REPLACE",
												new SimpleDateFormat("MMM d y h:mm:ss a")
														.format(new Timestamp(new Date().getTime())));

										String systemUserUUID = nodeOperations.getSystemUser(companyId);

										metaDataMessage.put("COMPANY_UUID", companyUUID);
										metaDataMessage.put("MESSAGE_ID", UUID.randomUUID().toString());
										metaDataMessage.put("USER_UUID", systemUserUUID);

										String moduleName = module.getName();

										Optional<Map<String, Object>> optionalEntry = entryRepository
												.findEntriesBySessionUuid(inputMessage.get("SESSION_UUID").toString(),
														moduleName.replaceAll("\\s+", "_") + "_" + companyId);
										Map<String, Object> entry = optionalEntry.get();

										DiscussionMessage builtMessage = nodeOperations.buildMetaDataPayload(message,
												instance);
										Document messageDoc = Document
												.parse(new ObjectMapper().writeValueAsString(builtMessage));
										if (discussionFieldName != null) {

											// INSERT META_DATA INTO MESSAGES USING DISCUSSION CONTROLLER
//												builtMessage.get(0).remove("DATE_CREATED");
											DiscussionMessage discussionMessage = new ObjectMapper().readValue(
													new ObjectMapper().writeValueAsString(builtMessage).toString(),
													DiscussionMessage.class);
											discussionMessage.setSubdomain(company.getCompanySubdomain());
											discussionMessage.setModuleId(moduleId);
											discussionMessage.setDataId(entry.get("_id").toString());
											nodeOperations.addToDiscussionQueue(new PublishDiscussionMessage(
													discussionMessage, instance.getCompany().getCompanySubdomain(),
													instance.getUserId(), true));
										}

										// SEND META_DATA TO ALL PARTIES INVOLVED

//											String confirmationTopic = "topic/chat/"
//													+ inputMessage.get("SESSION_UUID").toString();
//											this.template.convertAndSend(confirmationTopic,
//													messageDoc.toJson().toString());
//
//											MongoCollection<Document> notificationsCollection = mongoTemplate
//													.getCollection("notifications_" + companyId);

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

										Notification notifyUser = new Notification(companyId, moduleId,
												inputMessage.get("DATA_ID").toString(), userEntry.get("_id").toString(),
												new Date(), new Date(), true,
												userNamesMap.get(agentAssignedId) + " has joined the chat");
										addToNotificationQueue(notifyUser);

										Notification notifyAgent = new Notification(companyId, moduleId,
												inputMessage.get("DATA_ID").toString(), agentAssignedId, new Date(),
												new Date(), false, "You have been assigned a new chat");
										addToNotificationQueue(notifyAgent);
//
//											String agentTopic = "topic/notify/" + agentAssignedId;
//											String userTopic = "topic/notify/"
//													+ inputMessage.get("SESSION_UUID").toString();
//
//											this.template.convertAndSend(agentTopic, notificationMessage.toString());
//
//											notificationsCollection
//													.insertOne(Document.parse(notificationMessage.toString()));
//
										NotificationUser notifyMessage = new NotificationUser();
//
//										notifyMessage.setMessageType("NOTIFICATION");
//
//										notifyMessage.setDateCreated(new Date());
//										notifyMessage.setDataId(inputMessage.get("DATA_ID").toString());
//										notifyMessage.setModuleId(moduleId);
//										notifyMessage.setModuleName(module.getName());

//											// NOTIFY END USER
//										notifyMessage
//												.setMessage(userNamesMap.get(agentAssignedId) + " has joined the chat");
//										addToNotificationQueue(notifyMessage);
//
//											// NOTIFY AGENT
//										notifyMessage.setMessage("You have been assigned a new chat");
//										notifyMessage.setRead(false);
//										notifyMessage.setNotificationUuid(UUID.randomUUID().toString());
//										notifyMessage.setRecipientId(agentAssignedId);
//
//										addToNotificationQueue(notifyMessage);
//											notificationUserRepository.save(notifyMessage,
//													"notifications_" + companyId);

//											notificationsCollection.insertOne(Document.parse(notifyMessage.toString()));

										// FIREBASE PUSH NOTIFICATION

//											MongoCollection<Document> tokenCollection = mongoTemplate
//													.getCollection("user_tokens_" + companyId);
//											Document agentDoc = usersCollection
//													.find(Filters.eq("_id", new ObjectId(agentAssignedId))).first();
//											Document userDoc = tokenCollection
//													.find(Filters.eq("USER_UUID", agentDoc.getString("USER_UUID")))
//													.first();
//											String url = "https://" + subdomain + ".ngdesk.com/render/" + moduleId
//													+ "/detail/" + entry.get("_id").toString() + "";
//											browserNotification.sendWebNotifications(userDoc,
//													"You have been assigned a new chat", subdomain, url);
//											browserNotification.sendAndroidNotifications(userDoc,
//													"You have been assigned a new chat, please respond from Web",
//													"ngDesk notification");
//											browserNotification.sendIosNotifications(userDoc,
//													"You have been assigned a new chat, please respond from Web");

										List<String> list = new ArrayList<String>();
										list.add(agentAssignedId);
										inputMessage.put("AGENTS", list);

									}
								}
							}
						}
					}

//					}

				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
//		resultMap.put("INPUT_MESSAGE", inputMessage);

//		return resultMap;
	}

	@Override
	public void executeNextNode(WorkflowExecutionInstance instance) {

		logOnEnter(instance);

		Workflow workflow = instance.getWorkflow();

		Stage currentStage = workflow.getStages().stream().filter(stage -> stage.getId().equals(instance.getStageId()))
				.findFirst().orElse(null);
		if (currentStage != null) {

			Node currentNode = currentStage.getNodes().stream()
					.filter(node -> node.getNodeId().equals(instance.getNodeId())).findFirst().orElse(null);

			if (currentNode != null && currentNode.getConnections().size() > 0) {
				Connection connection = currentNode.getConnections().get(0);
				if (connection != null && connection.getToNode() != null) {
					Stage nextStage = workflow.getStages().stream()
							.filter(stage -> stage.getNodes().stream()
									.anyMatch(node -> node.getNodeId().equals(connection.getToNode())))
							.findFirst().orElse(null);
					if (nextStage != null) {
						Node nextNode = nextStage.getNodes().stream()
								.filter(node -> node.getNodeId().equals(connection.getToNode())).findFirst()
								.orElse(null);
						if (nextNode != null) {
							instance.setNodeId(nextNode.getNodeId());
							instance.setStageId(nextStage.getId());

							updateWorkflowInstance(instance);

							logOnExit(instance);
							rabbitTemplate.convertAndSend("execute-nodes", instance);
						}
					}
				}
			}
		}
	}

	@Override
	public boolean validateNodeOnSave(Optional<?> repo) {
		// TODO Auto-generated method stub
		return false;
	}

	public void addToNotificationQueue(Notification notification) {
		redisTemplate.convertAndSend("notification", notification);
	}
}
