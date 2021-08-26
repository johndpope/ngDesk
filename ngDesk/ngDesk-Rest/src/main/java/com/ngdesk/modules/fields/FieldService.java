package com.ngdesk.modules.fields;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.UrlValidator;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
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
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.ngdesk.Authentication;
import com.ngdesk.Global;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.ForbiddenException;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.roles.FieldPermission;
import com.ngdesk.roles.RoleService;
import com.ngdesk.wrapper.Wrapper;

@RestController
@Component
public class FieldService {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Global global;

	@Autowired
	private Authentication auth;

	@Autowired
	RoleService roleService;

	@Autowired
	private SimpMessagingTemplate template;

	@Autowired
	Wrapper wrapper;

	private final Logger log = LoggerFactory.getLogger(FieldService.class);

	// this call is used for master page of fields
	@GetMapping("/modules/{module_id}/fields/master")
	public ResponseEntity<Object> getFieldsMaster(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId,
			@RequestParam(value = "page_size", required = false) String pageSize,
			@RequestParam(value = "page", required = false) String page,
			@RequestParam(value = "sort", required = false) String sort,
			@RequestParam(value = "order", required = false) String order) {

		JSONArray fields = new JSONArray();
		int totalSize = 0;
		JSONObject resultObj = new JSONObject();

		try {
			log.trace("Enter FieldService.getFields() moduleId: " + moduleId);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");
			String userId = user.getString("USER_ID");
			String collectionName = "modules_" + companyId;

			// Retrieving a collection
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			// Get Document
			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
			if (module != null) {
				String moduleName = module.getString("NAME");
				if (!roleService.isAuthorizedForModule(userId, "GET", moduleId, companyId)) {
					throw new ForbiddenException("FORBIDDEN");
				}
				ArrayList<Document> fieldDocuments = (ArrayList) module.get("FIELDS");
				totalSize = fieldDocuments.size();
				AggregateIterable<Document> sortedDocuments = null;
				List<String> fieldNames = new ArrayList<String>();
				fieldNames.add("FIELD_ID");
				fieldNames.add("NAME");
				fieldNames.add("DISPLAY_LABEL");
				fieldNames.add("DATA_TYPE");

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
					sort = "FIELDS." + sort;
					if (order.equalsIgnoreCase("asc")) {
						sortedDocuments = collection.aggregate(Arrays
								.asList(Aggregates.unwind("$FIELDS"), Aggregates.match(Filters.eq("NAME", moduleName)),
										Aggregates.sort(Sorts.orderBy(Sorts.ascending(sort))),
										Aggregates.project(Filters.and(
												Projections.computed("FIELDS", Projections.include(fieldNames)),
												Projections.excludeId())),
										Aggregates.skip(skip), Aggregates.limit(pgSize)));
					} else if (order.equalsIgnoreCase("desc")) {
						sortedDocuments = collection.aggregate(Arrays
								.asList(Aggregates.unwind("$FIELDS"), Aggregates.match(Filters.eq("NAME", moduleName)),
										Aggregates.sort(Sorts.orderBy(Sorts.descending(sort))),
										Aggregates.project(Filters.and(
												Projections.computed("FIELDS", Projections.include(fieldNames)),
												Projections.excludeId())),
										Aggregates.skip(skip), Aggregates.limit(pgSize)));
					} else {
						throw new BadRequestException("INVALID_SORT_ORDER");
					}
				} else {
					sortedDocuments = collection.aggregate(Arrays.asList(Aggregates.unwind("$FIELDS"),
							Aggregates.match(Filters.eq("NAME", moduleName)),
							Aggregates.project(
									Filters.and(Projections.computed("FIELDS", Projections.include(fieldNames)),
											Projections.excludeId())),
							Aggregates.skip(skip), Aggregates.limit(pgSize)));
				}

				for (Document document : sortedDocuments) {
					Document data = (Document) document.get("FIELDS");
					fields.put(data);
				}

			} else {
				throw new BadRequestException("MODULE_NOT_EXISTS");
			}
			resultObj.put("FIELDS", fields);
			resultObj.put("TOTAL_RECORDS", totalSize);
			log.trace("Exit FieldService.getFields() moduleId: " + moduleId);
			return new ResponseEntity<>(resultObj.toString(), Global.postHeaders, HttpStatus.OK);

		} catch (JSONException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");

	}

	@GetMapping("/modules/{module_id}/fields")
	public ResponseEntity<Object> getFields(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId,
			@RequestParam(value = "page_size", required = false) String pageSize,
			@RequestParam(value = "page", required = false) String page,
			@RequestParam(value = "sort", required = false) String sort,
			@RequestParam(value = "order", required = false) String order) {

		JSONArray fields = new JSONArray();
		int totalSize = 0;
		JSONObject resultObj = new JSONObject();

		try {
			log.trace("Enter FieldService.getFields() moduleId: " + moduleId);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");
			String userId = user.getString("USER_ID");
			String collectionName = "modules_" + companyId;

			// Retrieving a collection
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			// Get Document
			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
			if (module != null) {
				String moduleName = module.getString("NAME");
				if (!roleService.isAuthorizedForModule(userId, "GET", moduleId, companyId)) {
					throw new ForbiddenException("FORBIDDEN");
				}
				ArrayList<Document> fieldDocuments = (ArrayList) module.get("FIELDS");
				totalSize = fieldDocuments.size();
				AggregateIterable<Document> sortedDocuments = null;

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
					sort = "FIELDS." + sort;
					if (order.equalsIgnoreCase("asc")) {
						sortedDocuments = collection.aggregate(Arrays.asList(Aggregates.unwind("$FIELDS"),
								Aggregates.match(Filters.eq("NAME", moduleName)),
								Aggregates.sort(Sorts.orderBy(Sorts.ascending(sort))),
								Aggregates
										.project(Filters.and(Projections.computed("FIELDS", Projections.excludeId()))),
								Aggregates.skip(skip), Aggregates.limit(pgSize)));
					} else if (order.equalsIgnoreCase("desc")) {
						sortedDocuments = collection.aggregate(Arrays.asList(Aggregates.unwind("$FIELDS"),
								Aggregates.match(Filters.eq("NAME", moduleName)),
								Aggregates.sort(Sorts.orderBy(Sorts.descending(sort))),
								Aggregates
										.project(Filters.and(Projections.computed("FIELDS", Projections.excludeId()))),
								Aggregates.skip(skip), Aggregates.limit(pgSize)));
					} else {
						throw new BadRequestException("INVALID_SORT_ORDER");
					}
				} else {
					sortedDocuments = collection.aggregate(Arrays.asList(Aggregates.unwind("$FIELDS"),
							Aggregates.match(Filters.eq("NAME", moduleName)),
							Aggregates.project(Filters.and(Projections.computed("FIELDS", Projections.excludeId()))),
							Aggregates.skip(skip), Aggregates.limit(pgSize)));
				}

				for (Document document : sortedDocuments) {
					Document data = (Document) document.get("FIELDS");
					fields.put(data);
				}

			} else {
				throw new BadRequestException("MODULE_NOT_EXISTS");
			}
			resultObj.put("FIELDS", fields);
			resultObj.put("TOTAL_RECORDS", totalSize);
			log.trace("Exit FieldService.getFields() moduleId: " + moduleId);
			return new ResponseEntity<>(resultObj.toString(), Global.postHeaders, HttpStatus.OK);

		} catch (JSONException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");

	}

	@GetMapping("/modules/{module_id}/fields/{field_name}")
	public Field getField(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId, @PathVariable("field_name") String fieldName)
			throws JsonParseException, JsonMappingException, IOException {
		try {
			log.trace("Enter FieldService.getField() moduleId: " + moduleId + ", fieldName: " + fieldName);
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

			if (module != null) {

				String moduleName = module.getString("NAME");

				if ((moduleName.equals("Users") && fieldName.equals("PASSWORD"))) {
					throw new ForbiddenException("FIELD_NOT_EXISTS");
				}

				if (fieldName.equalsIgnoreCase("DELETED")) {
					throw new ForbiddenException("FIELD_NOT_EXISTS");
				}

				if (!roleService.isAuthorizedForModule(userId, "GET", moduleId, companyId)) {
					throw new ForbiddenException("FORBIDDEN");
				}
				if (global.isExistsInModule("FIELDS", fieldName, collectionName, moduleName)) {
					ArrayList<Document> fieldDocuments = (ArrayList) module.get("FIELDS");
					// GET SPECIFIC FIELD
					for (Document fieldDocument : fieldDocuments) {
						if (fieldDocument.getString("NAME").equals(fieldName)) {
							Field field = new ObjectMapper().readValue(fieldDocument.toJson(), Field.class);
							log.trace("Exit FieldService.getField() moduleName: " + moduleName + ", fieldName: "
									+ fieldName);
							return field;
						}
					}
				} else {
					throw new ForbiddenException("FIELD_NOT_EXISTS");
				}
			} else {
				throw new ForbiddenException("MODULE_NOT_EXISTS");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@GetMapping("/modules/{module_id}/fields/id/{field_id}")
	public Field getFieldById(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId, @PathVariable("field_id") String fieldId)
			throws JsonParseException, JsonMappingException, IOException {
		try {
			log.trace("Enter FieldService.getFieldById() moduleId: " + moduleId + ", fieldId: " + fieldId);
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

			if (module != null) {
				String moduleName = module.getString("NAME");
				if ((moduleName.equals("Users") && fieldId.equals("cb3a9d64-0b46-43f0-8452-6038429e6bcb"))) {
					throw new ForbiddenException("FIELD_NOT_EXISTS");
				}

				if (!roleService.isAuthorizedForModule(userId, "GET", moduleId, companyId)) {
					throw new ForbiddenException("FORBIDDEN");
				}
				if (collection.find(Filters.and(Filters.eq("NAME", moduleName),
						Filters.elemMatch("FIELDS", Filters.eq("FIELD_ID", fieldId)))).first() != null) {
					ArrayList<Document> fieldDocuments = (ArrayList) module.get("FIELDS");
					// GET SPECIFIC FIELD
					for (Document fieldDocument : fieldDocuments) {

						if (fieldDocument.get("FIELD_ID") != null
								&& fieldDocument.getString("FIELD_ID").equals(fieldId)) {
							String fieldString = new ObjectMapper().writeValueAsString(fieldDocument);
							Field field = new ObjectMapper().readValue(fieldString, Field.class);

							// GETTING DATA FILTER FROM THE RELATIONFIELD FIELD
							if (field.getDatatypes().getDisplay().equalsIgnoreCase("relationship")
									&& field.getModule() != null && field.getRelationshipField() != null) {
								Document relationModule = collection
										.find(Filters.eq("_id", new ObjectId(field.getModule()))).first();
								List<Document> fields = (List<Document>) relationModule.get("FIELDS");
								for (Document relationField : fields) {
									if (relationField.getString("FIELD_ID")
											.equalsIgnoreCase(field.getRelationshipField())) {
										if (relationField.get("DATA_FILTER") == null) {
											field.setDataFilter(null);
										} else {
											Document relationDataFilter = (Document) relationField.get("DATA_FILTER");
											DataFilter dataFilter = new ObjectMapper()
													.readValue(relationDataFilter.toJson(), DataFilter.class);
											field.setDataFilter(dataFilter);
										}
									}
								}
							}

							log.trace("Exit FieldService.getFieldById() moduleName: " + moduleName + ", fieldId: "
									+ fieldId);
							return field;
						}
					}
				} else {
					throw new ForbiddenException("FIELD_NOT_EXISTS");
				}
			} else {
				throw new ForbiddenException("MODULE_NOT_EXISTS");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PostMapping("/modules/{module_id}/fields/{field_name}")
	public Field postFields(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId, @PathVariable("field_name") String fieldName,
			@Valid @RequestBody Field field) {
		try {
			log.trace("Enter FieldService.postFields() moduleId: " + moduleId + ", fieldName: " + fieldName);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			
			fieldName = fieldName.trim();
			fieldName = fieldName.toUpperCase();
			fieldName = fieldName.replaceAll("\\s+", "_");
			
			String fName = field.getName();
			fName = fName.trim();
			fName = fName.toUpperCase();
			fName = fName.replaceAll("\\s+", "_");
			
			field.setName(fName);
			
			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String companyId = user.getString("COMPANY_ID");
			String collectionName = "modules_" + companyId;

			field.setDateCreated(new Timestamp(new Date().getTime()));
			field.setCreatedBy(userId);

			if (global.restrictedFieldNames.contains(field.getName())) {
				throw new BadRequestException("RESTRICTED_FIELDS_SAVE");
			}

			if (field.getDatatypes().getDisplay().equals("Relationship")) {
				throw new BadRequestException("INVALID_REQUEST");
			}

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			List<Document> modules = (List<Document>) collection.find().into(new ArrayList<>());

			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();

			if (module != null) {
				String moduleName = module.getString("NAME");

				if (!roleService.isAuthorizedForModule(userId, "POST", moduleId, companyId)) {
					throw new ForbiddenException("FORBIDDEN");
				}
				ArrayList<Document> fieldDocuments = (ArrayList) module.get("FIELDS");
				if (fieldDocuments.size() >= 100) {
					throw new BadRequestException("MAX_FIELDS_REACHED");
				}
				for (Document document : fieldDocuments) {
					if (document.getString("NAME").equals(field.getName())) {
						throw new BadRequestException("FIELD_EXISTS");
					}
				}
				if (!verifyInternal(field)) {
					throw new BadRequestException("INTERNAL_INVALID");
				}

				if (!isFieldDiscussion(module, field, collectionName)) {
					throw new BadRequestException("FIELD_DISCUSSION_EXISTS");
				}

				String displayDataType = field.getDatatypes().getDisplay();

				if (displayDataType.equals("Picklist")) {
					if (field.getPicklistValues().size() < 1) {
						throw new BadRequestException("PICKLIST_VALUES_EMPTY");
					}
					List<String> picklistValues = field.getPicklistValues();
					Set<String> uniqueValues = new HashSet<String>(picklistValues);

					if (picklistValues.size() != uniqueValues.size()) {
						throw new BadRequestException("PICKLIST_VALUES_NOT_UNIQUE");
					}
				}

				if (displayDataType.equals("Picklist (Multi-Select)")) {
					if (field.getPicklistValues().size() < 1) {
						throw new BadRequestException("PICKLIST_VALUES_EMPTY");
					}
				}
				if (field.getName().contains(".")) {
					throw new BadRequestException("INVALID_FIELD_NAME");
				}

				if (!displayDataType.equals("Formula") && !field.getDatatypes().getBackend().equals("Formula")) {
					if (field.getFormula() != null) {
						throw new BadRequestException("INVALID_REQUEST");

					}
				}

				if (displayDataType.equals("Button")) {
					if (field.getWorkflow() == null) {
						throw new BadRequestException("WORKFLOW_NULL");
					}

					List<Document> workflows = (List<Document>) module.get("WORKFLOWS");
					boolean workflowFound = false;
					for (Document workflow : workflows) {
						String workflowId = workflow.getString("WORKFLOW_ID");
						if (workflowId.equalsIgnoreCase(field.getWorkflow())) {
							workflowFound = true;
							break;
						}
					}
					if (!workflowFound) {
						throw new BadRequestException("INVALID_WORKFLOW");
					}

				}

				if (displayDataType.equals("Aggregate")) {
					if (field.getAggregationField() == null) {
						throw new BadRequestException("AGGREGATION_FIELD_NULL");
					}

					if (field.getAggregationType() == null) {
						throw new BadRequestException("NOT_VALID_AGGREGATION_TYPE");
					}

					if (field.getAggregationRelatedField() == null) {
						throw new BadRequestException("RELATIONSHIP_FIELD_REQUIRED");
					}

					Optional<Document> optional = fieldDocuments.stream()
							.filter(f -> f.getString("FIELD_ID").equals(field.getAggregationField())).findAny();

					if (!optional.isPresent()) {
						throw new BadRequestException("AGGREGATION_FIELD_SHOULD_BE_ONE_TO_MANY");
					}

					Document relationshipField = optional.get();
					Document relatedModule = collection
							.find(Filters.eq("_id", new ObjectId(relationshipField.getString("MODULE")))).first();

					List<Document> relatedModuleFields = (List<Document>) relatedModule.get("FIELDS");
					Optional<Document> optionalRelatedField = relatedModuleFields.stream()
							.filter(f -> f.getString("FIELD_ID").equals(field.getAggregationRelatedField())).findAny();

					if (!optionalRelatedField.isPresent()) {
						throw new BadRequestException("INVALID_AGGREGATION_RELATIONSHIP_FIELD");
					}

					Document relatedField = optionalRelatedField.get();
					Document dataType = (Document) relatedField.get("DATA_TYPE");
					String backendDataType = dataType.getString("BACKEND");

					if (!backendDataType.equals("Float") && !backendDataType.equals("Integer")
							&& !backendDataType.equals("Double")) {
						throw new BadRequestException("INVALID_AGGREGATION_RELATIONSHIP_FIELD");
					}
				}

				if (field.getDefaultValue() != null && !field.getDefaultValue().equals("")) {
					isValidDefaultValue(field, companyId);
				}
				if (field.getDatatypes().getDisplay().equals("Chronometer") && field.getDefaultValue() != null) {
					long chronometerValueInSecond = 0;
					String value = field.getDefaultValue().toString();
					String valueWithoutSpace = value.replaceAll("\\s+", "");
					if (valueWithoutSpace.length() == 0 || valueWithoutSpace.charAt(0) == '-') {
						field.setDefaultValue(null);
					} else if (valueWithoutSpace.length() != 0) {
						chronometerValueInSecond = global.chronometerValueConversionInSeconds(valueWithoutSpace);
						field.setDefaultValue(chronometerValueInSecond + "");
					}
				}

				if (field.getDatatypes().getDisplay().equals("Address")) {
					String[] TypeName = { "Street 1", "Street 2", "City", "State", "Zipcode" };
					String groupId = UUID.randomUUID().toString();

					for (int i = 0; i < TypeName.length; i++) {
						String name = null;
						DataType dataType = new DataType();
						String displayType = TypeName[i];
						dataType.setDisplay(displayType);
						dataType.setBackend("String");
						Field fieldobj = new Field();
						fieldobj.setFieldId(UUID.randomUUID().toString());
						fieldobj.setDisplayLabel(field.getName().toLowerCase() + " " + displayType);
						if (displayType.contains("Street")) {
							name = displayType.substring(0, 6).toUpperCase() + "_" + displayType.split("Street ")[1];
						} else {
							name = displayType.toUpperCase();
						}
						fieldobj.setName(field.getName() + "_" + name);
						fieldobj.setDatatypes(dataType);
						fieldobj.setGroupId(groupId);
						fieldobj.setPicklist("Enter Values");
						String fieldBody = new ObjectMapper().writeValueAsString(fieldobj).toString();
						Document fieldDocument = Document.parse(fieldBody);
						collection.updateOne(Filters.eq("NAME", moduleName), Updates.addToSet("FIELDS", fieldDocument));

					}

				} else {

					field.setFieldId(UUID.randomUUID().toString());
					String fieldBody = new ObjectMapper().writeValueAsString(field).toString();
					Document fieldDocument = Document.parse(fieldBody);
					collection.updateOne(Filters.eq("NAME", moduleName), Updates.addToSet("FIELDS", fieldDocument));
				}

				updateRoles(moduleId, field.getFieldId(), companyId);
				int size = 0;
				int totalDataTimeField = 0;
				for (Document doc : fieldDocuments) {

					Document type = (Document) doc.get("DATA_TYPE");
					if (type.getString("DISPLAY").equals("Date/Time") || type.getString("DISPLAY").equals("Date")
							|| type.getString("DISPLAY").equals("Time")) {

						totalDataTimeField++;
					}
				}

				if (totalDataTimeField == 0 && !field.getDatatypes().getDisplay().equals("Date/Time")
						&& !field.getDatatypes().getDisplay().equals("Date")
						&& !field.getDatatypes().getDisplay().equals("Time")) {
					size = fieldDocuments.size();

				} else if (totalDataTimeField == 0 && (field.getDatatypes().getDisplay().equals("Date/Time")
						|| field.getDatatypes().getDisplay().equals("Date")
						|| field.getDatatypes().getDisplay().equals("Time"))) {
					size = 84;
				} else if (totalDataTimeField != 0 && !field.getDatatypes().getDisplay().equals("Date/Time")
						&& !field.getDatatypes().getDisplay().equals("Date")
						&& !field.getDatatypes().getDisplay().equals("Time")) {

					size = fieldDocuments.size() - totalDataTimeField;
				} else if (totalDataTimeField != 0 && (field.getDatatypes().getDisplay().equals("Date/Time")
						|| field.getDatatypes().getDisplay().equals("Date")
						|| field.getDatatypes().getDisplay().equals("Time"))) {

					size = 84 + totalDataTimeField;
				}

				// Load putmapping request for all the modules On elastic
				wrapper.putMappingForNewField(companyId, moduleId, field.getName(), size + 1);

				if (field.getDatatypes().getDisplay().equals("Auto Number") && field.isAutonumberGeneration()) {
					// GENERATE AUTONUMBER TO ALL EXISTING ENTRIES
					generateAutoNumberToExistingEntries(moduleName, companyId, field, moduleId);
				}

				// INDEX NOT CREATED FOR DISCUSSION, PICKLIST (MULIT-SELECT), PHONE.
				createIndexForTheField(companyId, moduleId, field);

				log.trace("Exit FieldService.postFields() moduleName: " + moduleName + ", fieldName: " + fieldName);
				this.template.convertAndSend("rest/" + moduleId + "/channels/email/mapping/field-created",
						"Field Created Successfully");
				return field;
			} else {
				throw new ForbiddenException("MODULE_NOT_EXISTS");
			}

		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PostMapping("/modules/{module_id}/fields/{field_name}/relationship")
	public Field postRelationshipField(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId, @PathVariable("field_name") String fieldName,
			@RequestParam("required") Boolean requiredField2,
			@RequestParam("primary_display_field") String displayField,
			@RequestParam("display_name") String displayName, @RequestParam("name") String field2Name,
			@Valid @RequestBody Field field) {

		log.trace("Enter FieldService.postRelationshipField() moduleId: " + moduleId + ", fieldName: " + fieldName);

		try {
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			if (displayName == null || displayName.trim().isEmpty()) {
				throw new BadRequestException("DISPLAY_NAME_REQUIRED");
			}

			if (field2Name == null || field2Name.trim().isEmpty()) {
				throw new BadRequestException("SYSTEM_NAME_REQUIRED");
			}

			if (displayField == null || displayField.trim().isEmpty()) {
				if (field.getRelationshipType().equals("One to Many")
						|| field.getRelationshipType().equals("One to One")) {
					throw new BadRequestException("PRIMARY_DISPLAY_FIELD_REQUIRED");
				}
			}

			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String companyId = user.getString("COMPANY_ID");
			String collectionName = "modules_" + companyId;

			if (global.restrictedFieldNames.contains(field.getName())) {
				throw new BadRequestException("FIELD_NAME_INVALID");
			}

			if (!field.getDatatypes().getDisplay().equals("Relationship")) {
				throw new BadRequestException("INVALID_REQUEST");
			}

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();

			if (module != null) {

				String moduleName = module.getString("NAME");
				if (!roleService.isAuthorizedForModule(userId, "POST", moduleId, companyId)) {
					throw new ForbiddenException("FORBIDDEN");
				}
				ArrayList<Document> fieldDocuments = (ArrayList) module.get("FIELDS");
				for (Document document : fieldDocuments) {
					if (document.getString("NAME").equals(field.getName())) {
						throw new BadRequestException("FIELD_EXISTS");
					}
					if (fieldDocuments.size() >= 100) {
						throw new BadRequestException("MAX_FIELDS_REACHED");
					}

					if (document.getString("FIELD_ID").equals(displayField)) {
						Document datatype = (Document) document.get("DATA_TYPE");
						if (datatype.getString("DISPLAY").equals("Relationship")) {
							throw new BadRequestException("PRIMARY_DISPLAY_CANNOT_BE_RELATIONSHIP");
						}
					}
				}
				if (!verifyInternal(field)) {
					throw new BadRequestException("INTERNAL_INVALID");
				}

				if (field.getRelationshipType() == null) {
					throw new BadRequestException("RELATIONSHIP_TYPE_REQUIRED");
				}

				if (field.getRelationshipType().equals("Many to One")) {
					throw new BadRequestException("NOT_VALID_RELATIONSHIP_TYPE");
				} else if (field.getRelationshipType().equals("One to Many")) {
					if (field.isRequired()) {
						throw new BadRequestException("REQUIRED_FIELD_MUST_BE_FALSE");
					}
				}

				Document relationModule = null;
				if (field.getModule() == null) {
					throw new BadRequestException("MODULE_REQUIRED");
				} else {
					if (!new ObjectId().isValid(field.getModule())) {
						throw new BadRequestException("INVALID_MODULE_ID");
					}
					relationModule = collection.find(Filters.eq("_id", new ObjectId(field.getModule()))).first();
					if (relationModule == null) {
						throw new ForbiddenException("MODULE_NOT_EXISTS");
					}
				}

				if (field.getLookupRelationshipField() == null) {
					if (field.getRelationshipType().equals("One to One"))
						throw new BadRequestException("RELATIONSHIP_FIELD_REQUIRED");
				}

				List<Document> relationModuleFields = (List<Document>) relationModule.get("FIELDS");
				HashSet<String> fieldIds = new HashSet<String>();
				for (Document moduleField : relationModuleFields) {
					fieldIds.add(moduleField.getString("FIELD_ID"));
					if (field.getRelationshipType().equals("One to One")) {
						if (field.getLookupRelationshipField().equals(moduleField.getString("FIELD_ID"))) {
							Document datatype = (Document) moduleField.get("DATA_TYPE");
							if (datatype.getString("DISPLAY").equals("Relationship")) {
								throw new BadRequestException("PRIMARY_DISPLAY_CANNOT_BE_RELATIONSHIP");
							}
						}
					}
				}

				if (field.getRelationshipType().equals("One to One")) {
					if (!fieldIds.contains(field.getLookupRelationshipField())) {
						throw new BadRequestException("RELATIONSHIP_FIELD_NOT_EXISTS");
					}
				}
				Field field2 = new Field();
				field2.setFieldId(UUID.randomUUID().toString());
				field.setFieldId(UUID.randomUUID().toString());
				field.setRelationshipField(field2.getFieldId());
				field2.setDataFilter(field.getDataFilter());
				field.setDataFilter(null);

				String fieldJson = new ObjectMapper().writeValueAsString(field);
				Document fieldDocument = Document.parse(fieldJson);

				collection.updateOne(Filters.eq("NAME", moduleName), Updates.addToSet("FIELDS", fieldDocument));

				// CREATING INDEX FOR FIRST FIELD
				createIndexForTheField(companyId, moduleId, field);

				FieldPermission permission = new FieldPermission();
				permission.setFieldId(field.getFieldId());
				permission.setPermission("Not Set");

				String permissionJson = new ObjectMapper().writeValueAsString(permission);
				Document document = Document.parse(permissionJson);
				updateRoles(moduleId, field.getFieldId(), companyId);
				field2.setName(field2Name);

				DataType datatype = new DataType();
				datatype.setDisplay("Relationship");

				if (field.getRelationshipType().equals("One to Many")) {
					field2.setRelationshipType("Many to One");
					field2.setRequired(requiredField2);
					datatype.setBackend("String");
				} else if (field.getRelationshipType().equals("Many to Many")) {
					field2.setRelationshipType("Many to Many");
					field2.setRequired(requiredField2);
					datatype.setBackend("Array");
				} else if (field.getRelationshipType().equals("One to One")) {
					field2.setRelationshipType("One to One");
					field2.setRequired(requiredField2);
					datatype.setBackend("String");
				}
				field2.setDisplayLabel(displayName);
				field2.setDatatypes(datatype);
				field2.setLookupRelationshipField(displayField);
				field2.setModule(module.getObjectId("_id").toString());
				field2.setRelationshipField(field.getFieldId());
				String field2Json = new ObjectMapper().writeValueAsString(field2);
				Document field2Doc = Document.parse(field2Json);
				collection.updateOne(Filters.eq("NAME", relationModule.getString("NAME")),
						Updates.addToSet("FIELDS", field2Doc));

				// CREATE INDEX FOR FIELD2
				createIndexForTheField(companyId, field.getModule(), field2);

				String permissionJson2 = new ObjectMapper().writeValueAsString(permission);
				Document document2 = Document.parse(permissionJson2);
				updateRoles(moduleId, field2.getFieldId(), companyId);
				this.template.convertAndSend("rest/" + field.getModule() + "/channels/email/mapping/field-created",
						"Field Created Successfully");
				return field;
			} else {
				throw new ForbiddenException("MODULE_NOT_EXISTS");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		log.trace("Exit FieldService.postRelationshipField() moduleId: " + moduleId + ", fieldName: " + fieldName);
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	private void generateAutoNumberToExistingEntries(String moduleName, String companyId, Field field,
			String moduleId) {
		try {
			String collectionName = moduleName.replaceAll("\\s+", "_") + "_" + companyId;
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			int count = (int) collection.countDocuments();

			int autoNumber = (int) field.getAutonumberStartingNumber();

			List<Document> entries = collection.find().sort(Sorts.ascending("DATE_CREATED")).skip(0).limit(count)
					.into(new ArrayList<Document>());
			for (Document entry : entries) {
				entry.put(field.getName(), autoNumber);

				String dataId = entry.remove("_id").toString();
				String entryString = new ObjectMapper().writeValueAsString(entry);
				wrapper.putData(companyId, moduleId, moduleName, entryString, dataId);

				autoNumber++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@PutMapping("/modules/{module_id}/fields/{field_name}")
	public Field putFields(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId, @PathVariable("field_name") String fieldName,
			@Valid @RequestBody Field field) {
		try {
			log.trace("Enter FieldService.putFields() moduleId: " + moduleId + ", fieldName: " + fieldName);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");
			String userId = user.getString("USER_ID");

			field.setDateUpdated(new Timestamp(new Date().getTime()));
			field.setLastUpdatedBy(userId);
			String collectionName = "modules_" + companyId;

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();

			if (field.getFieldId() != null) {
				if (module != null) {
					String moduleName = module.getString("NAME");

					if (!roleService.isAuthorizedForModule(userId, "PUT", moduleId, companyId)) {
						throw new ForbiddenException("FORBIDDEN");
					}
					if (collection.find(Filters.and(Filters.eq("FIELDS.FIELD_ID", field.getFieldId()),
							Filters.eq("NAME", moduleName))).first() != null) {
						ArrayList<Document> fields = (ArrayList) module.get("FIELDS");
						for (Document fielddoc : fields) {

							if (fielddoc.getString("NAME").equals(field.getName()) && fielddoc.getBoolean("INTERNAL")) {
								throw new BadRequestException("RESTRICTED_FIELDS_SET");
							}
							if (!fielddoc.getString("FIELD_ID").equals(field.getFieldId())) {
//								if (fielddoc.getString("NAME").equals(field.getName())) {
//									throw new BadRequestException("FIELD_EXISTS");
//								}

								Document fieldDataTypeDoc = (Document) fielddoc.get("DATA_TYPE");
								if (field.getDatatypes().getDisplay().equals("Discussion")
										&& fieldDataTypeDoc.get("DISPLAY").equals("Discussion")) {
									throw new BadRequestException("FIELD_DISCUSSION_EXISTS");
								}
							} else {
								// SAME FIELD
								JSONObject existingField = new JSONObject(fielddoc.toJson());
								String existingFieldName = existingField.getString("NAME");
								String datatypeDisplay = field.getDatatypes().getDisplay();
								String datatypeBackend = field.getDatatypes().getBackend();

								String existingDisplayDatatype = existingField.getJSONObject("DATA_TYPE")
										.getString("DISPLAY");
								String existingBackEndDatatype = existingField.getJSONObject("DATA_TYPE")
										.getString("BACKEND");

								if (!datatypeDisplay.equals(existingDisplayDatatype)
										|| !existingBackEndDatatype.equals(datatypeBackend)) {
									throw new BadRequestException("DATA_TYPE_MODIFIED");
								}
								if (!field.getName().equals(existingFieldName)) {
									throw new BadRequestException("FIELD_NAME_CANNOT_BE_CHANGED");
								}
							}
						}

						if (field.getDatatypes().getDisplay().equals("Button")) {
							if (field.getWorkflow() == null) {
								throw new BadRequestException("WORKFLOW_NULL");
							}

							List<Document> workflows = (List<Document>) module.get("WORKFLOWS");
							boolean workflowFound = false;
							for (Document workflow : workflows) {
								String workflowId = workflow.getString("WORKFLOW_ID");
								if (workflowId.equalsIgnoreCase(field.getWorkflow())) {
									workflowFound = true;
									break;
								}
							}
							if (!workflowFound) {
								throw new BadRequestException("INVALID_WORKFLOW");
							}

						}

						if (field.getDatatypes().getDisplay().equals("Chronometer")
								&& field.getDefaultValue() != null) {
							long chronometerValueInSecond = 0;
							String value = field.getDefaultValue().toString();
							String valueWithoutSpace = value.replaceAll("\\s+", "");
							if (valueWithoutSpace.length() == 0 || valueWithoutSpace.charAt(0) == '-') {
								field.setDefaultValue(null);
							} else if (valueWithoutSpace.length() != 0) {
								chronometerValueInSecond = global
										.chronometerValueConversionInSeconds(valueWithoutSpace);
								field.setDefaultValue(chronometerValueInSecond + "");
							}
						}

						if (!verifyInternal(field)) {
							throw new BadRequestException("INTERNAL_INVALID");
						}

						if (field.getDefaultValue() != null && !field.getDefaultValue().equals("")) {
							isValidDefaultValue(field, companyId);
						}

						if (field.getDatatypes().getDisplay().equals("Picklist")) {
							if (field.getPicklistValues().size() < 1) {
								throw new BadRequestException("PICKLIST_VALUES_EMPTY");
							}
							List<String> picklistValues = field.getPicklistValues();
							Set<String> uniqueValues = new HashSet<String>(picklistValues);

							if (picklistValues.size() != uniqueValues.size()) {
								throw new BadRequestException("PICKLIST_VALUES_NOT_UNIQUE");
							}
						}

						if (field.getDatatypes().getDisplay().equals("Picklist (Multi-Select)")) {
							if (field.getPicklistValues().size() < 1) {
								throw new BadRequestException("PICKLIST_VALUES_EMPTY");
							}
						}

						if (field.getDatatypes().getDisplay().equalsIgnoreCase("Relationship")) {
							if (field.getRelationshipType().equalsIgnoreCase("Many To One")
									&& field.getInheritanceMapping() != null) {
								// TODO: implement checkValidInheritanceMap logic
							}
							if (field.getDataFilter() != null && field.getModule() != null
									&& field.getRelationshipField() != null) {
								Document dataFilter = Document
										.parse(new ObjectMapper().writeValueAsString(field.getDataFilter()));

								collection.updateOne(Filters.eq("_id", new ObjectId(field.getModule())),
										Updates.set("FIELDS.$[field].DATA_FILTER", dataFilter),
										new UpdateOptions().arrayFilters(Arrays
												.asList(Filters.eq("field.RELATIONSHIP_FIELD", field.getFieldId()))));
							}
						}
						field.setDataFilter(null);
						String fieldBody = new ObjectMapper().writeValueAsString(field).toString();
						Document fieldDocument = Document.parse(fieldBody);

						collection.updateOne(Filters.eq("NAME", moduleName),
								Updates.pull("FIELDS", Filters.eq("FIELD_ID", field.getFieldId())));
						collection.updateOne(Filters.eq("NAME", moduleName), Updates.push("FIELDS", fieldDocument));

						log.trace("Exit FieldService.putFields() moduleName: " + moduleName + ", fieldName: "
								+ fieldName);
						return field;
					} else {
						throw new ForbiddenException("FIELD_NOT_EXISTS");
					}
				} else {
					throw new ForbiddenException("MODULE_NOT_EXISTS");
				}
			} else {
				throw new BadRequestException("FIELD_ID_NULL");
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

//	@DeleteMapping("/modules/{module_id}/fields/{field_name}")
//	public ResponseEntity<Object> deleteFields(HttpServletRequest request,
//			@RequestParam("authentication_token") String uuid, @PathVariable("field_name") String fieldName,
//			@PathVariable("module_id") String moduleId) {
//		try {
//			log.trace("Enter FieldService.deleteFields() moduleId: " + moduleId + ", fieldName: " + fieldName);
//			JSONObject user = auth.getUserDetails(uuid);
//			String userId = user.getString("USER_ID");
//			String companyId = user.getString("COMPANY_ID");
//			String collectionName = "modules_" + companyId;
//			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
//			if (!ObjectId.isValid(moduleId)) {
//				throw new BadRequestException("INVALID_MODULE_ID");
//			}
//			Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
//
//			if (module != null) {
//
//				String moduleName = module.getString("NAME");
//				if (!roleService.isAuthorizedForModule(userId, "DELETE", moduleId, companyId)) {
//					throw new ForbiddenException("FORBIDDEN");
//				}
//				
//				List<Document> fields = (List<Document>) module.get("FIELDS");
//				
//				Document fieldToBeDeleted = null;
//				
//				for(Document field: fields) {
//					
//					String name = field.getString("NAME");
//					
//					if(name.equals(fieldName)) {
//						fieldToBeDeleted = field;
//						break;
//					}
//				}
//				
//				if (fieldToBeDeleted != null) {
//					
//					if(fieldToBeDeleted.getBoolean("INTERNAL")) {
//						throw new BadRequestException("INTERNAL_INVALID");
//					}
//
//					collection.updateOne(Filters.eq("NAME", moduleName),
//							Updates.pull("FIELDS", Filters.eq("NAME", fieldName)));
//					log.trace(
//							"Exit FieldService.deleteFields() moduleName: " + moduleName + ", fieldName: " + fieldName);
//					return new ResponseEntity<Object>(HttpStatus.OK);
//				} else {
//					throw new ForbiddenException("FIELD_NOT_EXISTS");
//				}
//			} else {
//				throw new ForbiddenException("MODULE_NOT_EXISTS");
//			}
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//	}

	public boolean isFieldDiscussion(Document module, Field field, String collectionName) {
		try {
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			if (field.getDatatypes().getDisplay().equals("Discussion")) {

				if (collection
						.find(Filters.and(Filters.eq("_id", new ObjectId(module.get("_id").toString())),
								Filters.eq("FIELDS.DATA_TYPE.DISPLAY", field.getDatatypes().getDisplay())))
						.first() != null) {
					return false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}
		return true;
	}

	public boolean verifyInternal(Field field) {
		if (field.isInternal()) {
			return false;
		}
		return true;
	}

	private void updateRoles(String moduleId, String fieldId, String companyId) {
		try {

			MongoCollection<Document> collection = mongoTemplate.getCollection("roles_" + companyId);

			FieldPermission permission = new FieldPermission();
			permission.setFieldId(fieldId);
			permission.setPermission("Not Set");

			String permissionJson = new ObjectMapper().writeValueAsString(permission);
			Document document = Document.parse(permissionJson);

			collection.updateMany(
					Filters.and(Filters.ne("NAME", "SystemAdmin"),
							Filters.elemMatch("PERMISSIONS", Filters.eq("MODULE", moduleId))),
					Updates.addToSet("PERMISSIONS.$.FIELD_PERMISSIONS", document));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void createIndexForTheField(String companyId, String moduleId, Field field) {
		try {
			String displayDataType = field.getDatatypes().getDisplay();

			if (!(displayDataType.equalsIgnoreCase("Picklist (Multi-Select)")
					|| displayDataType.equalsIgnoreCase("Discussion") || displayDataType.equalsIgnoreCase("Phone")
					|| displayDataType.equalsIgnoreCase("Text Area")
					|| displayDataType.equalsIgnoreCase("Text Area Long")
					|| displayDataType.equalsIgnoreCase("Text Area Rich"))) {
				MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
				Document module = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
				String moduleName = module.getString("NAME");
				String collectionName = moduleName.replaceAll("\\s+", "_") + "_" + companyId;
				MongoCollection<Document> entriesCollection = mongoTemplate.getCollection(collectionName);

				if (displayDataType.equalsIgnoreCase("Relationship")) {

					if (!(field.getRelationshipType().equalsIgnoreCase("Many to Many")
							|| field.getRelationshipType().equalsIgnoreCase("One to Many"))) {

						// INDEX TO MONGO
						entriesCollection.createIndex(Indexes.ascending(field.getName()),
								new IndexOptions().background(true));
					}
				} else {

					// INDEX TO MONGO
					entriesCollection.createIndex(Indexes.ascending(field.getName()),
							new IndexOptions().background(true));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean isValidDefaultValue(Field field, String companyId) {

		try {
			log.trace("Enter FieldService.isValidDefaultValue()");
			String fieldName = field.getName();

			MongoCollection<Document> collection = mongoTemplate.getCollection("modules_" + companyId);
			String dataType = field.getDatatypes().getDisplay();

			if (dataType.equals("Email")) {
				String value = field.getDefaultValue();
				if (!EmailValidator.getInstance().isValid(value)) {
					throw new BadRequestException("EMAIL_INVALID");
				}
			} else if (dataType.equals("Number")) {
				try {
					int value = Integer.parseInt(field.getDefaultValue());
				} catch (NumberFormatException e) {
					e.printStackTrace();
					throw new BadRequestException("INVALID_DEFAULT_FIELD");
				}
			} else if (dataType.equals("Percent")) {
				Double value = Double.parseDouble(field.getDefaultValue());
			} else if (dataType.equals("Currency")) {
				Double value = Double.parseDouble(field.getDefaultValue());
				if (BigDecimal.valueOf(value).scale() > 3) {
					throw new BadRequestException("INVALID_NUMBER");
				}
			} else if (dataType.equals("Phone")) {
				JSONObject phoneObject = new JSONObject(field.getDefaultValue());
				if (!phoneObject.getString("DIAL_CODE").equalsIgnoreCase("")
						&& !phoneObject.getString("PHONE_NUMBER").equalsIgnoreCase("")) {
					String phoneNumberE164Format = phoneObject.getString("DIAL_CODE")
							+ phoneObject.getString("PHONE_NUMBER");
					PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
					try {
						PhoneNumber phoneNumberProto = phoneUtil.parse(phoneNumberE164Format, null);
						boolean isValid = phoneUtil.isValidNumber(phoneNumberProto);
						if (!isValid) {
							throw new BadRequestException("PHONE_NUMBER_LENGHT_INVALID");
						}
					} catch (NumberParseException e) {
						throw new BadRequestException("PHONE_NUMBER_LENGHT_INVALID");
					}
				} else if (field.isRequired()) {
					throw new BadRequestException("PHONE_NUMBER_LENGHT_INVALID");
				}

			} else if (dataType.equals("Checkbox")) {
				Boolean value = Boolean.parseBoolean(field.getDefaultValue());
			} else if (dataType.equals("URL")) {
				String[] schemes = { "http", "https" }; // DEFAULT schemes = "http", "https", "ftp"
				UrlValidator urlValidator = new UrlValidator(schemes);
				String value = field.getDefaultValue();
				if (!urlValidator.isValid(value)) {
					throw new BadRequestException("INVALID_URL");
				}
			} else if (dataType.equals("Date/Time") || dataType.equals("Date") || dataType.equals("Time")) {
				String value = field.getDefaultValue();
				if (global.verifyDateTime(value) == null) {
					throw new BadRequestException("INVALID_DATE_TIME");
				}
			} else if (dataType.equalsIgnoreCase("Picklist")) {
				List<String> picklistValues = field.getPicklistValues();
				String value = field.getDefaultValue();
				boolean valueExists = false;

				for (int i = 0; i < picklistValues.size(); i++) {
					if (picklistValues.get(i).equals(value)) {
						valueExists = true;
						break;
					}
				}

				if (!valueExists) {
					throw new BadRequestException("VALUE_MISSING_IN_PICKLIST");
				}

			} else if (dataType.equals("Relationship")) { // RELATIONSHIP VALIDATION NOT REQUIRED AS OF 05/31/2019
				String relationshipType = field.getRelationshipType();
				String relationModuleId = field.getModule();

				Document relationModule = collection.find(Filters.eq("_id", new ObjectId(relationModuleId))).first();

				if (relationModule == null) {
					throw new BadRequestException("INVALID_RELATIONSHIP");
				}

				String relationModuleName = relationModule.getString("NAME");
				String relationshipField = field.getRelationshipField();

				MongoCollection<Document> entriesCollection = mongoTemplate
						.getCollection(relationModuleName + "_" + companyId);

				if (relationshipType.equals("One to One") || relationshipType.equals("Many to One")) {
					if (field.isRequired()) {
						String value = field.getDefaultValue();
						if (!new ObjectId().isValid(value)) {
							throw new BadRequestException("INVALID_ENTRY_ID");
						}
						Document entry = entriesCollection.find(Filters.eq("_id", new ObjectId(value))).first();
						if (entry == null) {
							throw new BadRequestException("ENTRY_NOT_EXIST");
						}
					}
				} else if (relationshipType.equals("One to Many")) {
					if (field.getName() != null) {
						throw new BadRequestException("ONE_TO_MANY_ERROR");
					}
				} else if (relationshipType.equals("Many to Many")) {

					String value = field.getDefaultValue();
					List<String> validValues = new ArrayList<String>();
					if (!new ObjectId().isValid(value)) {
						throw new BadRequestException("INVALID_ENTRY_ID");
					}
					Document entry = entriesCollection.find(Filters.eq("_id", new ObjectId(value))).first();
					if (entry == null) {
						throw new BadRequestException("ENTRIES_NOT_EXIST");
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
			throw new InternalError("INTERNAL_ERROR");
		}

		log.trace("Exit FieldService.isValidDefaultValue() companyId: " + companyId);
		return true;
	}

	public void createFieldIfNotExists(String companyId, String moduleId, String fieldName, String displayLabel,
			String displayDataType, String backendDatatype, boolean required, boolean visibility, boolean notEditable,
			List<String> picklistValues) {
		try {
			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			Document moduleWithThisField = modulesCollection
					.find(Filters.elemMatch("FIELDS", Filters.eq("NAME", fieldName))).first();
			if (moduleWithThisField == null) {
				Document module = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
				List<Document> fields = (List<Document>) module.get("FIELDS");
				int size = 0;
				int totalDataTimeField = 0;
				for (Document doc : fields) {

					Document type = (Document) doc.get("DATA_TYPE");
					if (type.getString("DISPLAY").equals("Date/Time") || type.getString("DISPLAY").equals("Date")
							|| type.getString("DISPLAY").equals("Time")) {

						totalDataTimeField++;
					}
				}
				if (totalDataTimeField == 0 && !displayDataType.equals("Date/Time") && !displayDataType.equals("Date")
						&& !displayDataType.equals("Time")) {
					size = fields.size();

				} else if (totalDataTimeField == 0 && (displayDataType.equals("Date/Time")
						|| displayDataType.equals("Date") || !displayDataType.equals("Time"))) {
					size = 84;
				} else if (totalDataTimeField != 0 && !displayDataType.equals("Date/Time")
						&& !displayDataType.equals("Date") && !displayDataType.equals("Time")) {

					size = fields.size() - totalDataTimeField;
				} else if (totalDataTimeField != 0 && (displayDataType.equals("Date/Time")
						|| displayDataType.equals("Date") || displayDataType.equals("Time"))) {

					size = 84 + totalDataTimeField;
				}
				Field fieldObject = new Field();
				fieldObject.setName(fieldName);
				fieldObject.setDisplayLabel(displayLabel);
				DataType datatypeObject = new DataType();
				datatypeObject.setBackend(backendDatatype);
				datatypeObject.setDisplay(displayDataType);
				fieldObject.setDatatypes(datatypeObject);
				fieldObject.setRequired(required);
				fieldObject.setVisibility(visibility);
				fieldObject.setNotEditable(notEditable);
				fieldObject.setFieldId(UUID.randomUUID().toString());
				if ((displayDataType.equals("Picklist") || displayDataType.equals("Picklist (Multi-Select)"))
						&& !picklistValues.isEmpty()) {
					fieldObject.setPicklistValues(picklistValues);
				}
				String json = new ObjectMapper().writeValueAsString(fieldObject);
				modulesCollection.findOneAndUpdate(Filters.eq("_id", new ObjectId(moduleId)),
						Updates.addToSet("FIELDS", Document.parse(json)));
				wrapper.putMappingForNewField(companyId, moduleId, fieldName, size + 1);
				createIndexForTheField(companyId, moduleId, fieldObject);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// TODO: Add the code for validation and use it in Put Call for fields
	private boolean checkValidInheritanceMap(Map<String, String> inheritanceMap, List<Field> currentModuleFields,
			Field field) {
		try {

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
