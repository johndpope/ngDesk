package com.ngdesk.nodes;

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.ngdesk.Global;
import com.ngdesk.flowmanager.InputMessage;

@Component
public class ParentNode {

	private final Logger log = LoggerFactory.getLogger(ParentNode.class);

	@Autowired
	private ApplicationContext context;

	@Autowired
	private Global global;

	@Autowired
	private MongoTemplate mongoTemplate;

	public void execute(InputMessage message) {
		log.trace("Enter ParentNode.execute()");

		try {
			Document companyDocument = global.getCompanyFromUUID(message.getCompanyUUID());
			if (companyDocument != null) {

				String companyId = companyDocument.getObjectId("_id").toString();

				String collectionName = "channels_" + message.getType() + "_" + companyId;

				MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
				Document channel = collection.find(Filters.eq("NAME", message.getChannelName())).first();
				if (channel != null) {
					Document workflowDocument = (Document) channel.get("WORKFLOW");
					if (workflowDocument != null) {
						if (workflowDocument.containsKey("NODES")) {
							ArrayList<Document> nodeDocuments = (ArrayList<Document>) workflowDocument.get("NODES");

							if (nodeDocuments != null && nodeDocuments.size() > 0) {
								Document firstNode = nodeDocuments.get(0);

								if ("Start".equals(firstNode.getString("TYPE"))) {
									ObjectMapper mapper = new ObjectMapper();
									Map<String, Object> inputMessage = mapper.convertValue(message, Map.class);
									executeWorkflow(firstNode, nodeDocuments, inputMessage);
								}
							}
						}
					}
				}
			}
			log.trace("Exit ParentNode.execute()");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void executeModuleWorkflow(Map<String, Object> inputMap, String companyId, String moduleId, String type) {
		try {
			log.trace("Enter ParentNode.executeModuleWorkflow() at: " + new Timestamp(new Date().getTime()));
			String collectionName = "modules_" + companyId;

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
			if (module != null) {

				String moduleName = module.getString("NAME");

				ArrayList<Document> workflowDocuments = (ArrayList<Document>) module.get("WORKFLOWS");
				Collections.sort(workflowDocuments, new Comparator<Document>() {
					@Override
					public int compare(Document d1, Document d2) {
						return d1.getInteger("ORDER").compareTo(d2.getInteger("ORDER"));
					}
				});

				if (workflowDocuments != null && workflowDocuments.size() > 0) {
					for (Document workflowDoc : workflowDocuments) {

						String workflowType = workflowDoc.getString("TYPE");

						if (workflowType.equals(type) || (workflowType.equals("CREATE_OR_UPDATE")
								&& (type.equals("CREATE") || type.equals("UPDATE")))) {
							String dataId = inputMap.get("DATA_ID").toString();

							if (isValidConditions(workflowDoc, companyId, moduleName, dataId, moduleId, inputMap)) {
								MongoCollection<Document> entriesCollection = mongoTemplate
										.getCollection(moduleName.replaceAll("\\s+", "_") + "_" + companyId);
								Document entry = entriesCollection.find(Filters.eq("_id", new ObjectId(dataId)))
										.first();
								if (entry != null) {
									entry.remove("_id");
									entry.put("ENTRY_ID", dataId);
									entry.put("MODULE_ID", moduleId);
									String entryString = new ObjectMapper().writeValueAsString(entry);
									Map<String, Object> entryMap = new ObjectMapper().readValue(entryString, Map.class);

									for (String key : entryMap.keySet()) {
										inputMap.put(key, entryMap.get(key));
									}

									inputMap.put("IS_TRIGGER", true);

									List<Map<String, Object>> entriesList = new ArrayList<Map<String, Object>>();
									entriesList.add(entryMap);

									if (type.equalsIgnoreCase("create")) {
										inputMap.put("CREATED_ENTRIES", entriesList);
									} else if (type.equalsIgnoreCase("update")) {
										inputMap.put("UPDATED_ENTRIES", entriesList);
									}

								}

								Document workflow = (Document) workflowDoc.get("WORKFLOW");
								if (workflow != null) {
									if (workflow.containsKey("NODES")) {
										ArrayList<Document> nodeDocuments = (ArrayList<Document>) workflow.get("NODES");

										if (nodeDocuments != null && nodeDocuments.size() > 0) {
											Document firstNode = nodeDocuments.get(0);
											if ("Start".equals(firstNode.getString("TYPE"))) {

												if (inputMap.containsKey("USER_UUID")
														&& inputMap.get("USER_UUID") != null) {
													MongoCollection<Document> usersCollection = mongoTemplate
															.getCollection("Users_" + companyId);

													Document user = usersCollection
															.find(Filters.eq("USER_UUID", inputMap.get("USER_UUID")))
															.first();
													if (user != null) {
														String userId = user.getObjectId("_id").toString();
														user.remove("_id");

														Map<String, Object> userDetails = new ObjectMapper()
																.readValue(user.toJson(), Map.class);

														String role = user.getString("ROLE");
														MongoCollection<Document> rolesCollection = mongoTemplate
																.getCollection("roles_" + companyId);
														Document roleDocument = rolesCollection
																.find(Filters.eq("_id", new ObjectId(role))).first();

														if (roleDocument != null) {
															userDetails.put("ROLE_NAME",
																	roleDocument.getString("NAME"));
														} else {
															userDetails.put("ROLE_NAME", "Customers");
														}

														inputMap.put("SENDER", userDetails);
														inputMap.put("USER_ID", user.get("CONTACT").toString());
													}
												}
												executeWorkflow(firstNode, nodeDocuments, inputMap);
											}
										}
									}
								}
							}
						}
					}
				}
			}
			log.trace("Exit ParentNode.executeModuleWorkflow() at: " + new Timestamp(new Date().getTime()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean isValidConditions(Document workflowDoc, String companyId, String moduleName, String dataId,
			String moduleId, Map<String, Object> inputMap) {

		try {
			List<Document> conditions = (List<Document>) workflowDoc.get("CONDITIONS");
			if (conditions.size() == 0) {
				return true;
			}

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			Document moduleDocument = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();

			List<Document> fields = (List<Document>) moduleDocument.get("FIELDS");
			Map<String, String> fieldNames = new HashMap<String, String>();
			String discussionFieldId = null;
			String discussionFieldName = null;
			List<String> dateFieldIds = new ArrayList<String>();

			for (Document field : fields) {
				String fieldId = field.getString("FIELD_ID");
				String fieldName = field.getString("NAME");
				fieldNames.put(fieldId, fieldName);

				Document datatype = (Document) field.get("DATA_TYPE");
				if (datatype.getString("DISPLAY").equalsIgnoreCase("Discussion")) {
					discussionFieldId = fieldId;
					discussionFieldName = field.getString("NAME");
				} else if (datatype.getString("DISPLAY").equalsIgnoreCase("Date/Time")
						|| datatype.getString("DISPLAY").equalsIgnoreCase("Date")
						|| datatype.getString("DISPLAY").equalsIgnoreCase("Time")) {
					dateFieldIds.add(fieldId);
				}
			}

			String collectionName = moduleName.replaceAll("\\s+", "_") + "_" + companyId;
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			Document entryDoc = collection.find(Filters.eq("_id", new ObjectId(dataId))).first();
			entryDoc.remove("_id");

			Map<String, Object> entry = new ObjectMapper().readValue(entryDoc.toJson(), Map.class);

			List<Boolean> all = new ArrayList<Boolean>();
			List<Boolean> any = new ArrayList<Boolean>();

			if (entry != null) {

				Set<String> entryKeys = entry.keySet();

				for (Document condition : conditions) {
					String requirementType = condition.getString("REQUIREMENT_TYPE");
					String fieldId = condition.getString("CONDITION");
					String operator = condition.getString("OPERATOR");
					String value = condition.getString("CONDITION_VALUE");
					String fieldName = fieldNames.get(fieldId);

					if (!fieldId.contains("InputMessage") && !entryKeys.contains(fieldName)) {
						return false;
					}
					try {

						if (operator.equalsIgnoreCase("CHANGED")) {
							Map<String, Object> oldValue = (Map<String, Object>) inputMap.get("OLD_COPY");
							if (entry.containsKey(fieldName) && entry.get(fieldName) != null
									&& oldValue.containsKey(fieldName)) {
								if (requirementType.equals("All")) {
									if (!entry.get(fieldName).toString().equals(oldValue.get(fieldName).toString())) {
										all.add(true);
									} else {
										all.add(false);
									}
								} else if (requirementType.equals("Any")) {
									if (!entry.get(fieldName).toString().equals(oldValue.get(fieldName).toString())) {
										any.add(true);
									} else {
										any.add(false);
									}
								}
							}

						} else if (operator.equalsIgnoreCase("EQUALS_TO") || operator.equalsIgnoreCase("IS")) {

							if (fieldId.equalsIgnoreCase("{{InputMessage." + discussionFieldName + ".LATEST.SENDER}}")
									&& discussionFieldId != null) {

								if (entry.containsKey(discussionFieldName) && entry.get(discussionFieldName) != null) {

									List<Document> messages = (List<Document>) entryDoc.get(discussionFieldName);
									boolean isValid = false;
									for (int i = messages.size() - 1; i >= 0; i--) {
										if (messages.get(i).getString("MESSAGE_TYPE").equalsIgnoreCase("META_DATA")) {
											continue;
										}
										Document sender = (Document) messages.get(i).get("SENDER");
										String senderUUID = sender.getString("USER_UUID");

										Document senderUserDocumnet = global.getUserFromUUID(senderUUID, companyId);
										String userId = senderUserDocumnet.getObjectId("_id").toString();
										if (value.equalsIgnoreCase("{{REQUESTOR}}")) {
											if (entryDoc.getString("REQUESTOR").equals(userId)) {
												isValid = true;
												break;
											}
										} else if (userId.equals(value)) {
											isValid = true;
											break;
										}
										break;
									}
									if (requirementType.equals("All")) {
										all.add(isValid);
									} else if (requirementType.equals("Any")) {
										any.add(isValid);
									}
								}

							} else if (discussionFieldId != null && fieldId.equals(discussionFieldId)) {
								if (entry.containsKey(fieldName) && entry.get(fieldName) != null) {
									List<Document> messages = (List<Document>) entryDoc.get(fieldName);

									if (requirementType.equals("All")) {
										boolean isValid = true;
										for (Document message : messages) {
											if (!message.getString("MESSAGE").equals(value)) {
												isValid = false;
												break;
											}
										}
										all.add(isValid);
									} else if (requirementType.equals("Any")) {
										boolean isValid = false;
										for (Document message : messages) {
											if (message.getString("MESSAGE").equals(value)) {
												isValid = true;
												break;
											}
										}
										any.add(isValid);
									}
								}
							} else {
								if (!value.equalsIgnoreCase(entry.get(fieldName).toString())) {
									if (requirementType.equals("All")) {
										all.add(false);
									} else if (requirementType.equals("Any")) {
										any.add(false);
									}
								} else {
									if (requirementType.equals("All")) {
										all.add(true);
									} else if (requirementType.equals("Any")) {
										any.add(true);
									}
								}
							}
						} else if (operator.equalsIgnoreCase("NOT_EQUALS_TO")) {
							if (fieldId.equalsIgnoreCase("{{InputMessage." + discussionFieldName + ".LATEST.SENDER}}")
									&& discussionFieldId != null) {
								if (entry.containsKey(discussionFieldName) && entry.get(discussionFieldName) != null) {
									List<Document> messages = (List<Document>) entryDoc.get(discussionFieldName);
									boolean isValid = false;
									for (int i = messages.size() - 1; i >= 0; i--) {
										if (messages.get(i).getString("MESSAGE_TYPE").equalsIgnoreCase("META_DATA")) {
											continue;
										}
										Document sender = (Document) messages.get(i).get("SENDER");
										String senderUUID = sender.getString("USER_UUID");

										Document senderUserDocumnet = global.getUserFromUUID(senderUUID, companyId);
										String userId = senderUserDocumnet.getObjectId("_id").toString();
										if (value.equalsIgnoreCase("{{REQUESTOR}}")) {
											if (!entryDoc.getString("REQUESTOR").equals(userId)) {
												isValid = true;
												break;
											}
										} else if (!userId.equals(value)) {
											isValid = true;
											break;
										}
										break;
									}
									if (requirementType.equals("All")) {
										all.add(isValid);
									} else if (requirementType.equals("Any")) {
										any.add(isValid);
									}
								}

							} else if (discussionFieldId != null && fieldId.equals(discussionFieldId)) {
								if (entry.containsKey(fieldName) && entry.get(fieldName) != null) {
									List<Document> messages = (List<Document>) entryDoc.get(fieldName);

									if (requirementType.equals("All")) {
										boolean isValid = true;
										for (Document message : messages) {
											if (message.getString("MESSAGE").equals(value)) {
												isValid = false;
												break;
											}
										}
										all.add(isValid);
									} else if (requirementType.equals("Any")) {
										boolean isValid = false;
										for (Document message : messages) {
											if (!message.getString("MESSAGE").equals(value)) {
												isValid = true;
												break;
											}
										}
										any.add(isValid);
									}
								}
							} else {
								if (value.equals(entry.get(fieldName).toString())) {
									if (requirementType.equals("All")) {
										all.add(false);
									} else if (requirementType.equals("Any")) {
										any.add(false);
									}
								} else {
									if (requirementType.equals("All")) {
										all.add(true);
									} else if (requirementType.equals("Any")) {
										any.add(true);
									}
								}
							}

						} else if (operator.equalsIgnoreCase("contains")) {
							if (discussionFieldId != null && fieldId.equals(discussionFieldId)) {
								if (entry.containsKey(fieldName) && entry.get(fieldName) != null) {
									List<Document> messages = (List<Document>) entryDoc.get(fieldName);

									if (requirementType.equals("All")) {
										boolean isValid = true;
										for (Document message : messages) {

											String htmlMessage = message.getString("MESSAGE");
											String textMessage = Jsoup.parse(htmlMessage).text();

											if (!textMessage.contains(value)) {
												isValid = false;
												break;
											}
										}
										all.add(isValid);
									} else if (requirementType.equals("Any")) {
										boolean isValid = false;
										for (Document message : messages) {
											String htmlMessage = message.getString("MESSAGE");
											String textMessage = Jsoup.parse(htmlMessage).text();
											if (textMessage.contains(value)) {
												isValid = true;
												break;
											}
										}
										any.add(isValid);
									}
								}
							} else {
								if (!entry.get(fieldName).toString().toLowerCase().contains(value.toLowerCase())) {
									if (requirementType.equals("All")) {
										all.add(false);
									} else if (requirementType.equals("Any")) {
										any.add(false);
									}
								} else {
									if (requirementType.equals("All")) {
										all.add(true);
									} else if (requirementType.equals("Any")) {
										any.add(true);
									}
								}
							}
						} else if (operator.equalsIgnoreCase("DOES_NOT_CONTAIN")) {

							if (discussionFieldId != null && fieldId.equals(discussionFieldId)) {
								if (entry.containsKey(fieldName) && entry.get(fieldName) != null) {
									List<Document> messages = (List<Document>) entryDoc.get(fieldName);

									if (requirementType.equals("All")) {
										boolean isValid = true;
										for (Document message : messages) {
											if (message.getString("MESSAGE").contains(value)) {
												isValid = false;
												break;
											}
										}
										all.add(isValid);
									} else if (requirementType.equals("Any")) {
										boolean isValid = false;
										for (Document message : messages) {
											if (!message.getString("MESSAGE").contains(value)) {
												isValid = true;
												break;
											}
										}
										any.add(isValid);
									}
								}
							} else {
								if (entry.get(fieldName).toString().contains(value)) {
									if (requirementType.equals("All")) {
										all.add(false);
									} else if (requirementType.equals("Any")) {
										any.add(false);
									}
								} else {
									if (requirementType.equals("All")) {
										all.add(true);
									} else if (requirementType.equals("Any")) {
										any.add(true);
									}
								}
							}
						} else if (operator.equalsIgnoreCase("REGEX")) {
							Pattern pattern = Pattern.compile(value);
							Matcher matcher = pattern.matcher(entry.get(fieldName).toString());
							if (!matcher.find()) {
								if (requirementType.equals("All")) {
									all.add(false);
								} else if (requirementType.equals("Any")) {
									any.add(false);
								}
							} else {
								if (requirementType.equals("All")) {
									all.add(true);
								} else if (requirementType.equals("Any")) {
									any.add(true);
								}
							}

						} else if (operator.equalsIgnoreCase("LESS_THAN")) {
							if (!dateFieldIds.contains(fieldId)) {
								if (Integer.parseInt(entry.get(fieldName).toString()) >= Integer.parseInt(value)) {
									if (requirementType.equals("All")) {
										all.add(false);
									} else if (requirementType.equals("Any")) {
										any.add(false);
									}
								} else {
									if (requirementType.equals("All")) {
										all.add(true);
									} else if (requirementType.equals("Any")) {
										any.add(true);
									}
								}
							} else if (dateFieldIds.contains(fieldId)) {
								if (global.isValidDate(value)) {
									Instant instant = null;
									Instant fieldInstant = null;
									instant = Instant.parse(value);
									fieldInstant = Instant.parse(entry.get(fieldName).toString());
									Date dateValue = (Date) Date.from(instant);
									Date fieldValue = (Date) Date.from(fieldInstant);

									if (dateValue.before(fieldValue)) {
										if (requirementType.equals("All")) {
											all.add(false);
										} else if (requirementType.equals("Any")) {
											any.add(false);
										}
									} else {
										if (requirementType.equals("All")) {
											all.add(true);
										} else if (requirementType.equals("Any")) {
											any.add(true);
										}
									}
								} else {
									if (requirementType.equals("All")) {
										all.add(false);
									} else if (requirementType.equals("Any")) {
										any.add(false);
									}
								}
							}
						} else if (operator.equalsIgnoreCase("LENGTH_IS_LESS_THAN")) {
							if ((entry.get(fieldName).toString()).length() >= Integer.parseInt(value)) {
								if (requirementType.equals("All")) {
									all.add(false);
								} else if (requirementType.equals("Any")) {
									any.add(false);
								}
							} else {
								if (requirementType.equals("All")) {
									all.add(true);
								} else if (requirementType.equals("Any")) {
									any.add(true);
								}
							}
						} else if (operator.equalsIgnoreCase("LENGTH_IS_GREATER_THAN")) {
							if ((entry.get(fieldName).toString()).length() < Integer.parseInt(value)) {
								if (requirementType.equals("All")) {
									all.add(false);
								} else if (requirementType.equals("Any")) {
									any.add(false);
								}
							} else {
								if (requirementType.equals("All")) {
									all.add(true);
								} else if (requirementType.equals("Any")) {
									any.add(true);
								}
							}
						} else if (operator.equalsIgnoreCase("GREATER_THAN")) {
							if (!dateFieldIds.contains(fieldId)) {
								if (Integer.parseInt(entry.get(fieldName).toString()) < Integer.parseInt(value)) {
									if (requirementType.equals("All")) {
										all.add(false);
									} else if (requirementType.equals("Any")) {
										any.add(false);
									}
								} else {
									if (requirementType.equals("All")) {
										all.add(true);
									} else if (requirementType.equals("Any")) {
										any.add(true);
									}
								}
							} else if (dateFieldIds.contains(fieldId)) {
								Instant instant = null;
								Instant fieldInstant = null;
								if (global.isValidDate(value)) {
									instant = Instant.parse(value);
									fieldInstant = Instant.parse(entry.get(fieldName).toString());

									Date dateValue = (Date) Date.from(instant);
									Date fieldValue = (Date) Date.from(fieldInstant);

									if (dateValue.after(fieldValue)) {
										if (requirementType.equals("All")) {
											all.add(false);
										} else if (requirementType.equals("Any")) {
											any.add(false);
										}
									} else {
										if (requirementType.equals("All")) {
											all.add(true);
										} else if (requirementType.equals("Any")) {
											any.add(true);
										}
									}
								} else {
									if (requirementType.equals("All")) {
										all.add(false);
									} else if (requirementType.equals("Any")) {
										any.add(false);
									}
								}
							}
						} else if (operator.equalsIgnoreCase("IS_UNIQUE")) {
							if (fieldName != null) {
								List<Document> entries = collection.find(Filters.eq(fieldName, value))
										.into(new ArrayList<Document>());
								if (entries.size() > 1) {
									if (requirementType.equals("All")) {
										all.add(false);
									} else if (requirementType.equals("Any")) {
										any.add(false);
									}
								} else if (entries.size() == 1) {
									if (requirementType.equals("All")) {
										all.add(true);
									} else if (requirementType.equals("Any")) {
										any.add(true);
									}
								}
							}
						}

					} catch (Exception e) {
						e.printStackTrace();
						if (requirementType.equals("All")) {
							all.add(false);
						} else if (requirementType.equals("Any")) {
							any.add(false);
						}
					}

				}

				boolean allValue = true;
				for (boolean booleanValue : all) {
					if (!booleanValue) {
						allValue = false;
						break;
					}
				}
				boolean anyValue = true;
				for (boolean booleanValue : any) {
					if (!booleanValue) {
						anyValue = false;
					} else {
						anyValue = true;
						break;
					}
				}
				return (allValue && anyValue);
			} else {
				return false;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public void executeWorkflow(Document node, ArrayList<Document> nodeDocuments, Map<String, Object> inputMessage) {
		try {
			log.trace("Enter ParentNode.executeWorkflow(): " + new ObjectMapper().writeValueAsString(inputMessage));

			// Document parameter
			Class[] paramDocument = new Class[2];
			paramDocument[0] = Document.class;
			paramDocument[1] = Map.class;

			Class cls = Class.forName("com.ngdesk.nodes." + node.getString("TYPE"));
			Object obj = context.getBean(cls);

			String nodeName = node.getString("NAME");
			inputMessage.put("NODE_NAME", nodeName);

			Method method = cls.getDeclaredMethod("executeNode", paramDocument);
			Map<String, Object> resultMap = (Map<String, Object>) method.invoke(obj, node, inputMessage);
			inputMessage = (Map<String, Object>) resultMap.get("INPUT_MESSAGE");

			if (resultMap.containsKey("NODE_ID")) {
				String nodeId = resultMap.get("NODE_ID").toString();
				Document nextNode = getNode(nodeId, nodeDocuments);
				if (nextNode != null) {
					executeWorkflow(nextNode, nodeDocuments, inputMessage);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Document getNode(String nodeId, ArrayList<Document> nodes) {
		try {
			for (Document node : nodes) {
				if (node.getString("ID").equals(nodeId)) {
					return node;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

}
