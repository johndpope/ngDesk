package com.ngdesk.nodes;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.ngdesk.Global;
import com.ngdesk.SendMail;
import com.ngdesk.flowmanager.Attachment;

@Component
public class UpdateEntry extends Node {

	private static final Logger logger = LogManager.getLogger(UpdateEntry.class);

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Global global;

	@Autowired
	private Environment env;

	@Autowired
	private HttpRequestNode httpRequestNode;

	@Autowired
	private SendMail sendMail;

	@Override
	public Map<String, Object> executeNode(Document node, Map<String, Object> inputMessage) {

		Map<String, Object> resultMap = new HashMap<String, Object>();

		logger.trace("Enter UpdateEntry.executeNode()");

		try {
			String companyUUID = (String) inputMessage.get("COMPANY_UUID");
			Document company = global.getCompanyFromUUID(inputMessage.get("COMPANY_UUID").toString());
			String subdomain = company.getString("COMPANY_SUBDOMAIN");
			String companyId = global.getCompanyId(companyUUID);

			// GET INFORMATION FROM NODE
			Document values = (Document) node.get("VALUES");
			String moduleId = values.getString("MODULE");

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			Document module = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();

			if (module != null) {
				String moduleName = module.getString("NAME");

				ArrayList<Document> fields = (ArrayList<Document>) values.get("FIELDS");
				String entry = values.getString("ENTRY_ID");
				String reg = "\\{\\{(.*)\\}\\}";
				Pattern r = Pattern.compile(reg);
				Matcher m = r.matcher(entry);
				String entryId = null;
				if (m.find()) {
					String path = m.group(1);
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
							entryId = obj.toString();
						}
					}
				} else {
					entryId = entry;
				}
				MongoCollection<Document> entriesCollection = mongoTemplate
						.getCollection(moduleName.replaceAll("\\s+", "_") + "_" + companyId);
				if (new ObjectId().isValid(entryId)) {
					Document entryDoc = new Document();

					Document existingEntry = entriesCollection.find(Filters.and(Filters.eq("DELETED", false),
							Filters.eq("EFFECTIVE_TO", null), Filters.eq("_id", new ObjectId(entryId)))).first();

					if (entryDoc != null) {
						String attachmentValue = null;
						String discussionFieldName = null;
						String messageId = null;
						for (Document field : fields) {
							String fieldId = field.getString("FIELD");

							Document fieldDocument = global.getFieldFromId(fieldId, moduleName, companyId);
							String name = fieldDocument.getString("NAME");
							List<String> val = (List<String>) field.get("VALUE");
							Document dataType = (Document) fieldDocument.get("DATA_TYPE");
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

							Matcher valueMatcher = r.matcher(valuePattern.toString());

							if (valueMatcher.find()) {
								String path = valueMatcher.group(1);
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
										value = obj;
									}
								}
							} else {
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
							if (dataType.getString("DISPLAY").equals("Discussion")) {
								discussionFieldName = fieldDocument.getString("NAME");
								List<Map<String, Object>> discussionList = null;
								discussionList = global.buildDiscussionPayload(inputMessage, value.toString(),
										"MESSAGE");

								entryDoc.put(name, new ArrayList<Document>());
								if (discussionList != null) {
									Map<String, Object> discussionObj = discussionList.get(0);
									Document messageDoc = Document
											.parse(new ObjectMapper().writeValueAsString(discussionObj));
									messageId = messageDoc.getString("MESSAGE_ID");
									List<Document> messages = new ArrayList<Document>();
									messages.add(messageDoc);
									entryDoc.put(name, messages);
								}
							} else if (dataType.getString("DISPLAY").equals("List Text")) {

								List<String> list = (List<String>) existingEntry.get(name);
								if (list == null) {
									list = new ArrayList<String>();
								}
								if (value != null) {
									List<String> newList = new ArrayList<String>();
									try {
										newList = (List<String>) value;
									} catch (ClassCastException e) {
										String[] items = value.toString().split(",");
										for (String item : items) {
											newList.add(item);
										}
									}

									for (String item : newList) {
										if (fieldDocument.getBoolean("IS_LIST_TEXT_UNIQUE") && !list.contains(item)) {
											list.add(item);
										} else if (!fieldDocument.getBoolean("IS_LIST_TEXT_UNIQUE")) {
											list.add(item);
										}
									}
									entryDoc.put(name, list);

								}
							} else if (dataType.getString("DISPLAY").equals("Picklist (Multi-Select)")) {

								List<String> picklistMultiSelect = new ArrayList<String>();
								value = val;

								if (value != null) {

									List<String> newpicklistMultiSelect = new ArrayList<String>();
									try {
										newpicklistMultiSelect = (List<String>) value;

									} catch (ClassCastException e) {
										String[] items = value.toString().split(",");
										for (String item : items) {
											newpicklistMultiSelect.add(item);
										}
									}

									for (String item : newpicklistMultiSelect) {
										if (!picklistMultiSelect.contains(item)) {
											picklistMultiSelect.add(item);
										}
									}
									entryDoc.put(name, picklistMultiSelect);

								}
							} else if (dataType.getString("DISPLAY").equals("Relationship")) {
								if (value != null) {
									if (fieldDocument.getString("RELATIONSHIP_TYPE").equalsIgnoreCase("Many to Many")) {
										List<String> stringList = new ArrayList<String>();
										try {
											List<Object> listValues = (List<Object>) value;
											for (Object ob : listValues) {
												stringList.add(Objects.toString(ob));
											}
										} catch (ClassCastException e) {
											if (value.toString().contains(",")) {
												List<Object> listValues = new ObjectMapper().readValue(value.toString(),
														List.class);
												for (Object ob : listValues) {
													stringList.add(Objects.toString(ob));
												}
											} else {
												stringList.add(Objects.toString(value));
											}
										}
										if (stringList.size() > 0) {
											entryDoc.put(name, stringList);
										}
									} else {
										entryDoc.put(name, value);
									}
								}

							} else {
								if (value != null) {
									entryDoc.put(name, value);
								}
							}
						}

						entryDoc.remove("_id");
						entryDoc.put("DATA_ID", entryId);
						if (discussionFieldName != null && messageId != null) {
							if (entryDoc.containsKey(discussionFieldName)
									&& entryDoc.get(discussionFieldName) != null) {
								List<Document> messages = (List<Document>) entryDoc.get(discussionFieldName);
								for (Document discussionMessage : messages) {

									if (discussionMessage.getString("MESSAGE_ID").equals(messageId)) {
										List<Attachment> attachments = new ArrayList<Attachment>();
										if (attachmentValue != null && attachmentValue.length() > 0) {
											attachments = new ObjectMapper().readValue(attachmentValue,
													new TypeReference<List<Attachment>>() {
													});
										}
										discussionMessage.put("ATTACHMENTS", attachments);
										break;
									}
								}
							}
						}
						boolean isTrigger = false;
						if (inputMessage.containsKey("IS_TRIGGER")) {
							isTrigger = (boolean) inputMessage.get("IS_TRIGGER");
						}
						List<Document> moduleFields = (List<Document>) module.get("FIELDS");
						for (Document field : moduleFields) {
							String fieldName = field.getString("NAME");
							Document dataType = (Document) field.get("DATA_TYPE");
							String displayDataType = dataType.getString("DISPLAY");
							if (fieldName.equals("CURRENT_TIMESTAMP")) {
								entryDoc.put(fieldName, new Date());
							}

							if (messageId == null && displayDataType.equalsIgnoreCase("Discussion")) {
								entryDoc.remove(fieldName);
							}

							if (displayDataType.equalsIgnoreCase("chronometer") && entryDoc.containsKey(fieldName)
									&& entryDoc.get(fieldName) != null) {
								try {
									entryDoc.getString(fieldName);
								} catch (ClassCastException e) {
									entryDoc.put(fieldName, "0m");
								}
							}

							if ((displayDataType.equalsIgnoreCase("Date/Time")
									|| displayDataType.equalsIgnoreCase("Time")
									|| displayDataType.equalsIgnoreCase("Time")) && entryDoc.containsKey(fieldName)) {

								SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:sssZ");
								Date date = entryDoc.getDate(fieldName);
								entryDoc.put(fieldName, date);
							}
						}
						String url = "http://" + env.getProperty("dataservice.host") + ":8087/modules/" + moduleId
								+ "/data?is_trigger=" + isTrigger + "&company_id=" + companyId + "&user_uuid="
								+ inputMessage.get("USER_UUID").toString();
						String payload = new ObjectMapper().writeValueAsString(entryDoc);
						logger.debug(url);
						logger.debug(payload);
						String response = null;

						JSONObject httpResponse = httpRequestNode.request(url, payload, "PUT", null);
						logger.debug(httpResponse);
						if (httpResponse.has("RESPONSE")) {
							response = httpResponse.getString("RESPONSE");
						}
						if (response != null) {
							JSONObject responseObj = new JSONObject(response);
							String dataId = responseObj.getString("DATA_ID");
							Map<String, Object> entryMap = new HashMap<String, Object>();
							entryMap.put("ENTRY_ID", dataId);
							entryMap.put("MODULE_ID", moduleId);
							entryMap.put("ESCALATED", false);
							List<Map<String, Object>> entries = new ArrayList<Map<String, Object>>();
							if (inputMessage.get("UPDATED_ENTRIES") != null) {
								entries = (List<Map<String, Object>>) inputMessage.get("UPDATED_ENTRIES");
								entries.add(entryMap);
								inputMessage.put("UPDATED_ENTRIES", entries);
							}
							entries.add(entryMap);
							inputMessage.put("UPDATED_ENTRIES", entries);
						} else {
							// CALL FAILED:
							String subject = "Call Failed on UpdateEntry for " + subdomain;
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
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		ArrayList<Document> connections = (ArrayList<Document>) node.get("CONNECTIONS_TO");

		// UPDATE ENTRY NODE HAS ONLY ONE CONNECTION
		if (connections.size() == 1) {
			Document connection = connections.get(0);
			resultMap.put("NODE_ID", connection.getString("TO_NODE"));
		}
		resultMap.put("INPUT_MESSAGE", inputMessage);
		logger.trace("Exit UpdateEntry.executeNode()");
		return resultMap;
	}
}
