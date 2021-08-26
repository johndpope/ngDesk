package com.ngdesk.modules.workflows;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import com.ngdesk.Authentication;
import com.ngdesk.Global;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.ForbiddenException;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.modules.rules.Condition;
import com.ngdesk.roles.RoleService;
import com.ngdesk.workflow.Field;
import com.ngdesk.workflow.Node;

@RestController
@Component
public class ModuleWorkflowService {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Authentication auth;

	@Autowired
	private Global global;

	@Autowired
	private RoleService roleService;
	
	@Value("${manager.host}")
	private String managerHost;

	@Autowired
	private SimpMessagingTemplate template;

	private final Logger log = LoggerFactory.getLogger(ModuleWorkflowService.class);

	@GetMapping("/modules/{module_id}/workflows")
	public ResponseEntity<Object> getWorkflows(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId,
			@RequestParam(value = "page_size", required = false) String pageSize,
			@RequestParam(value = "page", required = false) String page,
			@RequestParam(value = "sort", required = false) String sort,
			@RequestParam(value = "order", required = false) String order) {

		JSONArray workflows = new JSONArray();
		JSONObject resultObj = new JSONObject();
		int totalSize = 0;
		try {
			log.trace("Enter ModuleWorkflowService.getWorkflows()  moduleId: " + moduleId);

			// GET COMPANY ID
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String companyId = user.getString("COMPANY_ID");

			// ACCESS MONGO
			MongoCollection<Document> collection = mongoTemplate.getCollection("modules_" + companyId);
			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();

			// CHECK DOC
			if (module != null) {

				if (!roleService.isAuthorizedForModule(userId, "GET", moduleId, companyId)) {
					throw new ForbiddenException("FORBIDDEN");
				}
				ArrayList<Document> workflowDocuments = (ArrayList) module.get("WORKFLOWS");
				totalSize = workflowDocuments.size();
				String moduleName = module.getString("NAME");
				AggregateIterable<Document> sortedDocuments = null;
				List<String> moduleWorkflowNames = new ArrayList<String>();
				moduleWorkflowNames.add("WORKFLOW_ID");
				moduleWorkflowNames.add("NAME");
				moduleWorkflowNames.add("TYPE");
				moduleWorkflowNames.add("DESCRIPTION");
				moduleWorkflowNames.add("CONDITIONS");
				moduleWorkflowNames.add("WORKFLOW");

				// by default return all documents
				int skip = 0;
				int pgSize = 100;
				int pg = 1;

				if (pageSize != null && page != null) {
					pgSize = Integer.valueOf(pageSize);
					pg = Integer.valueOf(page);
					if (pgSize <= 0) {
						throw new BadRequestException("INVALID_PAGE_SIZE");
					} else if (pg <= 0) {
						throw new BadRequestException("INVALID_PAGE_NUMBER");
					} else {
						skip = (pg - 1) * pgSize;
					}
				}
				if (sort != null && order != null) {
					sort = "WORKFLOWS." + sort;
					if (order.equalsIgnoreCase("asc")) {
						sortedDocuments = collection
								.aggregate(
										Arrays.asList(Aggregates.unwind("$WORKFLOWS"),
												Aggregates.match(Filters.eq("NAME", moduleName)),
												Aggregates.sort(Sorts.orderBy(Sorts.ascending(sort))),
												Aggregates
														.project(
																Filters.and(
																		Projections.computed("WORKFLOWS",
																				Projections
																						.include(moduleWorkflowNames)),
																		Projections.excludeId())),
												Aggregates.skip(skip), Aggregates.limit(pgSize)));
					} else if (order.equalsIgnoreCase("desc")) {
						sortedDocuments = collection
								.aggregate(
										Arrays.asList(Aggregates.unwind("$WORKFLOWS"),
												Aggregates.match(Filters.eq("NAME", moduleName)),
												Aggregates.sort(Sorts.orderBy(Sorts.descending(sort))),
												Aggregates
														.project(
																Filters.and(
																		Projections.computed("WORKFLOWS",
																				Projections
																						.include(moduleWorkflowNames)),
																		Projections.excludeId())),
												Aggregates.skip(skip), Aggregates.limit(pgSize)));
					} else {
						throw new BadRequestException("INVALID_SORT_ORDER");
					}
				} else {
					sortedDocuments = collection
							.aggregate(
									Arrays.asList(Aggregates.unwind("$WORKFLOWS"),
											Aggregates.match(Filters.eq("NAME",
													moduleName)),
											Aggregates.project(Filters.and(
													Projections.computed("WORKFLOWS",
															Projections.include(moduleWorkflowNames)),
													Projections.excludeId())),
											Aggregates.skip(skip), Aggregates.limit(pgSize)));
				}

				for (Document document : sortedDocuments) {
					Document data = (Document) document.get("WORKFLOWS");
					workflows.put(data);
				}
			} else {
				throw new ForbiddenException("MODULE_NOT_EXISTS");
			}
			resultObj.put("WORKFLOWS", workflows);
			resultObj.put("TOTAL_RECORDS", totalSize);
			log.trace("Exit ModuleWorkflowService.getWorkflows()  moduleId: " + moduleId);
			return new ResponseEntity<>(resultObj.toString(), Global.postHeaders, HttpStatus.OK);

		} catch (JSONException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@GetMapping("/modules/{module_id}/workflows/{workflow_id}")
	public ModuleWorkflow getWorkflow(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId, @PathVariable("workflow_id") String workflowId) {

		try {
			log.trace(
					"Enter ModuleWorkflowService.getWorkflow()  moduleId: " + moduleId + ", workflowId: " + workflowId);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			// GET COMPANY ID
			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String companyId = user.getString("COMPANY_ID");

			// ACCESS MONGO
			String collectionName = "modules_" + companyId;
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();

			if (module == null) {
				throw new BadRequestException("MODULE_NOT_EXISTS");
			}
			String moduleName = module.getString("NAME");

			if (!roleService.isAuthorizedForModule(userId, "GET", moduleId, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			if (workflowExistsInModule(workflowId, collectionName, moduleName)) {
				ArrayList<Document> workflowDocuments = (ArrayList) module.get("WORKFLOWS");
				for (Document workflowDocument : workflowDocuments) {
					if (workflowDocument.getString("WORKFLOW_ID").equals(workflowId)) {
						ModuleWorkflow workflow = new ObjectMapper().readValue(workflowDocument.toJson(),
								ModuleWorkflow.class);
						log.trace("Exit ModuleWorkflowService.getWorkflow()  moduleName: " + moduleName
								+ ", workflowId: " + workflowId);

						return workflow;
					}
				}
			} else {
				throw new BadRequestException("WORKFLOW_NOT_EXISTS");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PostMapping("/modules/{id}/workflows/{workflow_name}")
	public ModuleWorkflow postWorkflow(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid, @PathVariable("id") String id,
			@PathVariable("workflow_name") String workflowName, @Valid @RequestBody ModuleWorkflow moduleWorkflow) {
		try {
			log.trace(
					"Enter ModuleWorkflowService.postWorkflow()  moduleId: " + id + ", workflowName: " + workflowName);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String companyId = user.getString("COMPANY_ID");
			String companySubdomain = user.getString("COMPANY_SUBDOMAIN");

			// ACCESS MONGO
			MongoCollection<Document> collection = mongoTemplate.getCollection("modules_" + companyId);
			if (!ObjectId.isValid(id)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}

			Document module = collection.find(Filters.eq("_id", new ObjectId(id))).first();

			if (module == null) {
				throw new ForbiddenException("MODULE_NOT_EXISTS");
			}

			List<Document> workflows = (List<Document>) module.get("WORKFLOWS");
			for (Document workflow : workflows) {
				if (workflow.getString("NAME").equalsIgnoreCase(workflowName)) {
					throw new ForbiddenException("WORKFLOW_NAME_EXISTS");
				}

				if ((workflow.getInteger("ORDER")) == (moduleWorkflow.getOrder())) {
					throw new ForbiddenException("WORKFLOW_ORDER_EXISTS");
				}
			}

			String moduleName = module.getString("NAME");

			if (!roleService.isAuthorizedForModule(userId, "POST", id, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			String discussionFieldName = null;
			HashSet<String> fieldIds = new HashSet<String>();
			List<String> chronometerFieldIds = new ArrayList<String>();
			List<Document> moduleFields = (List<Document>) module.get("FIELDS");
			for (Document field : moduleFields) {
				Document dataType = (Document) field.get("DATA_TYPE");
				String displayDataType = dataType.getString("DISPLAY");
				if (displayDataType.equalsIgnoreCase("Discussion")) {
					discussionFieldName = field.getString("NAME");
				}
				fieldIds.add(field.getString("FIELD_ID"));
				if (displayDataType.equals("Chronometer")) {
					chronometerFieldIds.add(field.getString("FIELD_ID"));
				}

				// CHECK VALIDITY FOR CONDITION PICKLIST VALUE
				for (Condition condition : moduleWorkflow.getConditions()) {
					if (!condition.getOpearator().equalsIgnoreCase("CHANGED")) {
						if (displayDataType.equalsIgnoreCase("Picklist")
								&& condition.getCondition().equals(field.get("FIELD_ID"))) {
							List<String> picklistValues = (List<String>) field.get("PICKLIST_VALUES");
							if (!picklistValues.contains(condition.getConditionValue())) {
								throw new BadRequestException("CONDITION_PICKLIST_VALUE_INVALID");
							}
						}
					}
				}
			}

			List<com.ngdesk.modules.rules.Condition> conditions = moduleWorkflow.getConditions();
			for (com.ngdesk.modules.rules.Condition condition : conditions) {
				if (!chronometerFieldIds.isEmpty()) {
					if (chronometerFieldIds.contains(condition.getCondition())) {
						String value = condition.getConditionValue();
						String conditionValue = global.chronometerValueConversionInSeconds(value) + "";
						condition.setConditionValue(conditionValue);
					}
				}
				if (condition.getCondition()
						.equalsIgnoreCase("{{InputMessage." + discussionFieldName + ".LATEST.SENDER}}")) {
					continue;
				}
				if (!fieldIds.contains(condition.getCondition())) {
					throw new BadRequestException("FIELD_DOES_NOT_EXIST");
				}
			}

			List<Node> nodes = moduleWorkflow.getWorkflow().getNodes();
			for (Node node : nodes) {
				if (node.getType().equals("GetEntries")) {
					String moduleId = node.getValues().getModuleId();
					if (moduleId == null) {
						throw new BadRequestException("INCORRECT_GET_ENTRIES_NODE_VALUES");
					} else if (moduleExists(moduleId, companyId)) {
						continue;
					} else {
						throw new BadRequestException("MODULE_DOES_NOT_EXIST");
					}
				} else if (node.getType().equals("CreateEntry")) {
					String moduleId = node.getValues().getModuleId();
					List<Field> fields = node.getValues().getFields();
					if (moduleId == null) {
						throw new BadRequestException("INCORRECT_CREATE_ENTRY_NODE_VALUE_MODULE_ID");
					} else if (fields == null) {
						throw new BadRequestException("INCORRECT_CREATE_ENTRY_NODE_VALUE_FIELDS");
					} else if (moduleExists(moduleId, companyId)) {

						MongoCollection<Document> modulesCollection = mongoTemplate
								.getCollection("modules_" + companyId);
						if (!ObjectId.isValid(moduleId)) {
							throw new BadRequestException("INVALID_MODULE_ID");
						}
						Document moduleDocument = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId)))
								.first();

						List<Document> moduleFieldsList = (List<Document>) moduleDocument.get("FIELDS");
						List<String> requiredFields = new ArrayList<String>();
						for (Document field : moduleFieldsList) {
							if (field.getBoolean("REQUIRED")) {
								requiredFields.add(field.getString("FIELD_ID"));
							}
						}

						List<String> passedFields = new ArrayList<String>();
						for (Field field : fields) {
							passedFields.add(field.getFieldId());
						}

						for (String fieldId : requiredFields) {
							if (!passedFields.contains(fieldId)) {
								throw new BadRequestException("REQUIRED_FIELDS_MISSING");
							}
						}

						for (Field field : fields) {
							String fieldId = field.getFieldId();
							if (fieldExists(fieldId, moduleId, companyId)) {
								continue;
							} else {
								throw new BadRequestException("FIELD_DOES_NOT_EXIST");
							}
						}
					} else {
						throw new BadRequestException("MODULE_DOES_NOT_EXIST");
					}
				} else if (node.getType().equals("UpdateEntry")) {
					String moduleId = node.getValues().getModuleId();
					String entryId = node.getValues().getEntryId();
					List<Field> fields = node.getValues().getFields();
					if (moduleId == null) {
						throw new BadRequestException("INCORRECT_UPDATE_ENTRY_NODE_VALUE_MODULE_ID");
					} else if (entryId == null) {
						throw new BadRequestException("INCORRECT_UPDATE_ENTRY_NODE_VALUE_ENTRY_ID");
					} else if (fields == null) {
						throw new BadRequestException("INCORRECT_UPDATE_ENTRY_NODE_VALUE_FIELDS");
					} else if (moduleExists(moduleId, companyId)) {
						for (Field field : fields) {
							String fieldId = field.getFieldId();
							if (fieldExists(fieldId, moduleId, companyId)) {
								continue;
							} else {
								throw new BadRequestException("FIELD_DOES_NOT_EXIST");
							}
						}
					} else {
						throw new ForbiddenException("MODULE_DOES_NOT_EXIST");
					}
				} else if (node.getType().equals("CreateEntryAndAssign")) {
					String moduleId = node.getValues().getModuleId();
					List<Field> fields = node.getValues().getFields();
					List<String> teams = node.getValues().getTeams();
					// CHECK ATTRIBUTES
					if (moduleId == null) {
						throw new BadRequestException("INCORRECT_CREATE_ENTRY_AND_ASSIGN_NODE_VALUE_MODULE_ID");
					} else if (fields == null) {
						throw new BadRequestException("INCORRECT_CREATE_ENTRY_AND_ASSIGN_NODE_VALUE_FIELDS");
						// CHECK MODULE
					} else if (teams == null) {
						throw new BadRequestException("INCORRECT_CREATE_ENTRY_AND_ASSIGN_NODE_VALUE_TEAMS");
					} else if (moduleExists(moduleId, companyId)) {

						MongoCollection<Document> modulesCollection = mongoTemplate
								.getCollection("modules_" + companyId);
						if (!ObjectId.isValid(moduleId)) {
							throw new BadRequestException("INVALID_MODULE_ID");
						}
						Document moduleDocument = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId)))
								.first();

						List<Document> moduleFieldsList = (List<Document>) moduleDocument.get("FIELDS");
						List<String> requiredFields = new ArrayList<String>();
						for (Document field : moduleFieldsList) {
							if (field.getBoolean("REQUIRED")) {
								requiredFields.add(field.getString("FIELD_ID"));
							}
						}

						List<String> passedFields = new ArrayList<String>();
						for (Field field : fields) {
							passedFields.add(field.getFieldId());
						}

						for (String fieldId : requiredFields) {
							if (!passedFields.contains(fieldId)) {
								throw new BadRequestException("REQUIRED_FIELDS_MISSING");
							}
						}

						// CHECK VALID FIELDS
						for (Field field : fields) {
							// CHECK FIELD FOR EACH FIELD
							String fieldId = field.getFieldId();
							if (fieldExists(fieldId, moduleId, companyId)) {
								continue;
							} else {
								throw new BadRequestException("FIELD_DOES_NOT_EXIST");
							}
						}

						// CHECK VALID TEAMS
						for (String teamId : teams) {
							if (teamExists(teamId, companyId)) {
								continue;
							} else {
								throw new BadRequestException("TEAM_DOES_NOT_EXIST");
							}
						}

					} else {
						throw new BadRequestException("MODULE_DOES_NOT_EXIST");
					}
				} else if (node.getType().equals("Route")) {
					// CHECK VALID VARIABLE
					if (isValidModuleWorkflowVariable(node.getValues().getVariable(), moduleName, companyId)) {
						continue;
					} else {
						throw new BadRequestException("MODULE_WORKFLOW_VARIABLE_INVALID");
					}
				} else if (node.getType().equals("StartEscalation")) {
					String escalationId = node.getValues().getEscalationId();
					if (!validEscalationId(escalationId, companyId)) {
						throw new BadRequestException("ESCALATION_DOES_NOT_EXIST");
					}
				} else if (node.getType().equals("MakePhoneCall") || node.getType().equals("SendSms")) {
					String to = node.getValues().getTo();
					String body = node.getValues().getBody();

					if (to == null) {
						throw new BadRequestException("SMS_TO_REQUIRED");
					}
					if (body == null) {
						throw new BadRequestException("SMS_BODY_REQUIRED");
					}

					String reg = "\\{\\{(?i)(inputMessage[_a-zA-Z\\.\\-]+)\\}\\}";
					Pattern r = Pattern.compile(reg);
					Matcher matcherTo = r.matcher(to);
					if (!matcherTo.find()) {
						if (!global.isDocumentIdExists(to, "Users_" + companyId)) {
							throw new BadRequestException("USER_NOT_EXISTS");
						}
					}
				}
			}

			moduleWorkflow.setWorkflowId(UUID.randomUUID().toString());
			moduleWorkflow.setDateCreated(new Timestamp(new Date().getTime()));
			// CONVERT
			String workflowJson = new ObjectMapper().writeValueAsString(moduleWorkflow);
			Document newWorkflowDocument = Document.parse(workflowJson);

			// POST WORKFLOW
			collection.updateOne(Filters.eq("NAME", moduleName), Updates.addToSet("WORKFLOWS", newWorkflowDocument));

			checkNode(moduleWorkflow, companySubdomain);
			log.trace("Exit ModuleWorkflowService.postWorkflow()  moduleName: " + moduleName + ", workflowName: "
					+ workflowName);

			this.template.convertAndSend("rest/" + id + "/triggers/fields/button", "Trigger posted successfully");
			return moduleWorkflow;

		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");

	}

	@PutMapping("/modules/{id}/workflows/{workflow_id}")
	public ModuleWorkflow putWorkflow(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid, @PathVariable("id") String id,
			@PathVariable("workflow_id") String workflowId, @Valid @RequestBody ModuleWorkflow moduleWorkflow) {

		try {
			log.trace("Enter ModuleWorkflowService.putWorkflow()  moduleId: " + id + ", workflowId: " + workflowId);
			if (!workflowId.equals(moduleWorkflow.getWorkflowId())) {
				throw new BadRequestException("WORKFLOW_ID_MISMATCH");
			}
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String companyId = user.getString("COMPANY_ID");
			String companySubdomain = user.getString("COMPANY_SUBDOMAIN");

			// ACCESS MONGO
			MongoCollection<Document> collection = mongoTemplate.getCollection("modules_" + companyId);
			if (!ObjectId.isValid(id)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}

			Document module = collection.find(Filters.eq("_id", new ObjectId(id))).first();

			if (module == null) {
				throw new BadRequestException("MODULE_NOT_EXISTS");
			}

			String moduleName = module.getString("NAME");
			if (!workflowExistsInModule(workflowId, "modules_" + companyId, moduleName)) {
				throw new ForbiddenException("WORKFLOW_NOT_EXISTS");
			}

			List<Document> workflows = (List<Document>) module.get("WORKFLOWS");
			for (Document workflow : workflows) {
				if (workflow.getString("NAME").equalsIgnoreCase(moduleWorkflow.getName())
						&& !workflow.getString("WORKFLOW_ID").equals(workflowId)) {
					throw new ForbiddenException("WORKFLOW_NAME_EXISTS");
				}

				if (workflow.getInteger("ORDER") == (moduleWorkflow.getOrder())
						&& !workflow.getString("WORKFLOW_ID").equals(workflowId)) {
					throw new ForbiddenException("WORKFLOW_ORDER_EXISTS");
				}
			}

			if (!roleService.isAuthorizedForModule(userId, "PUT", id, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			HashSet<String> fieldIds = new HashSet<String>();
			List<Document> moduleFields = (List<Document>) module.get("FIELDS");
			String discussionFieldName = null;
			List<String> chronometerFieldIds = new ArrayList<String>();
			for (Document field : moduleFields) {
				Document dataType = (Document) field.get("DATA_TYPE");
				String displayDataType = dataType.getString("DISPLAY");
				if (displayDataType.equalsIgnoreCase("Discussion")) {
					discussionFieldName = field.getString("NAME");
				}
				fieldIds.add(field.getString("FIELD_ID"));
				if (displayDataType.equals("Chronometer")) {
					chronometerFieldIds.add(field.getString("FIELD_ID"));
				}

				// CHECK VALIDITY FOR CONDITION PICKLIST VALUE
				for (Condition condition : moduleWorkflow.getConditions()) {
					if (!condition.getOpearator().equalsIgnoreCase("CHANGED")) {
						if (displayDataType.equalsIgnoreCase("Picklist")
								&& condition.getCondition().equals(field.get("FIELD_ID"))) {
							List<String> picklistValues = (List<String>) field.get("PICKLIST_VALUES");
							if (!picklistValues.contains(condition.getConditionValue())) {
								throw new BadRequestException("CONDITION_PICKLIST_VALUE_INVALID");
							}
						}
					}
				}
			}

			List<com.ngdesk.modules.rules.Condition> conditions = moduleWorkflow.getConditions();
			for (com.ngdesk.modules.rules.Condition condition : conditions) {
				if (!chronometerFieldIds.isEmpty()) {
					if (chronometerFieldIds.contains(condition.getCondition())) {
						String value = condition.getConditionValue();
						String conditionValue = global.chronometerValueConversionInSeconds(value) + "";
						condition.setConditionValue(conditionValue);
					}
				}
				if (condition.getCondition()
						.equalsIgnoreCase("{{InputMessage." + discussionFieldName + ".LATEST.SENDER}}")) {
					continue;
				}
				if (!fieldIds.contains(condition.getCondition())) {
					throw new BadRequestException("FIELD_DOES_NOT_EXIST");
				}
			}

			List<Node> nodes = moduleWorkflow.getWorkflow().getNodes();
			for (Node node : nodes) {
				if (node.getType().equals("GetEntries")) {
					String moduleId = node.getValues().getModuleId();
					if (moduleId == null) {
						throw new BadRequestException("INCORRECT_GET_ENTRIES_NODE_VALUES");
					} else if (moduleExists(moduleId, companyId)) {
						continue;
					} else {
						throw new BadRequestException("MODULE_DOES_NOT_EXIST");
					}
				} else if (node.getType().equals("CreateEntry")) {
					String moduleId = node.getValues().getModuleId();
					List<Field> fields = node.getValues().getFields();
					if (moduleId == null) {
						throw new BadRequestException("INCORRECT_CREATE_ENTRY_NODE_VALUE_MODULE_ID");
					} else if (fields == null) {
						throw new BadRequestException("INCORRECT_CREATE_ENTRY_NODE_VALUE_FIELDS");
					} else if (moduleExists(moduleId, companyId)) {

						MongoCollection<Document> modulesCollection = mongoTemplate
								.getCollection("modules_" + companyId);
						if (!ObjectId.isValid(moduleId)) {
							throw new BadRequestException("INVALID_MODULE_ID");
						}
						Document moduleDocument = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId)))
								.first();

						List<Document> moduleFieldsList = (List<Document>) moduleDocument.get("FIELDS");
						List<String> requiredFields = new ArrayList<String>();
						for (Document field : moduleFieldsList) {
							if (field.getBoolean("REQUIRED")) {
								requiredFields.add(field.getString("FIELD_ID"));
							}
						}

						List<String> passedFields = new ArrayList<String>();
						for (Field field : fields) {
							passedFields.add(field.getFieldId());
						}

						for (String fieldId : requiredFields) {
							if (!passedFields.contains(fieldId)) {
								throw new BadRequestException("REQUIRED_FIELDS_MISSING");
							}
						}

						for (Field field : fields) {
							String fieldId = field.getFieldId();
							if (fieldExists(fieldId, moduleId, companyId)) {
								continue;
							} else {
								throw new BadRequestException("FIELD_DOES_NOT_EXIST");
							}
						}
					} else {
						throw new BadRequestException("MODULE_DOES_NOT_EXIST");
					}
				} else if (node.getType().equals("UpdateEntry")) {
					String moduleId = node.getValues().getModuleId();
					String entryId = node.getValues().getEntryId();
					List<Field> fields = node.getValues().getFields();
					if (moduleId == null) {
						throw new BadRequestException("INCORRECT_UPDATE_ENTRY_NODE_VALUE_MODULE_ID");
					} else if (entryId == null) {
						throw new BadRequestException("INCORRECT_UPDATE_ENTRY_NODE_VALUE_ENTRY_ID");
					} else if (fields == null) {
						throw new BadRequestException("INCORRECT_UPDATE_ENTRY_NODE_VALUE_FIELDS");
					} else if (moduleExists(moduleId, companyId)) {
						for (Field field : fields) {
							String fieldId = field.getFieldId();
							if (fieldExists(fieldId, moduleId, companyId)) {
								continue;
							} else {
								throw new BadRequestException("FIELD_DOES_NOT_EXIST");
							}
						}
					} else {
						throw new BadRequestException("MODULE_DOES_NOT_EXIST");
					}
				} else if (node.getType().equals("CreateEntryAndAssign")) {
					String moduleId = node.getValues().getModuleId();
					List<Field> fields = node.getValues().getFields();
					List<String> teams = node.getValues().getTeams();
					// CHECK ATTRIBUTES
					if (moduleId == null) {
						throw new BadRequestException("INCORRECT_CREATE_ENTRY_AND_ASSIGN_NODE_VALUE_MODULE_ID");
					} else if (fields == null) {
						throw new BadRequestException("INCORRECT_CREATE_ENTRY_AND_ASSIGN_NODE_VALUE_FIELDS");
						// CHECK MODULE
					} else if (teams == null) {
						throw new BadRequestException("INCORRECT_CREATE_ENTRY_AND_ASSIGN_NODE_VALUE_TEAMS");
					} else if (moduleExists(moduleId, companyId)) {

						MongoCollection<Document> modulesCollection = mongoTemplate
								.getCollection("modules_" + companyId);
						if (!ObjectId.isValid(moduleId)) {
							throw new BadRequestException("INVALID_MODULE_ID");
						}
						Document moduleDocument = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId)))
								.first();

						List<Document> moduleFieldsList = (List<Document>) moduleDocument.get("FIELDS");
						List<String> requiredFields = new ArrayList<String>();
						for (Document field : moduleFieldsList) {
							if (field.getBoolean("REQUIRED")) {
								requiredFields.add(field.getString("FIELD_ID"));
							}
						}

						List<String> passedFields = new ArrayList<String>();
						for (Field field : fields) {
							passedFields.add(field.getFieldId());
						}

						for (String fieldId : requiredFields) {
							if (!passedFields.contains(fieldId)) {
								throw new BadRequestException("REQUIRED_FIELDS_MISSING");
							}
						}

						// CHECK VALID FIELDS
						for (Field field : fields) {
							// CHECK FIELD FOR EACH FIELD
							String fieldId = field.getFieldId();
							if (fieldExists(fieldId, moduleId, companyId)) {
								continue;
							} else {
								throw new BadRequestException("FIELD_DOES_NOT_EXIST");
							}
						}

						// CHECK VALID TEAMS
						for (String teamId : teams) {
							if (teamExists(teamId, companyId)) {
								continue;
							} else {
								throw new BadRequestException("TEAM_DOES_NOT_EXIST");
							}
						}

					} else {
						throw new BadRequestException("MODULE_DOES_NOT_EXIST");
					}
				} else if (node.getType().equals("Route")) {
					// CHECK VALID VARIABLE
					if (isValidModuleWorkflowVariable(node.getValues().getVariable(), moduleName, companyId)) {
						continue;
					} else {
						throw new BadRequestException("MODULE_WORKFLOW_VARIABLE_INVALID");
					}
				} else if (node.getType().equals("StartEscalation")) {
					String escalationId = node.getValues().getEscalationId();
					if (!validEscalationId(escalationId, companyId)) {
						throw new BadRequestException("ESCALATION_DOES_NOT_EXIST");
					}
				} else if (node.getType().equals("MakePhoneCall") || node.getType().equals("SendSms")) {
					String to = node.getValues().getTo();
					String body = node.getValues().getBody();

					if (to == null) {
						throw new BadRequestException("SMS_TO_REQUIRED");
					}
					if (body == null) {
						throw new BadRequestException("SMS_BODY_REQUIRED");
					}

					String reg = "\\{\\{(?i)(inputMessage[_a-zA-Z\\.\\-]+)\\}\\}";
					Pattern r = Pattern.compile(reg);
					Matcher matcherTo = r.matcher(to);
					if (!matcherTo.find()) {
						if (!global.isDocumentIdExists(to, "Users_" + companyId)) {
							throw new BadRequestException("USER_NOT_EXISTS");
						}
					}
				}
			}

			moduleWorkflow.setDateUpdated(new Timestamp(new Date().getTime()));
			moduleWorkflow.setLastUpdatedBy(userId);
			moduleWorkflow.setWorkflowId(workflowId);

			// CONVERT
			String workflowJson = new ObjectMapper().writeValueAsString(moduleWorkflow);
			Document newWorkflowDocument = Document.parse(workflowJson);

			// PUT WORKFLOW
			collection.updateOne(Filters.eq("NAME", moduleName),
					Updates.pull("WORKFLOWS", Filters.eq("WORKFLOW_ID", moduleWorkflow.getWorkflowId())));
			collection.updateOne(Filters.eq("NAME", moduleName), Updates.push("WORKFLOWS", newWorkflowDocument));

			checkNode(moduleWorkflow, companySubdomain);

			log.trace("Exit ModuleWorkflowService.putWorkflow()  moduleName: " + moduleName + ", workflowId: "
					+ workflowId);

			return moduleWorkflow;

		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");

	}

	@DeleteMapping("/modules/{module_id}/workflows/{workflow_id}")
	public ResponseEntity<Object> deleteworkflow(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("workflow_id") String workflowId, @PathVariable("module_id") String moduleId) {
		try {
			log.trace("Enter ModuleWorkflowService.deleteworkflow()  moduleId: " + moduleId + ", workflowId: "
					+ workflowId);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String companyId = user.getString("COMPANY_ID");
			String collectionName = "modules_" + companyId;

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
			if (module == null) {
				throw new BadRequestException("MODULE_NOT_EXISTS");
			}
			String moduleName = module.getString("NAME");
			if (!roleService.isAuthorizedForModule(userId, "DELETE", moduleId, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			if (workflowExistsInModule(workflowId, collectionName, moduleName)) {
				collection.updateOne(Filters.eq("NAME", moduleName),
						Updates.pull("WORKFLOWS", Filters.eq("WORKFLOW_ID", workflowId)));
				log.trace("Exit ModuleWorkflowService.deleteworkflow()  moduleName: " + moduleName + ", workflowId: "
						+ workflowId);
				return new ResponseEntity<Object>(HttpStatus.OK);
			} else {
				throw new BadRequestException("WORKFLOW_NOT_EXISTS");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	public boolean workflowExistsInModule(String workflowId, String collectionName, String moduleName) {
		try {
			log.trace("Enter ModuleWorkflowService.workflowExistsInModule()  workflowId: " + workflowId
					+ ", collectionName: " + collectionName + ", moduleName: " + moduleName);
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			// Get Document
			Document module = collection.find(Filters.eq("NAME", moduleName)).first();
			if (module.get("WORKFLOWS") != null) {
				ArrayList<Document> documents = (ArrayList) module.get("WORKFLOWS");
				// GET SPECIFIC FIELD
				for (Document document : documents) {
					if (document.getString("WORKFLOW_ID").equals(workflowId)) {
						log.trace("Exit ModuleWorkflowService.workflowExistsInModule()  workflowId: " + workflowId
								+ ", collectionName: " + collectionName + ", moduleName: " + moduleName);
						return true;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}

		return false;
	}

	public boolean moduleExists(String id, String companyId) {
		try {
			// ACCESS MONGO
			log.trace("Enter ModuleWorkflowService.moduleExists()  id: " + id + ", companyId: " + companyId);
			MongoCollection<Document> collection = mongoTemplate.getCollection("modules_" + companyId);
			if (new ObjectId().isValid(id)) {
				Document moduleDocument = collection.find(Filters.eq("_id", new ObjectId(id))).first();

				if (moduleDocument != null) {
					log.trace("Exit ModuleWorkflowService.moduleExists()  id: " + id + ", companyId: " + companyId);
					return true;
				} else {
					return false;
				}

			} else {
				throw new BadRequestException("INVALID_ENTRY_ID");
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new BadRequestException("INTERNAL_ERROR");
		}

	}

	public boolean fieldExists(String fieldId, String moduleId, String companyId) {
		try {
			log.trace("Enter ModuleWorkflowService.fieldExists()  fieldId: " + fieldId + ", companyId: " + companyId
					+ ", moduleId: " + moduleId);
			// ACCESS MONGO
			MongoCollection<Document> collection = mongoTemplate.getCollection("modules_" + companyId);
			if (new ObjectId().isValid(moduleId)) {
				Document fieldDocument = collection.find(
						Filters.and(Filters.eq("_id", new ObjectId(moduleId)), Filters.eq("FIELDS.FIELD_ID", fieldId)))
						.first();

				if (fieldDocument != null) {

					log.trace("Exit ModuleWorkflowService.fieldExists()  fieldId: " + fieldId + ", companyId: "
							+ companyId + ", moduleId: " + moduleId);
					return true;
				} else {
					return false;
				}
			} else {
				throw new BadRequestException("INVALID_ENTRY_ID");
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new BadRequestException("INTERNAL_ERROR");
		}
	}

	public boolean entryExists(String entryId, String moduleId, String companyId) {
		try {
			log.trace("Enter ModuleWorkflowService.entryExists()  entryId: " + entryId + ", companyId: " + companyId
					+ ", moduleId: " + moduleId);
			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			Document moduleDocument = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();

			String moduleName = moduleDocument.getString("NAME");
			MongoCollection<Document> module = mongoTemplate
					.getCollection(moduleName.replaceAll("\\s+", "_") + "_" + companyId);

			if (new ObjectId().isValid(entryId)) {

				Document entryDocument = module.find(Filters.eq("_id", new ObjectId(entryId))).first();

				if (entryDocument != null) {
					log.trace("Exit ModuleWorkflowService.entryExists()  entryId: " + entryId + ", companyId: "
							+ companyId + ", moduleId: " + moduleId);
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new BadRequestException("INTERNAL_ERROR");
		}
	}

	public boolean teamExists(String teamId, String companyId) {
		try {
			log.trace("Enter ModuleWorkflowService.teamExists()  teamId: " + teamId + ", companyId: " + companyId);
			MongoCollection<Document> teamsCollection = mongoTemplate.getCollection("Teams_" + companyId);
			if (new ObjectId().isValid(teamId)) {
				Document teamDocument = teamsCollection.find(Filters.eq("_id", new ObjectId(teamId))).first();

				if (teamDocument != null) {
					log.trace(
							"Exit ModuleWorkflowService.teamExists()  teamId: " + teamId + ", companyId: " + companyId);
					return true;
				} else {
					return false;
				}
			} else {
				throw new BadRequestException("INVALID_ENTRY_ID");
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new BadRequestException("INTERNAL_ERROR");
		}
	}

	public boolean isValidModuleWorkflowVariable(String value, String moduleName, String companyId) {
		try {
			// ACCESS MONGO
			log.trace("Enter ModuleWorkflowService.isValidModuleWorkflowVariable()  value: " + value + ", moduleName: "
					+ moduleName + ", companyId: " + companyId);
			MongoCollection<Document> collection = mongoTemplate.getCollection("modules_" + companyId);
			Document fieldDocument = collection
					.find(Filters.and(Filters.eq("NAME", moduleName), Filters.eq("FIELDS.FIELD_ID", value))).first();
			if (fieldDocument != null) {
				log.trace("Exit ModuleWorkflowService.isValidModuleWorkflowVariable()  value: " + value
						+ ", moduleName: " + moduleName + ", companyId: " + companyId);
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new BadRequestException("INTERNAL_ERROR");
		}
	}

	public void checkNode(ModuleWorkflow moduleworkflow, String companySubdomain) throws JsonProcessingException {
		log.trace("Enter ModuleWorkflowService.checkNode()  companySubdomain: " + companySubdomain);
		List<Node> nodes = moduleworkflow.getWorkflow().getNodes();
		for (Node node : nodes) {
			if (node.getType().equals("CreateEntryAndAssign")) {
				String nodeId = node.getNodeId();
				String nodeJson = new ObjectMapper().writeValueAsString(node);
				log.trace("E ModuleWorkflowService.checkNode()  companySubdomain: " + companySubdomain);
				global.request("http://" + managerHost + ":9081/ngdesk/" + companySubdomain
						+ "/queues?node_id=" + nodeId, nodeJson, "POST", null);
			}
		}
		log.trace("Exit ModuleWorkflowService.checkNode()  companySubdomain: " + companySubdomain);
	}

	public boolean validEscalationId(String escalationId, String companyId) {
		try {
			String collectionName = "escalations_" + companyId;

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			if (new ObjectId().isValid(escalationId)) {
				Document document = collection.find(Filters.eq("_id", new ObjectId(escalationId))).first();
				if (document == null) {
					return false;
				}
			} else {
				throw new BadRequestException("ESCALATION_DOES_NOT_EXIST");
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}
		return true;
	}

}
