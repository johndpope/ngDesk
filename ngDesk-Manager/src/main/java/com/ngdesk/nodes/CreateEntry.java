package com.ngdesk.nodes;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.ngdesk.Global;
import com.ngdesk.SendMail;
import com.ngdesk.flowmanager.Attachment;

@Component
public class CreateEntry extends Node {

	private static final Logger logger = LogManager.getLogger(CreateEntry.class);

	@Autowired
	private HttpRequestNode httpRequestNode;

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	private Global global;

	@Autowired
	private Environment env;

	@Autowired
	private SendMail sendMail;

	@Override
	public Map<String, Object> executeNode(Document node, Map<String, Object> inputMessage) {
		Map<String, Object> resultMap = new HashMap<String, Object>();

		logger.trace("Enter CreateEntry.executeNode()");

		try {

			// MOVED OVER TO FUNCTION
			String response = createEntry(node, inputMessage);

			if (response != null) {
				ObjectMapper mapper = new ObjectMapper();
				Map<String, Object> entry = mapper.readValue(response, Map.class);
				Map<String, Object> createEntry = new HashMap();
				createEntry.put("Entry", entry);
				inputMessage.put(node.getString("NAME"), createEntry);
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

		logger.trace("Exit CreateEntry.executeNode() ");
		return resultMap;
	}

	public String createEntry(Document node, Map<String, Object> inputMessage) {

		logger.trace("Enter CreateEntry.createEntry()");
		String response = null;

		try {
			String message = new ObjectMapper().writeValueAsString(inputMessage);

			Document values = (Document) node.get("VALUES");
			String moduleId = values.getString("MODULE");

			String companyUUID = inputMessage.get("COMPANY_UUID").toString();

			Document company = global.getCompanyFromUUID(inputMessage.get("COMPANY_UUID").toString());
			String subdomain = company.getString("COMPANY_SUBDOMAIN");
			String companyId = company.getObjectId("_id").toString();

			Document module = global.getModuleFromId(moduleId, companyId);
			String moduleName = module.getString("NAME");

			ArrayList<Document> fields = (ArrayList<Document>) values.get("FIELDS");
			Map<String, Object> payloadMap = new HashMap<>();

			String reg = "\\{\\{(.*)\\}\\}";
			Pattern r = Pattern.compile(reg);
			String attachmentValue = null;
			String discussionFieldName = null;

			for (Document field : fields) {
				String fieldId = field.getString("FIELD");
				Document fieldDocument = global.getFieldFromId(fieldId, moduleName, companyId);
				String name = fieldDocument.getString("NAME");

				List<String> val = (List<String>) field.get("VALUE");

				Object valuePattern = null;

				if (val.size() > 1) {
					valuePattern = new ObjectMapper().writeValueAsString(val);
				} else if (val.size() == 1) {
					valuePattern = val.get(0);
				}

				String attachmentPattern = null;

				Object value = null;

				if (field.containsKey("ATTACHMENTS") && field.get("ATTACHMENTS") != null) {
					attachmentPattern = field.getString("ATTACHMENTS");
					Matcher matcher = r.matcher(attachmentPattern);

					if (matcher.find()) {
						String path = matcher.group(1);
						String sections[] = path.split("\\.");
						if (sections.length > 1 && sections[0].equalsIgnoreCase("INPUTMESSAGE")) {
							Object obj = inputMessage;
							for (int j = 1; j < sections.length; ++j) {
								obj = ((Map<String, Object>) obj).get(sections[j]);
								if (obj == null) {
									break;
								}
							}
							if (obj != null) {
								attachmentValue = new ObjectMapper().writeValueAsString(obj);
							}
						}
					}
				}

				Matcher m = r.matcher(valuePattern.toString());
				if (m.find()) {
					String path = m.group(1);

					String sections[] = path.split("\\.");
					if (sections.length > 1 && sections[0].equalsIgnoreCase("INPUTMESSAGE")) {
						Object obj = inputMessage;
						for (int j = 1; j < sections.length; ++j) {
							obj = ((Map<String, Object>) obj).get(sections[j]);
							if (obj == null)
								break;
						}
						if (obj != null) {
							value = obj;
						}
					}
				} else {

					Document dataType = (Document) fieldDocument.get("DATA_TYPE");
					if (dataType.getString("DISPLAY").equals("Relationship")) {
						String relationshipType = fieldDocument.getString("RELATIONSHIP_TYPE");
						if (relationshipType.equalsIgnoreCase("Many to Many")) {
							value = val;
						} else {
							value = valuePattern;
						}
					} else {
						value = valuePattern;
					}
				}

				// CHECK FOR DISCUSSION FIELD
				Document dataType = (Document) fieldDocument.get("DATA_TYPE");

				if (dataType.getString("DISPLAY").equals("Discussion")) {
					discussionFieldName = fieldDocument.getString("NAME");
					payloadMap.put(name, global.buildDiscussionPayload(inputMessage, value.toString(), "MESSAGE"));
				} else if (dataType.getString("DISPLAY").equals("List Text")) {
					List<String> list = new ArrayList<String>();
					if (value != null) {
						try {
							list = (List<String>) value;
						} catch (ClassCastException e) {
							logger.trace("Not a valid list");
							String[] items = value.toString().split(",");
							for (String item : items) {
								if (fieldDocument.getBoolean("IS_LIST_TEXT_UNIQUE") && !list.contains(item)) {
									list.add(item);
								} else if (!fieldDocument.getBoolean("IS_LIST_TEXT_UNIQUE")) {
									list.add(item);
								}
							}
						}
						payloadMap.put(name, list);
					}
				} else if (dataType.getString("DISPLAY").equals("Picklist (Multi-Select)")) {
					List<String> picklistMultiSelect = new ArrayList<String>();
					value = val;
					if (value != null) {
						try {
							picklistMultiSelect = (List<String>) value;
						} catch (ClassCastException e) {
							logger.trace("Not a valid picklistMultiSelect ");
							String[] items = value.toString().split(",");
							for (String item : items) {
								if (!picklistMultiSelect.contains(item)) {
									picklistMultiSelect.add(item);
								}
							}
						}
						payloadMap.put(name, picklistMultiSelect);
					}
				} else {
					if (name.equals("CURRENT_TIMESTAMP")) {
						payloadMap.put(name, new Date());
					} else if (value != null) {
						payloadMap.put(name, value);
					}
				}

			}

			if (discussionFieldName != null) {
				if (payloadMap.containsKey(discussionFieldName) && payloadMap.get(discussionFieldName) != null) {

					List<Map<String, Object>> messages = (List<Map<String, Object>>) payloadMap
							.get(discussionFieldName);
					for (Map<String, Object> discussionMessage : messages) {

						List<Attachment> attachments = new ArrayList<Attachment>();
						if (attachmentValue != null && attachmentValue.length() > 0) {
							attachments = new ObjectMapper().readValue(attachmentValue,
									new TypeReference<List<Attachment>>() {
									});
						}
						discussionMessage.put("ATTACHMENTS", attachments);
					}
				}
			}

			boolean isTrigger = false;
			if (inputMessage.containsKey("IS_TRIGGER")) {
				isTrigger = (boolean) inputMessage.get("IS_TRIGGER");
			}

			String url = "http://" + env.getProperty("dataservice.host") + ":8087/modules/" + moduleId
					+ "/data?is_trigger=" + isTrigger + "&company_id=" + companyId + "&user_uuid="
					+ inputMessage.get("USER_UUID").toString();

			String sourceType = inputMessage.get("TYPE").toString();
			payloadMap.put("SOURCE_TYPE", sourceType);
			String payload = new ObjectMapper().writeValueAsString(payloadMap);

			logger.debug("URL: " + url);
			logger.debug("PAYLOAD: " + payload);

			JSONObject httpResponse = httpRequestNode.request(url, payload, "POST", null);

			if (httpResponse.has("RESPONSE")) {
				response = httpResponse.getString("RESPONSE");
			}

			logger.trace("RESPONSE: " + response);
 
			// ToDo Add response to inputMessage
			if (response != null) {
				JSONObject responseObj = new JSONObject(response);
				String entryId = responseObj.getString("DATA_ID");

				Map<String, Object> entry = new HashMap<String, Object>();
				entry.put("ENTRY_ID", entryId);
				entry.put("MODULE_ID", moduleId);
				entry.put("ESCALATED", false);
				List<Map<String, Object>> entries = new ArrayList<Map<String, Object>>();
				if (inputMessage.get("CREATED_ENTRIES") != null) {
					entries = (List<Map<String, Object>>) inputMessage.get("CREATED_ENTRIES");
					entries.add(entry);
					inputMessage.put("CREATED_ENTRIES", entries);
				}
				entries.add(entry);
				inputMessage.put("CREATED_ENTRIES", entries);
			} else {
				// CALL FAILED:
				String subject = "Call Failed on CreateEntry for " + subdomain;
				String body = url;
				body += "<br/><br/>" + StringEscapeUtils.escapeJava(payload);
				body += "<br/><br/> Response: " + httpResponse.getString("ERROR");
				body += "<br/><br/> Response Code: " + httpResponse.getInt("RESPONSE_CODE");

				String environment = env.getProperty("env");
				if (environment.equals("prd")) {
					sendMail.send("spencer@allbluesolutions.com", "support@ngdesk.com", subject, body);
					sendMail.send("sharath.satish@allbluesolutions.com", "support@ngdesk.com", subject, body);
				}

			}

			if (sourceType.equalsIgnoreCase("email") && response != null) {
				String collectionName = "email_entries_" + companyId;
				MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
				String inputMessageString = new ObjectMapper().writeValueAsString(inputMessage);

				JSONObject responseJson = new JSONObject(response);
				String entryId = responseJson.getString("DATA_ID");

				JSONObject payloadMapJson = new JSONObject();
				payloadMapJson.put("ENTRY_ID", entryId);
				payloadMapJson.put("INPUT_MESSAGE", new JSONObject(inputMessageString));

				Document document = Document.parse(payloadMapJson.toString());
				collection.insertOne(document);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.trace("Exit CreateEntry.createEntry()");
		return response;
	}

}
