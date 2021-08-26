package com.ngdesk.nodes;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;
import org.redisson.api.RMap;
import org.redisson.api.RSortedSet;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.ngdesk.Global;
import com.ngdesk.data.dao.DataService;
import com.ngdesk.data.dao.PublishDiscussionMessage;
import com.ngdesk.discussion.DiscussionController;
import com.ngdesk.discussion.DiscussionMessage;

@Component
public class StartEscalation extends Node {
	private static final Logger logger = LogManager.getLogger(StartEscalation.class);

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	private Global global;

	@Autowired
	private Environment environment;

	@Autowired
	RedissonClient redisson;

	@Autowired
	private ParentNode parentNode;

	@Autowired
	DiscussionController discussionController;
	
	@Autowired
	DataService dataService;

	@Override
	public Map<String, Object> executeNode(Document node, Map<String, Object> inputMessage) {
		Map<String, Object> resultMap = new HashMap<String, Object>();

		logger.trace("Enter StartEscalation.executeNode() : inputMessage " + inputMessage.toString());

		try {
			// GET ESCALATION ID FROM VALUES OF NODE
			Document values = (Document) node.get("VALUES");
			String escalationId = values.getString("ESCALATION");
			String subject = "";
			String body = "";

			if (values.get("SUBJECT") != null) {
				subject = values.getString("SUBJECT");
			}
			if (values.get("BODY") != null) {
				body = values.getString("BODY");
			}

			// GET COMPANY ID
			Document company = global.getCompanyFromUUID(inputMessage.get("COMPANY_UUID").toString());
			String companyId = company.getObjectId("_id").toString();

			if (new ObjectId().isValid(escalationId)) {
				MongoCollection<Document> escalationCollection = mongoTemplate
						.getCollection("escalations_" + companyId);
				Document escalationDocument = escalationCollection.find(Filters.eq("_id", new ObjectId(escalationId)))
						.first();
				if (escalationDocument != null) {

					String escalationName = escalationDocument.getString("NAME");

					String[] entriesToEscalted = { "CREATED_ENTRIES", "UPDATED_ENTRIES" };

					for (String entryToBeEscalated : entriesToEscalted) {
						// GET ALL THE ENTRIES FOR CREATED ENTRIES / UPDATED ENTRIES
						if (inputMessage.containsKey(entryToBeEscalated)
								&& inputMessage.get(entryToBeEscalated) != null) {

							List<Map<String, Object>> entries = (List<Map<String, Object>>) inputMessage
									.get(entryToBeEscalated);

							logger.debug("HAS UNESCALATED ENTRIES: " + entries.size());
							for (Map<String, Object> entry : entries) {
								String entryId = entry.get("ENTRY_ID").toString();
								String moduleId = entry.get("MODULE_ID").toString();

								if (addEscaltiontoCollection(escalationId, companyId, entryId, moduleId, subject,
										body)) {

									MongoCollection<Document> modulesCollection = mongoTemplate
											.getCollection("modules_" + companyId);

									Document module = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId)))
											.first();

									String discussionFieldName = null;
									String discussionFieldId = null;
									if (module != null) {
										List<Document> fields = (List<Document>) module.get("FIELDS");
										for (Document field : fields) {
											Document dataType = (Document) field.get("DATA_TYPE");
											if (dataType.getString("DISPLAY").equals("Discussion")) {
												discussionFieldName = field.getString("NAME");
												discussionFieldId = field.getString("FIELD_ID");
												break;
											}
										}
									}
									if (discussionFieldName != null) {
										String moduleName = module.getString("NAME");
										MongoCollection<Document> entriesCollection = mongoTemplate
												.getCollection(moduleName.replaceAll("\\s+", "_") + "_" + companyId);

										String escalationMetadata = global.getFile("escalation_start_metadata.html");
										String currentTime = new SimpleDateFormat("MMM d y h:mm:ss a")
												.format(new Timestamp(new Date().getTime()));

										escalationMetadata = escalationMetadata.replaceAll("ESCALATION_NAME",
												escalationName);
										escalationMetadata = escalationMetadata.replaceAll("[\\n\\t]", " ");
										Map<String, Object> inputMessage1 = new HashMap<String, Object>();

										String systemUserUUID = global.getSystemUser(companyId);

										inputMessage1.put("COMPANY_UUID", company.getString("COMPANY_UUID"));
										inputMessage1.put("MESSAGE_ID", UUID.randomUUID().toString());
										inputMessage1.put("USER_UUID", systemUserUUID);

										// INSERT META_DATA INTO MESSAGES USING DISCUSSION CONTROLLER

										List<Map<String, Object>> discussion = global
												.buildDiscussionPayload(inputMessage1, escalationMetadata, "META_DATA");
										discussion.get(0).remove("DATE_CREATED");
										DiscussionMessage discussionMessage = new ObjectMapper().readValue(
												new ObjectMapper().writeValueAsString(discussion.get(0)).toString(),
												DiscussionMessage.class);
										discussionMessage.setSubdomain(company.getString("COMPANY_SUBDOMAIN"));
										discussionMessage.setModuleId(moduleId);
										discussionMessage.setEntryId(entryId);
										
										String systemAdminUserId = dataService.generateSystemUserEntry(companyId).getObjectId("_id").toString();
										dataService.addToDiscussionQueue(new PublishDiscussionMessage(discussionMessage,
												company.getString("COMPANY_SUBDOMAIN"), systemAdminUserId, true));

									}
								}
							}
							inputMessage.put(entryToBeEscalated, entries);
						}
					}
				}
			}

			ArrayList<Document> connections = (ArrayList<Document>) node.get("CONNECTIONS_TO");
			if (connections.size() == 1) {
				Document connection = connections.get(0);
				resultMap.put("NODE_ID", connection.getString("TO_NODE"));
			}
			resultMap.put("INPUT_MESSAGE", inputMessage);
		} catch (Exception e) {
			e.printStackTrace();
		}

		logger.trace("Exit StartEscalation.executeNode()");
		return resultMap;
	}

	// Adding Escalation to Escalated Entries Collection
	public boolean addEscaltiontoCollection(String escalationId, String companyId, String entryId, String moduleId,
			String subject, String body) {
		try {
			String collectionName = "escalated_entries_" + companyId;
			MongoCollection<Document> escalationEntriescollection = mongoTemplate.getCollection(collectionName);
			String escalationCollectionName = "escalations_" + companyId;
			MongoCollection<Document> escalationcollection = mongoTemplate.getCollection(escalationCollectionName);

			Document escalationDocument = escalationcollection.find(Filters.eq("_id", new ObjectId(escalationId)))
					.first();

			// Adding first rule of escalation to redis
			ArrayList<Document> ruleDocuments = (ArrayList) escalationDocument.get("RULES");

			Document doc = escalationEntriescollection
					.find(Filters.and(Filters.eq("ESCALATION_ID", escalationId), Filters.eq("ENTRY_ID", entryId)))
					.first();
			if (doc == null) {
				addEscalationToRedis(escalationId, ruleDocuments.get(0), companyId, entryId);
			}

			Document document = escalationEntriescollection
					.find(Filters.and(Filters.eq("ENTRY_ID", entryId), Filters.eq("ESCALATION_ID", escalationId)))
					.first();

			if (document == null) {
				JSONObject entryObj = new JSONObject();

				entryObj.put("ENTRY_ID", entryId);
				entryObj.put("MODULE_ID", moduleId);
				entryObj.put("ESCALATION_ID", escalationId);
				entryObj.put("SUBJECT", subject);
				entryObj.put("BODY", body);
				Document escalatedEntryDoc = Document.parse(entryObj.toString());
				escalationEntriescollection.insertOne(escalatedEntryDoc);
				return true;

			} else {
				return false;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
	}

	// Adding Escalation To Redis

	public void addEscalationToRedis(String escalationId, Document ruleDocument, String companyId, String entryId) {
		try {

			String epochDate = "01/01/1970";
			Date date = new SimpleDateFormat("dd/MM/yyyy").parse(epochDate);
			Timestamp epoch = new Timestamp(date.getTime());

			Timestamp today = new Timestamp(new Date().getTime());

			int minutesAfter = ruleDocument.getInteger("MINS_AFTER");
			long millisec = TimeUnit.MINUTES.toMillis(minutesAfter);
			long currentTimeDiff = today.getTime() + millisec - epoch.getTime();

			RSortedSet<Long> escalationTimes = redisson.getSortedSet("escalationTimes");
			RMap<Long, String> escalationRules = redisson.getMap("escalationRules");

			while (escalationTimes.contains(currentTimeDiff)) {
				currentTimeDiff += 1;
			}

			escalationTimes.add(currentTimeDiff);

			JSONObject ruleJson = new JSONObject(ruleDocument.toJson().toString());
			ruleJson.put("COMPANY_ID", companyId);
			ruleJson.put("ESCALATION_ID", escalationId);
			ruleJson.put("ENTRY_ID", entryId);

			logger.trace("Added to redis: " + currentTimeDiff + ruleJson.toString());

			escalationRules.put(currentTimeDiff, ruleJson.toString());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
