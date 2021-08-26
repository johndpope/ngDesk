package com.ngdesk.modules.slas;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.ngdesk.roles.RoleService;
import com.ngdesk.workflow.Field;
import com.ngdesk.workflow.Node;
import com.ngdesk.wrapper.Wrapper;

@RestController
@Component
public class SlaService {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Authentication auth;

	@Autowired
	private Global global;

	@Autowired
	private RoleService roleService;

	@Autowired
	Wrapper wrapper;

	@Autowired
	RabbitTemplate rabbitTemplate;

	private final Logger log = LoggerFactory.getLogger(SlaService.class);

	@GetMapping("/modules/{module_id}/sla")
	public ResponseEntity<Object> getSlas(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId,
			@RequestParam(value = "page_size", required = false) String pageSize,
			@RequestParam(value = "page", required = false) String page,
			@RequestParam(value = "sort", required = false) String sort,
			@RequestParam(value = "order", required = false) String order) {

		JSONArray slas = new JSONArray();
		JSONObject resultObj = new JSONObject();
		int totalSize = 0;
		try {
			log.trace("Enter SlaService.getSlas()  moduleId: " + moduleId);

			// GET COMPANY ID
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String companyId = user.getString("COMPANY_ID");
			String roleId = user.getString("ROLE");

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
				if (!roleService.isSystemAdmin(roleId, companyId)) {
					throw new ForbiddenException("FORBIDDEN");
				}
				if (!module.containsKey("SLAS") || module.get("SLAS") == null) {
					resultObj.put("SLAS", new JSONArray());
					resultObj.put("TOTAL_RECORDS", 0);
					return new ResponseEntity<>(resultObj.toString(), Global.postHeaders, HttpStatus.OK);
				}
				ArrayList<Document> slaDocuments = (ArrayList) module.get("SLAS");
				String moduleName = module.getString("NAME");
				totalSize = slaDocuments.size();
				AggregateIterable<Document> sortedDocuments = null;
				List<String> slasNames = new ArrayList<String>();
				slasNames.add("SLA_ID");
				slasNames.add("NAME");
				slasNames.add("CONDITIONS");
				slasNames.add("DESCRIPTION");
				slasNames.add("VIOLATIONS");
				slasNames.add("WORKFLOW");
				slasNames.add("DELETED");

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
					sort = "SLAS." + sort;
					if (order.equalsIgnoreCase("asc")) {
						sortedDocuments = collection.aggregate(Arrays
								.asList(Aggregates.unwind("$SLAS"), Aggregates.match(Filters.eq("NAME", moduleName)),
										Aggregates.sort(Sorts.orderBy(Sorts.ascending(sort))),
										Aggregates.project(Filters.and(
												Projections.computed("SLAS", Projections.include(slasNames)),
												Projections.excludeId())),
										Aggregates.skip(skip), Aggregates.limit(pgSize)));
					} else if (order.equalsIgnoreCase("desc")) {
						sortedDocuments = collection.aggregate(Arrays
								.asList(Aggregates.unwind("$SLAS"), Aggregates.match(Filters.eq("NAME", moduleName)),
										Aggregates.sort(Sorts.orderBy(Sorts.descending(sort))),
										Aggregates.project(Filters.and(
												Projections.computed("SLAS", Projections.include(slasNames)),
												Projections.excludeId())),
										Aggregates.skip(skip), Aggregates.limit(pgSize)));
					} else {
						throw new BadRequestException("INVALID_SORT_ORDER");
					}
				} else {
					sortedDocuments = collection.aggregate(Arrays.asList(Aggregates.unwind("$SLAS"),
							Aggregates.match(Filters.eq("NAME", moduleName)),
							Aggregates.project(Filters.and(Projections.computed("SLAS", Projections.include(slasNames)),
									Projections.excludeId())),
							Aggregates.skip(skip), Aggregates.limit(pgSize)));
				}

				for (Document document : sortedDocuments) {
					Document data = (Document) document.get("SLAS");
					slas.put(data);
				}
			} else {
				throw new ForbiddenException("MODULE_NOT_EXISTS");
			}
			resultObj.put("SLAS", slas);
			resultObj.put("TOTAL_RECORDS", totalSize);
			log.trace("Exit SLAService.getSLA()  moduleId: " + moduleId);
			return new ResponseEntity<>(resultObj.toString(), Global.postHeaders, HttpStatus.OK);

		} catch (JSONException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@GetMapping("/modules/{module_id}/sla/{sla_id}")
	public Sla getSla(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId, @PathVariable("sla_id") String slaId) {

		try {
			log.trace("Enter SlaService.getSla()  moduleId: " + moduleId + ", slasId: " + slaId);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			// GET COMPANY ID
			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String companyId = user.getString("COMPANY_ID");
			String roleId = user.getString("ROLE");

			// ACCESS MONGO
			String collectionName = "modules_" + companyId;
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();

			if (module == null) {
				throw new ForbiddenException("MODULE_NOT_EXISTS");
			}
			String moduleName = module.getString("NAME");
			if (!roleService.isAuthorizedForModule(userId, "GET", moduleId, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}
			if (!roleService.isSystemAdmin(roleId, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			if (slaExistsInModule(slaId, collectionName, moduleName)) {
				ArrayList<Document> slasDocuments = (ArrayList) module.get("SLAS");
				for (Document slasDocument : slasDocuments) {
					if (slasDocument.getString("SLA_ID").equals(slaId)) {
						Sla sla = new ObjectMapper().readValue(new ObjectMapper().writeValueAsString(slasDocument),
								Sla.class);
						log.trace("Exit SlaService.getSla()  moduleName: " + moduleName + ", slaId: " + slaId);

						return sla;
					}
				}
			} else {
				throw new ForbiddenException("SLA_NOT_EXISTS");
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

	@PostMapping("/modules/{module_id}/sla")
	public Sla postSla(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String slaModuleId, @Valid @RequestBody Sla sla) {

		try {
			log.trace("Enter SlaService.postSla()  moduleId: " + slaModuleId);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			// GET COMPANY ID
			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String companyId = user.getString("COMPANY_ID");
			String roleId = user.getString("ROLE");

			if (Character.isDigit(sla.getName().charAt(0))) {
				throw new BadRequestException("INVALID_CHAR_SLA_NAME");
			}

			// ACCESS MONGO
			MongoCollection<Document> collection = mongoTemplate.getCollection("modules_" + companyId);
			if (!ObjectId.isValid(slaModuleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			Document module = collection.find(Filters.eq("_id", new ObjectId(slaModuleId))).first();

			if (module == null) {
				throw new BadRequestException("MODULE_NOT_EXISTS");
			}
			if (!roleService.isAuthorizedForModule(userId, "POST", slaModuleId, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}
			if (!roleService.isSystemAdmin(roleId, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			List<Document> moduleFields = (List<Document>) module.get("FIELDS");
			List<String> fieldIds = new ArrayList<String>();
			List<String> notSupportedViolationFields = new ArrayList<String>();
			List<String> discussionFields = new ArrayList<String>();
			List<String> notSupportedConditionFields = new ArrayList<String>();
			Hashtable<String, String> fieldToDataTypeMap = new Hashtable<String, String>();

			List<SlaConditions> conditions = sla.getConditions();
			List<Violation> violations = (List<Violation>) sla.getViolations();

			List<String> chronometerFieldIds = new ArrayList<String>();
			for (Document moduleField : moduleFields) {
				String fieldId = moduleField.getString("FIELD_ID");
				fieldIds.add(fieldId);

				Document datatype = (Document) moduleField.get("DATA_TYPE");
				fieldToDataTypeMap.put(fieldId, datatype.getString("DISPLAY"));

				String dataType = datatype.getString("DISPLAY");

				if (dataType.equals("Chronometer")) {
					chronometerFieldIds.add(moduleField.getString("FIELD_ID"));
				}

				// GETTING INVLIAD FIELDS FOR CONDITIONS AND VIOLATIONS
				if (dataType.equalsIgnoreCase("Auto Number") || dataType.equalsIgnoreCase("Picklist (Multi-Select)")) {
					notSupportedViolationFields.add(fieldId);
				} else if (dataType.equalsIgnoreCase("Relationship")) {
					String relationshipType = moduleField.getString("RELATIONSHIP_TYPE");

					if (relationshipType.equalsIgnoreCase("one to many")
							|| relationshipType.equalsIgnoreCase("many to many")) {
						notSupportedViolationFields.add(fieldId);
						notSupportedConditionFields.add(fieldId);
					}
				} else if (dataType.equalsIgnoreCase("Discussion")) {
					discussionFields.add(fieldId);
					notSupportedConditionFields.add(fieldId);
				}

				for (Violation violation : violations) {
					if (dataType.equalsIgnoreCase("Picklist") && violation.getCondition().equals(fieldId)
							&& violation.getOpearator().equalsIgnoreCase("HAS_BEEN")) {
						List<String> picklistValues = (List<String>) moduleField.get("PICKLIST_VALUES");
						if (!picklistValues.contains(violation.getConditionValue())) {
							throw new BadRequestException("PICKLIST_VALUE_INVALID");
						}
					}
				}

				// CHECK VALIDITY FOR CONDITION PICKLIST VALUE
				for (SlaConditions condition : conditions) {
					if (!(condition.getOpearator().equalsIgnoreCase("EXISTS")
							|| condition.getOpearator().equalsIgnoreCase("DOES_NOT_EXIST"))) {
						if (dataType.equalsIgnoreCase("Picklist") && condition.getCondition().equals(fieldId)) {
							List<String> picklistValues = (List<String>) moduleField.get("PICKLIST_VALUES");
							if (!picklistValues.contains(condition.getConditionValue())) {
								throw new BadRequestException("CONDITION_PICKLIST_VALUE_INVALID");
							}
						}
					}
				}

			}

			if (module.containsKey("SLAS")) {
				List<Document> existingSlas = (List<Document>) module.get("SLAS");
				if (existingSlas != null) {
					for (Document existingSla : existingSlas) {
						if (existingSla.getString("NAME").equalsIgnoreCase(sla.getName().trim())) {
							throw new BadRequestException("SLA_NAME_ALREADY_EXISTS");
						}
					}
				}
			}
			int max_recurrence;
			boolean isRecurring = sla.isRecurring();
			max_recurrence = sla.getMaxRecurrence();
			if (!isRecurring) {
				sla.setIntervalTime(1);
			}
			if (!isRecurring) {
				sla.setMaxRecurrence(10);
			} else {
				if (max_recurrence < 0) {
					throw new BadRequestException("SLA_RECURRENCE_NEGATIVE");
				} else if (max_recurrence > 99) {
					throw new BadRequestException("SLA_RECURRENCE_REACHED_LIMIT");
				} else {
					sla.setMaxRecurrence(max_recurrence);
				}
			}

			// CHECKING : CONDITION FIELD EXISTS AND OPERATOR
			for (SlaConditions condition : conditions) {
				isValidFieldAndCondition(condition.getCondition(), fieldIds, notSupportedConditionFields, null,
						condition.getOpearator(), null);
				if (!checkValidOperator(fieldToDataTypeMap, condition.getOpearator(), condition.getCondition())) {
					throw new BadRequestException("INVALID_OPERATOR");
				}
				if (chronometerFieldIds.contains(condition.getCondition())) {
					String value = condition.getConditionValue();
					String conditionValue = global.chronometerValueConversionInSeconds(value) + "";
					condition.setConditionValue(conditionValue);
				}
			}

			// CHECKING : VIOLATION FIELD EXISTS AND OPERATOR
			for (Violation violation : violations) {
				if (violation.getSlaExpiry() < 0) {
					throw new BadRequestException("INVALID_SLA_EXPIRY");
				}
				isValidFieldAndCondition(violation.getCondition(), fieldIds, notSupportedViolationFields,
						discussionFields, violation.getOpearator(), violation.getConditionValue());
				if (discussionFields.contains(violation.getCondition())) {
					if (teamExists(violation.getConditionValue(), companyId)) {
						continue;
					} else if (violation.getConditionValue().equalsIgnoreCase("REQUESTOR")) {
						continue;
					} else {
						throw new BadRequestException("TEAM_DOES_NOT_EXIST");
					}
				}
			}

			String moduleName = module.getString("NAME");

			// WORKFLOW NODE CONDITIONS
			List<Node> nodes = sla.getWorkflow().getNodes();
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
				} else if (node.getType().equals("DeleteEntry")) {
					String moduleId = node.getValues().getModuleId();
					String entryId = node.getValues().getEntryId();
					if (moduleId == null) {
						throw new BadRequestException("INCORRECT_DELETE_ENTRY_NODE_VALUE_ENTRY_ID");
					} else if (entryId == null) {
						throw new BadRequestException("INCORRECT_DELETE_ENTRY_NODE_VALUE_MODULE_ID");
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
					System.out.println(fields);
					if (moduleId == null) {
						throw new BadRequestException("INCORRECT_UPDATE_ENTRY_NODE_VALUE_MODULE_ID");
					} else if (entryId == null) {
						throw new BadRequestException("INCORRECT_UPDATE_ENTRY_NODE_VALUE_ENTRY_ID");
					} else if (fields == null || fields.isEmpty()) {
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
					if (!global.isDocumentIdExists(to, "Users_" + companyId)) {
						throw new BadRequestException("USER_NOT_EXISTS");
					}
				}
			}
			// REMOVING BLANK SPACE FROM SLA NAME
			String slaName = sla.getName();
			slaName = slaName.trim();
			sla.setName(slaName);

			// SETTING FIELD NAME
			String slaFieldName = slaName.toUpperCase();
			slaFieldName = slaFieldName.replaceAll("\\s+", "_");

			// DEFAULT FIELDS
			sla.setSlaId(UUID.randomUUID().toString());
			sla.setDateCreated(new Date());
			sla.setDateUpdated(new Date());
			sla.setCreatedBy(userId);
			sla.setLastUpdatedBy(userId);

			String slaTemplate = global.getFile("SlaField.json");

			slaTemplate = slaTemplate.replaceAll("DISPLAY_LABEL_REPLACE", sla.getName());
			slaTemplate = slaTemplate.replaceAll("FIELD_ID_REPLACE", UUID.randomUUID().toString());
			slaTemplate = slaTemplate.replaceAll("SLA_NAME_REPLACE", slaFieldName);

			JSONObject fieldObject = new JSONObject(slaTemplate);

			Document slaFieldDocument = Document.parse(fieldObject.toString());

			Document slaDocument = Document.parse(new ObjectMapper().writeValueAsString(sla));
			slaDocument.put("DATE_CREATED", new Date());
			slaDocument.put("DATE_UPDATED", new Date());

			ArrayList<Document> fieldDocuments = (ArrayList) module.get("FIELDS");
			if (fieldDocuments.size() >= 50) {
				throw new BadRequestException("MAX_FIELDS_REACHED");
			}

			// ADD FIELD TO MODULE
			collection.updateOne(Filters.eq("_id", module.getObjectId("_id")),
					Updates.addToSet("FIELDS", slaFieldDocument));

			// Load putmapping request for all the modules On elastic
			wrapper.putMappingForNewField(companyId, slaModuleId, slaFieldName, fieldDocuments.size() + 1);

			// ADD ENTRY TO SLA
			collection.findOneAndUpdate(Filters.eq("_id", new ObjectId(slaModuleId)),
					Updates.addToSet("SLAS", slaDocument));
			String subdomain = request.getAttribute("SUBDOMAIN").toString();

			publishToGraphql(subdomain);
			log.trace("Exit SLAService.postSla()  moduleId: " + slaModuleId);
			return sla;

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PostMapping("modules/{module_id}/sla/{slaId}")
	public ResponseEntity<Object> enableSla(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String slaModuleId, @PathVariable("slaId") String slaId) {
		try {

			log.trace("Enter SLAService.enableSla()  moduleId: " + slaModuleId);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			// GET COMPANY ID
			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String companyId = user.getString("COMPANY_ID");
			String roleId = user.getString("ROLE");

			// ACCESS MONGO
			MongoCollection<Document> collection = mongoTemplate.getCollection("modules_" + companyId);
			if (!ObjectId.isValid(slaModuleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			Document module = collection.find(Filters.eq("_id", new ObjectId(slaModuleId))).first();

			if (module == null) {
				throw new BadRequestException("MODULE_NOT_EXISTS");
			}
			if (!roleService.isAuthorizedForModule(userId, "DELETE", slaModuleId, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}
			if (!roleService.isSystemAdmin(roleId, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}
			boolean slaExist = false;
			List<Document> existingSlas = (List<Document>) module.get("SLAS");

			for (Document existingSla : existingSlas) {
				if (existingSla.getString("SLA_ID").equalsIgnoreCase(slaId)) {
					existingSla.put("DELETED", false);
					slaExist = true;
				}
			}
			if (!slaExist) {
				throw new BadRequestException("SLA_DOES_NOT_EXISTS");
			}

			collection.updateOne(Filters.eq("_id", new ObjectId(slaModuleId)), Updates.set("SLAS", existingSlas));

			log.trace("Exit SLAService.enableSla()  moduleId: " + slaModuleId);

			return new ResponseEntity<Object>(HttpStatus.OK);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new BadRequestException("INTERNAL_EXCEPTION");
	}

	@PutMapping("/modules/{module_id}/sla/{slaId}")
	public Sla putSLA(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String slaModuleId, @PathVariable("slaId") String slaId,
			@Valid @RequestBody Sla sla) {
		try {
			log.trace("Enter SlaService.putSla()  moduleId: " + slaModuleId);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			// GET COMPANY ID
			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String companyId = user.getString("COMPANY_ID");
			String roleId = user.getString("ROLE");

			// ACCESS MONGO
			MongoCollection<Document> collection = mongoTemplate.getCollection("modules_" + companyId);
			if (!ObjectId.isValid(slaModuleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			Document module = collection.find(Filters.eq("_id", new ObjectId(slaModuleId))).first();

			if (module == null) {
				throw new BadRequestException("MODULE_NOT_EXISTS");
			}
			if (!roleService.isAuthorizedForModule(userId, "PUT", slaModuleId, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}
			if (!roleService.isSystemAdmin(roleId, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			/*
			 * Document slaDoc = Document.parse(new ObjectMapper().writeValueAsString(sla));
			 * String slaDocId = slaDoc.getString("SLA_ID");
			 * if(!slaDocId.equalsIgnoreCase(slaId)) { throw new
			 * BadRequestException("SLA_ID_DOES_NOT_EXISTS"); } String slaName =
			 * slaDoc.getString("NAME"); if(!slaName.equalsIgnoreCase(sla.getName().trim()))
			 * { throw new BadRequestException("SLA_NAME_CANNOT_BE_UPDATED"); }
			 */

			List<Document> existingSlas = (List<Document>) module.get("SLAS");

			Document existingSlaDoc = null;
			for (Document existingSla : existingSlas) {
				if (existingSla.getString("NAME").equalsIgnoreCase(sla.getName())
						&& !existingSla.getString("SLA_ID").equalsIgnoreCase(slaId)) {

					throw new BadRequestException("SLA_NAME_ALREADY_EXISTS");
				}

				if (existingSla.getString("SLA_ID").equalsIgnoreCase(slaId)) {
					existingSlaDoc = existingSla;
					break;
				}
			}
			if (existingSlaDoc == null) {
				throw new BadRequestException("SLA_ID_DOES_NOT_EXISTS");
			}
			if (!existingSlaDoc.getString("NAME").equalsIgnoreCase(sla.getName())) {
				throw new BadRequestException("SLA_NAME_CANNOT_BE_UPDATED");
			}

			List<Document> moduleFields = (List<Document>) module.get("FIELDS");
			List<String> fieldIds = new ArrayList<String>();
			List<String> notSupportedViolationFields = new ArrayList<String>();
			List<String> discussionFields = new ArrayList<String>();
			List<String> notSupportedConditionFields = new ArrayList<String>();
			Hashtable<String, String> fieldToDataTypeMap = new Hashtable<String, String>();

			List<SlaConditions> conditions = sla.getConditions();
			List<Violation> violations = (List<Violation>) sla.getViolations();
			List<String> chronometerFieldIds = new ArrayList<String>();
			for (Document moduleField : moduleFields) {
				String fieldId = moduleField.getString("FIELD_ID");
				fieldIds.add(fieldId);

				Document fieldDataType = (Document) moduleField.get("DATA_TYPE");
				String dataType = fieldDataType.getString("DISPLAY");
				fieldToDataTypeMap.put(fieldId, dataType);

				if (dataType.equals("Chronometer")) {
					chronometerFieldIds.add(moduleField.getString("FIELD_ID"));
				}

				if (dataType.equalsIgnoreCase("Auto Number") || dataType.equalsIgnoreCase("Picklist (Multi-Select)")) {
					notSupportedViolationFields.add(fieldId);
				} else if (dataType.equalsIgnoreCase("Relationship")) {
					String relationshipType = moduleField.getString("RELATIONSHIP_TYPE");
					if (relationshipType.equalsIgnoreCase("one to many")
							|| relationshipType.equalsIgnoreCase("many to many")) {
						notSupportedViolationFields.add(fieldId);
						notSupportedConditionFields.add(fieldId);
					}
				} else if (dataType.equalsIgnoreCase("Discussion")) {
					discussionFields.add(fieldId);
					notSupportedConditionFields.add(fieldId);
				}

				for (Violation violation : violations) {
					if (dataType.equalsIgnoreCase("Picklist") && violation.getCondition().equals(fieldId)
							&& violation.getOpearator().equalsIgnoreCase("HAS_BEEN")) {
						List<String> picklistValues = (List<String>) moduleField.get("PICKLIST_VALUES");
						if (!picklistValues.contains(violation.getConditionValue())) {
							throw new BadRequestException("PICKLIST_VALUE_INVALID");
						}
					}
				}

				// CHECK VALIDITY FOR CONDITION PICKLIST VALUE
				for (SlaConditions condition : conditions) {
					if (!(condition.getOpearator().equalsIgnoreCase("EXISTS")
							|| condition.getOpearator().equalsIgnoreCase("DOES_NOT_EXIST"))) {
						if (dataType.equalsIgnoreCase("Picklist") && condition.getCondition().equals(fieldId)) {
							List<String> picklistValues = (List<String>) moduleField.get("PICKLIST_VALUES");
							if (!picklistValues.contains(condition.getConditionValue())) {
								throw new BadRequestException("CONDITION_PICKLIST_VALUE_INVALID");
							}
						}
					}
				}

			}
			int max_recurrence;
			boolean isRecurring = sla.isRecurring();
			max_recurrence = sla.getMaxRecurrence();
			if (!isRecurring) {
				sla.setIntervalTime(1);
			}
			if (!isRecurring) {
				sla.setMaxRecurrence(1);
				;
			} else {
				if (max_recurrence < 0) {
					throw new BadRequestException("SLA_RECURRENCE_NEGATIVE");
				} else if (max_recurrence > 99) {
					throw new BadRequestException("SLA_RECURRENCE_REACHED_LIMIT");
				} else {
					sla.setMaxRecurrence(max_recurrence);
				}
			}
			// CHECKING : CONDITION FIELD EXISTS
			for (SlaConditions condition : conditions) {
				isValidFieldAndCondition(condition.getCondition(), fieldIds, notSupportedConditionFields, null,
						condition.getOpearator(), null);
				if (!checkValidOperator(fieldToDataTypeMap, condition.getOpearator(), condition.getCondition())) {
					throw new BadRequestException("INVALID_OPERATOR");
				}
				if (chronometerFieldIds.contains(condition.getCondition())) {
					String value = condition.getConditionValue();
					String conditionValue = global.chronometerValueConversionInSeconds(value) + "";
					condition.setConditionValue(conditionValue);
				}
			}

			// CHECKING : VIOLATION FIELD EXISTS
			for (Violation violation : violations) {
				if (violation.getSlaExpiry() < 0) {
					throw new BadRequestException("INVALID_SLA_EXPIRY");
				}
				isValidFieldAndCondition(violation.getCondition(), fieldIds, notSupportedViolationFields,
						discussionFields, violation.getOpearator(), violation.getConditionValue());
				if (discussionFields.contains(violation.getCondition())) {
					if (teamExists(violation.getConditionValue(), companyId)) {
						continue;
					} else if (violation.getConditionValue().equalsIgnoreCase("REQUESTOR")) {
						continue;
					} else {
						throw new BadRequestException("TEAM_DOES_NOT_EXIST");
					}
				}
			}

			String moduleName = module.getString("NAME");

			// CHECKING WORKFLOW CONDITIONS
			List<Node> nodes = sla.getWorkflow().getNodes();
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
				} else if (node.getType().equals("DeleteEntry")) {
					String moduleId = node.getValues().getModuleId();
					String entryId = node.getValues().getEntryId();
					if (moduleId == null) {
						throw new BadRequestException("INCORRECT_DELETE_ENTRY_NODE_VALUE_ENTRY_ID");
					} else if (entryId == null) {
						throw new BadRequestException("INCORRECT_DELETE_ENTRY_NODE_VALUE_MODULE_ID");
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
					} else if (fields == null || fields.isEmpty()) {
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
					if (!global.isDocumentIdExists(to, "Users_" + companyId)) {
						throw new BadRequestException("USER_NOT_EXISTS");
					}
				}
			}
			// DEFAULT PUT FIELDS
			sla.setDateUpdated(new Date());
			sla.setLastUpdatedBy(userId);

			Document slaDocument = Document.parse(new ObjectMapper().writeValueAsString(sla));
			slaDocument.put("DELETED", false);

			slaDocument.put("DATE_CREATED", existingSlaDoc.getDate("DATE_CREATED"));
			slaDocument.put("DATE_UPDATED", new Date());

			collection.updateOne(Filters.eq("NAME", moduleName),
					Updates.pull("SLAS", Filters.eq("SLA_ID", sla.getSlaId())));

			// UPDATE ONE
			collection.updateOne(Filters.eq("NAME", moduleName), Updates.addToSet("SLAS", slaDocument));

			log.trace("Exit SLAService.putSla()  moduleId: " + slaModuleId);

			return sla;

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@DeleteMapping("/modules/{module_id}/sla/{sla_id}")
	public ResponseEntity<Object> deleteSla(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String slaModuleId, @PathVariable("sla_id") String slaId) {
		try {

			log.trace("Enter SLAService.deleteSla()  moduleId: " + slaModuleId);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			// GET COMPANY ID
			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String companyId = user.getString("COMPANY_ID");
			String roleId = user.getString("ROLE");

			// ACCESS MONGO
			MongoCollection<Document> collection = mongoTemplate.getCollection("modules_" + companyId);

			if (!ObjectId.isValid(slaModuleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			Document module = collection.find(Filters.eq("_id", new ObjectId(slaModuleId))).first();

			if (module == null) {
				throw new BadRequestException("MODULE_NOT_EXISTS");
			}
			if (!roleService.isAuthorizedForModule(userId, "DELETE", slaModuleId, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}
			if (!roleService.isSystemAdmin(roleId, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}
			boolean slaExist = false;
			List<Document> existingSlas = (List<Document>) module.get("SLAS");

			// CHECK SLA EXISTS
			for (Document existingSla : existingSlas) {
				if (existingSla.getString("SLA_ID").equalsIgnoreCase(slaId)) {
					existingSla.put("DELETED", true);
					slaExist = true;
				}
			}
			if (!slaExist) {
				throw new BadRequestException("SLA_DOES_NOT_EXISTS");
			}

			// DELETE SLA
			collection.updateOne(Filters.eq("_id", new ObjectId(slaModuleId)), Updates.set("SLAS", existingSlas));

			// Removing slaName field from current module if SLA is deleted
			String moduleName = module.getString("NAME");

			MongoCollection<Document> moduleCollection = mongoTemplate
					.getCollection(moduleName.replaceAll("\\s+", "_") + "_" + companyId);
			for (Document existingSla : existingSlas) {
				if (existingSla.getString("SLA_ID").equalsIgnoreCase(slaId)) {
					if (existingSla.getBoolean("DELETED")) {
						String slaName = existingSla.getString("NAME").toUpperCase().trim().replaceAll("\\s+", "_");
						moduleCollection.updateMany(Filters.eq("DELETED", false), Updates.unset(slaName));
					}
				}
			}

			// TODO: Review for performance
			return new ResponseEntity<Object>(HttpStatus.OK);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new BadRequestException("INTERNAL_EXCEPTION");
	}

	public boolean slaExistsInModule(String slaId, String collectionName, String moduleName) {
		try {
			log.trace("Enter SlaService.slaExistsInModule()  slaId: " + slaId + ", collectionName: " + collectionName
					+ ", moduleName: " + moduleName);
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			// Get Document
			Document module = collection.find(Filters.eq("NAME", moduleName)).first();
			if (module.get("SLAS") != null) {
				ArrayList<Document> documents = (ArrayList) module.get("SLAS");
				// GET SPECIFIC FIELD
				for (Document document : documents) {
					if (document.getString("SLA_ID").equals(slaId)) {
						log.trace("Exit SLAService.slaExistsInModule()  slaId: " + slaId + ", collectionName: "
								+ collectionName + ", moduleName: " + moduleName);
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

	public boolean moduleExists(String id, String companyId) {
		try {
			// ACCESS MONGO
			log.trace("Enter SlaService.moduleExists()  id: " + id + ", companyId: " + companyId);
			MongoCollection<Document> collection = mongoTemplate.getCollection("modules_" + companyId);
			if (new ObjectId().isValid(id)) {
				Document moduleDocument = collection.find(Filters.eq("_id", new ObjectId(id))).first();

				if (moduleDocument != null) {
					log.trace("Exit SlaService.moduleExists()  id: " + id + ", companyId: " + companyId);
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
			log.trace("Enter SlaService.fieldExists()  fieldId: " + fieldId + ", companyId: " + companyId
					+ ", moduleId: " + moduleId);
			// ACCESS MONGO
			MongoCollection<Document> collection = mongoTemplate.getCollection("modules_" + companyId);
			if (new ObjectId().isValid(moduleId)) {
				Document fieldDocument = collection.find(
						Filters.and(Filters.eq("_id", new ObjectId(moduleId)), Filters.eq("FIELDS.FIELD_ID", fieldId)))
						.first();

				if (fieldDocument != null) {

					log.trace("Exit SlaService.fieldExists()  fieldId: " + fieldId + ", companyId: " + companyId
							+ ", moduleId: " + moduleId);
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
			log.trace("Enter SlaService.entryExists()  entryId: " + entryId + ", companyId: " + companyId
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
					log.trace("Exit SlaService.entryExists()  entryId: " + entryId + ", companyId: " + companyId
							+ ", moduleId: " + moduleId);
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
			log.trace("Enter SlaService.teamExists()  teamId: " + teamId + ", companyId: " + companyId);
			MongoCollection<Document> teamsCollection = mongoTemplate.getCollection("Teams_" + companyId);
			if (new ObjectId().isValid(teamId)) {
				Document teamDocument = teamsCollection.find(Filters.eq("_id", new ObjectId(teamId))).first();

				if (teamDocument != null) {
					log.trace("Exit SlaService.teamExists()  teamId: " + teamId + ", companyId: " + companyId);
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

	public boolean isValidModuleWorkflowVariable(String value, String moduleName, String companyId) {
		try {
			// ACCESS MONGO
			log.trace("Enter SlaService.isValidModuleWorkflowVariable()  value: " + value + ", moduleName: "
					+ moduleName + ", companyId: " + companyId);
			MongoCollection<Document> collection = mongoTemplate.getCollection("modules_" + companyId);
			Document fieldDocument = collection
					.find(Filters.and(Filters.eq("NAME", moduleName), Filters.eq("FIELDS.FIELD_ID", value))).first();
			if (fieldDocument != null) {
				log.trace("Exit SlaService.isValidModuleWorkflowVariable()  value: " + value + ", moduleName: "
						+ moduleName + ", companyId: " + companyId);
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new BadRequestException("INTERNAL_ERROR");
		}
	}

	public void isValidFieldAndCondition(String conditionFieldId, List<String> fieldIds,
			List<String> notSupportedFields, List<String> discussionFields, String conditionString,
			String conditionValue) {

		// FUNCTION TO HANDLE VALIDATION OF VIOLATION AND CONDITION FIELDS;
		try {
			if (discussionFields != null) {
				if (!fieldIds.contains(conditionFieldId)) {
					throw new BadRequestException("FIELD_DOES_NOT_EXIST");

				} else if (notSupportedFields.contains(conditionFieldId)) {
					throw new BadRequestException("RESTRICTED_SLA_FIELD");

				} else if (!discussionFields.contains(conditionFieldId)
						&& conditionString.equalsIgnoreCase("HAS_NOT_BEEN_REPLIED_BY")) {
					throw new BadRequestException("INVALID_OPERATOR");

				} else if (discussionFields.contains(conditionFieldId)
						&& !conditionString.equalsIgnoreCase("HAS_NOT_BEEN_REPLIED_BY")) {
					throw new BadRequestException("INVALID_OPERATOR");
				}
			} else if (!fieldIds.contains(conditionFieldId)) {
				throw new BadRequestException("FIELD_DOES_NOT_EXIST");

			} else if (notSupportedFields.contains(conditionFieldId)) {
				throw new BadRequestException("RESTRICTED_SLA_FIELD");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public boolean checkValidOperator(Hashtable<String, String> fieldsToDataTypeMap, String conditionOperator,
			String conditionFieldId) {
		try {

			String dataTypeOfConditionField = fieldsToDataTypeMap.get(conditionFieldId);

			if (conditionOperator.equalsIgnoreCase("EXISTS") || conditionOperator.equalsIgnoreCase("DOES_NOT_EXIST")) {
				return true;
			} else if (dataTypeOfConditionField.equalsIgnoreCase("Text")
					|| dataTypeOfConditionField.equalsIgnoreCase("Picklist")) {
				if (!global.validTextOperators.contains(conditionOperator)) {
					return false;
				}
			} else if (dataTypeOfConditionField.equalsIgnoreCase("Number")) {
				if (!global.validNumericOperators.contains(conditionOperator)) {
					return false;
				}
			} else if (dataTypeOfConditionField.equalsIgnoreCase("Date/Time")
					|| dataTypeOfConditionField.equalsIgnoreCase("Date")
					|| dataTypeOfConditionField.equalsIgnoreCase("Time")) {
				if (!global.validDateOperators.contains(conditionOperator)) {
					return false;
				}
			} else if (dataTypeOfConditionField.equalsIgnoreCase("Relationship")
					|| dataTypeOfConditionField.equalsIgnoreCase("Checkbox")) {
				if (!global.validRelationOperators.contains(conditionOperator)) {
					return false;
				}
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return true;
	}

	public void publishToGraphql(String subdomain) {
		log.debug("Publishing to graphql queue");
		rabbitTemplate.convertAndSend("update-company-schema", subdomain);
	}

}
