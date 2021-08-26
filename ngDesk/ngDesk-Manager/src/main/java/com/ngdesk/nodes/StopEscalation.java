package com.ngdesk.nodes;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.types.ObjectId;
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
public class StopEscalation extends Node {
	private static final Logger logger = LogManager.getLogger(StopEscalation.class);

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	private Global global;

	@Autowired
	private Environment environment;

	@Autowired
	private ParentNode parentNode;

	@Autowired
	DiscussionController discussionController;

	@Autowired
	RedissonClient client;
	
	@Autowired
	DataService dataService;

	@Override
	public Map<String, Object> executeNode(Document node, Map<String, Object> inputMessage) {
		Map<String, Object> resultMap = new HashMap<String, Object>();

		logger.trace("Enter StopEscalation.executeNode()");

		try {
			// GET COMPANY ID
			Document company = global.getCompanyFromUUID(inputMessage.get("COMPANY_UUID").toString());
			String companyId = company.getObjectId("_id").toString();

			String[] entriesToEscalted = { "CREATED_ENTRIES", "UPDATED_ENTRIES" };
			for (String entryToBeEscalated : entriesToEscalted) {
				if (inputMessage.containsKey(entryToBeEscalated) && inputMessage.get(entryToBeEscalated) != null) {

					List<Map<String, Object>> entries = (List<Map<String, Object>>) inputMessage
							.get(entryToBeEscalated);

					HashSet<String> entryIds = new HashSet<String>();
					Map<String, String> entryModulesMap = new HashMap<String, String>();

					for (Map<String, Object> entry : entries) {
						String entryId = entry.get("ENTRY_ID").toString();
						String moduleId = entry.get("MODULE_ID").toString();
						entryIds.add(entryId);
						entryModulesMap.put(entryId, moduleId);
					}
					Map<String, Document> deletedEntries = deleteEscalationsFromRedis(companyId, entryIds);
					for (String entryId : deletedEntries.keySet()) {

						if (deletedEntries.get(entryId) != null) {
							Document deletedEntry = deletedEntries.get(entryId);

							String escalationId = deletedEntry.getString("ESCALATION_ID");

							MongoCollection<Document> escalationsCollection = mongoTemplate
									.getCollection("escalations_" + companyId);
							Document escalationDocument = escalationsCollection
									.find(Filters.eq("_id", new ObjectId(escalationId))).first();

							String escalationName = "";

							if (escalationDocument != null) {
								escalationName = escalationDocument.getString("NAME");
							}

							// ESCALATION HAS BEEN STOPPED FOR THIS ENTRY
							String moduleId = entryModulesMap.get(entryId);
							MongoCollection<Document> modulesCollection = mongoTemplate
									.getCollection("modules_" + companyId);
							Document module = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();

							if (module != null) {
								String discussionFieldName = null;
								String discussionFieldId = null;
								List<Document> fields = (List<Document>) module.get("FIELDS");
								for (Document field : fields) {
									Document dataType = (Document) field.get("DATA_TYPE");
									if (dataType.getString("DISPLAY").equals("Discussion")) {
										discussionFieldName = field.getString("NAME");
										discussionFieldId = field.getString("FIELD_ID");
										break;
									}
								}
								if (discussionFieldName != null) {
									String moduleName = module.getString("NAME");

									MongoCollection<Document> entriesCollection = mongoTemplate
											.getCollection(moduleName.replaceAll("\\s+", "_") + "_" + companyId);

									String escalationMetadata = global.getFile("escalation_stop_metadata.html");
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

									List<Map<String, Object>> discussion = global.buildDiscussionPayload(inputMessage1,
											escalationMetadata, "META_DATA");
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
					}
				}
			}

			ArrayList<Document> connections = (ArrayList<Document>) node.get("CONNECTIONS_TO");

			// EMAIL NODE HAS ONLY ONE CONNECTION
			if (connections.size() == 1) {
				Document connection = connections.get(0);
				resultMap.put("NODE_ID", connection.getString("TO_NODE"));
			}
			resultMap.put("INPUT_MESSAGE", inputMessage);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultMap;
	}

	// Delete Escalation From Redis
	public Map<String, Document> deleteEscalationsFromRedis(String companyId, HashSet<String> entryIds) {
		try {
			String collectionName = "escalated_entries_" + companyId;
			MongoCollection<Document> escalationEntriescollection = mongoTemplate.getCollection(collectionName);

			// MAP TO RETURN
			Map<String, Document> deletedEntries = new HashMap<String, Document>();

			for (String entryId : entryIds) {
				Document deletedEntry = escalationEntriescollection.findOneAndDelete(Filters.eq("ENTRY_ID", entryId));
				deletedEntries.put(entryId, deletedEntry);
			}

			return deletedEntries;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
