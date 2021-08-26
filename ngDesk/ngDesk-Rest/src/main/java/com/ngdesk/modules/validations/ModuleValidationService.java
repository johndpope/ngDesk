package com.ngdesk.modules.validations;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
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
import com.mongodb.Function;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import com.mongodb.util.JSON;
import com.ngdesk.Authentication;
import com.ngdesk.Global;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.ForbiddenException;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.modules.rules.Condition;
import com.ngdesk.roles.RoleService;

@RestController
@Component
public class ModuleValidationService {
	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Authentication auth;

	@Autowired
	private Global global;

	@Autowired
	private RoleService roleService;

	private final Logger log = LoggerFactory.getLogger(ModuleValidationService.class);

	@GetMapping("/modules/{module_id}/validations")
	public ResponseEntity<Object> getValidations(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId,
			@RequestParam(value = "page_size", required = false) String pageSize,
			@RequestParam(value = "page", required = false) String page,
			@RequestParam(value = "sort", required = false) String sort,
			@RequestParam(value = "order", required = false) String order) {

		try {
			log.trace("Enter ModuleValidationService.getValidations() moduleId: " + moduleId);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");
			String collectionName = "modules_" + companyId;

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
			String moduleName = module.getString("NAME");

			if (module != null) {
				int totalSize = 0;
				ArrayList<Document> validationDocuments = (ArrayList<Document>) module.get("VALIDATIONS");
				totalSize = validationDocuments.size();
				AggregateIterable<Document> sortedDocuments = null;
				List<String> validationNames = new ArrayList<String>();
				validationNames.add("VALIDATION_ID");
				validationNames.add("NAME");
				validationNames.add("TYPE");
				validationNames.add("DESCRIPTION");
				validationNames.add("ROLES");
				validationNames.add("VALIDATIONS");

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
					sort = "VALIDATIONS." + sort;
					if (order.equalsIgnoreCase("asc")) {
						sortedDocuments = collection
								.aggregate(
										Arrays.asList(Aggregates.unwind("$VALIDATIONS"),
												Aggregates.match(Filters.eq("NAME", moduleName)),
												Aggregates.sort(Sorts
														.orderBy(Sorts.ascending(sort))),
												Aggregates.project(Filters.and(
														Projections.computed("VALIDATIONS",
																Projections.include(validationNames)),
														Projections.excludeId())),
												Aggregates.skip(skip), Aggregates.limit(pgSize)));
					} else if (order.equalsIgnoreCase("desc")) {
						sortedDocuments = collection
								.aggregate(
										Arrays.asList(Aggregates.unwind("$VALIDATIONS"),
												Aggregates.match(Filters.eq("NAME", moduleName)),
												Aggregates.sort(Sorts
														.orderBy(Sorts.descending(sort))),
												Aggregates.project(Filters.and(
														Projections.computed("VALIDATIONS",
																Projections.include(validationNames)),
														Projections.excludeId())),
												Aggregates.skip(skip), Aggregates.limit(pgSize)));
					} else {
						throw new BadRequestException("INVALID_SORT_ORDER");
					}
				} else {
					sortedDocuments = collection
							.aggregate(
									Arrays.asList(
											Aggregates.unwind("$VALIDATIONS"), Aggregates.match(Filters.eq("NAME",
													moduleName)),
											Aggregates.project(Filters.and(
													Projections.computed("VALIDATIONS",
															Projections.include(validationNames)),
													Projections.excludeId())),
											Aggregates.skip(skip), Aggregates.limit(pgSize)));
				}

				JSONArray validations = new JSONArray();
				for (Document document : sortedDocuments) {
					Document data = (Document) document.get("VALIDATIONS");
					validations.put(data);

				}
				JSONObject resultObj = new JSONObject();
				resultObj.put("VALIDATIONS", validations);
				resultObj.put("TOTAL_RECORDS", totalSize);

				log.trace("Exit ModuleValidationService.getValidations() moduleId: " + moduleId);
				return new ResponseEntity<>(resultObj.toString(), Global.postHeaders, HttpStatus.OK);

			} else {
				throw new ForbiddenException("MODULE_NOT_EXISTS");
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");

	}

	@GetMapping("/modules/{module_id}/validations/{validation_id}")
	public ModuleValidation getValidationById(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId, @PathVariable("validation_id") String validationId) {
		try {
			log.trace("Enter ModuleValidationService.getValidationById() moduleId: " + moduleId);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");
			String collectionName = "modules_" + companyId;

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}

			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}
			Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
			if (module == null) {
				throw new BadRequestException("MODULE_NOT_EXISTS");
			}
			List<Document> validations = (List<Document>) module.get("VALIDATIONS");
			for (Document validation : validations) {
				if (validation.getString("VALIDATION_ID").equalsIgnoreCase(validationId)) {
					ObjectMapper mapper = new ObjectMapper();
					ModuleValidation validationObject = mapper.readValue(mapper.writeValueAsString(validation),
							ModuleValidation.class);
					log.trace("Enter ModuleValidationService.getValidationById() moduleId: " + moduleId);
					return validationObject;
				}
			}
			throw new BadRequestException("VALIDATION_NOT_EXISTS");

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

	@PostMapping("/modules/{module_id}/validations")
	public ModuleValidation postValidations(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId, @Valid @RequestBody ModuleValidation validations) {
		try {
			log.trace("Enter ModuleValidationService.postValidations() moduleId: " + moduleId);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");
			String userRole = user.getString("ROLE");
			String userId = user.getString("USER_ID");
			String collectionName = "modules_" + companyId;

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}
			Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
			if (module != null) {
				String moduleName = module.getString("NAME");
				List<Document> fields = (List<Document>) module.get("FIELDS");
				List<String> chronometerFieldIds = new ArrayList<String>();
				for (Document field : fields) {
					Document datatype = (Document) field.get("DATA_TYPE");
					if (datatype.getString("DISPLAY").equals("Chronometer")) {
						chronometerFieldIds.add(field.getString("FIELD_ID"));
					}
					// CHECK VALIDITY FOR CONDITION PICKLIST VALUE
					for (Validation validation : validations.getValidations()) {
						if (!(validation.getOperator().equalsIgnoreCase("EXISTS")
								|| validation.getOperator().equalsIgnoreCase("DOES_NOT_EXIST"))) {
							if (datatype.getString("DISPLAY").equalsIgnoreCase("Picklist")
									&& validation.getCondition().equals(field.get("FIELD_ID"))) {
								List<String> picklistValues = (List<String>) field.get("PICKLIST_VALUES");
								if (!picklistValues.contains(validation.getConditionValue())) {
									throw new BadRequestException("CONDITION_PICKLIST_VALUE_INVALID");
								}
							}
						}

					}
				}

				if (!chronometerFieldIds.isEmpty()) {
					for (Validation validation : validations.getValidations()) {
						String condition = validation.getCondition();
						if (chronometerFieldIds.contains(condition)) {
							String value = validation.getConditionValue();
							String conditionValue = global.chronometerValueConversionInSeconds(value) + "";
							validation.setConditionValue(conditionValue);
						}
					}
				}

				Document existingMatch = collection
						.find(Filters.and(Filters.elemMatch("VALIDATIONS", Filters.eq("NAME", validations.getName())),
								Filters.eq("NAME", moduleName)))
						.first();

				if (existingMatch != null) {
					throw new BadRequestException("VALIDATION_EXISTS");
				}

				MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
				for (String roleId : validations.getRoles()) {
					if (!ObjectId.isValid(roleId)) {
						throw new BadRequestException("INVALID_ROLE_ID");
					}
					Document roleDocument = rolesCollection.find(Filters.eq("_id", new ObjectId(roleId))).first();
					if (roleDocument == null) {
						throw new BadRequestException("INVALID_ROLE_ID");
					}
				}
				if (!isValidConditions(validations.getValidations(), collectionName, moduleName)) {
					throw new BadRequestException("INVALID_CONDITION");
				}
				if (!checkValidOperator(validations.getValidations(), module)) {
					throw new BadRequestException("INVALID_OPERATOR");
				}
				if (!checkValidRelationshipValue(validations.getValidations(), module, companyId)) {
					throw new BadRequestException("INVALID_VALUE");
				}

				validations.setValidationId(UUID.randomUUID().toString());
				if (isUniqueOperator(validations.getValidations(), moduleName, companyId, collectionName)) {
					validations.setCreatedBy(userId);
					validations.setLastUpdatedBy(userId);
					String json = new ObjectMapper().writeValueAsString(validations);
					Document validationDoc = Document.parse(json);
					validationDoc.put("DATE_CREATED", new Date());
					collection.updateOne(Filters.eq("NAME", moduleName),
							Updates.addToSet("VALIDATIONS", validationDoc));
					log.trace("Exit ModuleValidationService.postValidations() moduleId: " + moduleId);
					return validations;
				} else {
					throw new BadRequestException("INVALID_CONDITION");
				}
			} else {
				throw new ForbiddenException("MODULE_NOT_EXISTS");
			}

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PutMapping("/modules/{module_id}/validations")
	public ModuleValidation putValidations(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId, @Valid @RequestBody ModuleValidation validations) {
		try {

			log.trace("Enter ModuleValidationService.putValidations() moduleId: " + moduleId);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");
			String userRole = user.getString("ROLE");
			String userId = user.getString("USER_ID");

			String collectionName = "modules_" + companyId;
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
			if (module == null) {
				throw new ForbiddenException("MODULE_NOT_EXISTS");
			}
			if (validations.getValidationId() == null) {
				throw new BadRequestException("VALIDATION_ID_MISSING");
			}
			String moduleName = module.getString("NAME");
			List<Document> fields = (List<Document>) module.get("FIELDS");
			List<String> chronometerFieldIds = new ArrayList<String>();
			for (Document field : fields) {
				Document datatype = (Document) field.get("DATA_TYPE");
				if (datatype.getString("DISPLAY").equals("Chronometer")) {
					chronometerFieldIds.add(field.getString("FIELD_ID"));
				}

				// CHECK VALIDITY FOR CONDITION PICKLIST VALUE
				for (Validation validation : validations.getValidations()) {
					if (!(validation.getOperator().equalsIgnoreCase("EXISTS")
							|| validation.getOperator().equalsIgnoreCase("DOES_NOT_EXIST"))) {

						if (datatype.getString("DISPLAY").equalsIgnoreCase("Picklist")
								&& validation.getCondition().equals(field.get("FIELD_ID"))) {
							List<String> picklistValues = (List<String>) field.get("PICKLIST_VALUES");
							if (!picklistValues.contains(validation.getConditionValue())) {
								throw new BadRequestException("CONDITION_PICKLIST_VALUE_INVALID");
							}
						}
					}
				}
			}

			Document existingMatch = collection.find(Filters.and(
					Filters.elemMatch("VALIDATIONS", Filters.eq("VALIDATION_ID", validations.getValidationId())),
					Filters.eq("NAME", moduleName))).first();

			if (existingMatch == null) {
				throw new BadRequestException("VALIDATION_NOT_EXISTS");
			}

			if (!chronometerFieldIds.isEmpty()) {
				for (Validation validation : validations.getValidations()) {
					String condition = validation.getCondition();
					if (chronometerFieldIds.contains(condition)) {
						String value = validation.getConditionValue();
						String conditionValue = global.chronometerValueConversionInSeconds(value) + "";
						validation.setConditionValue(conditionValue);
					}
				}
			}

			Date dateCreated = null;
			String createdBy = "";
			List<Document> existingValidationArray = (List<Document>) existingMatch.get("VALIDATIONS");
			for (Document existingValidation : existingValidationArray) {
				if (existingValidation.getString("VALIDATION_ID").equalsIgnoreCase(validations.getValidationId())) {
					dateCreated = existingValidation.getDate("DATE_CREATED");
					createdBy = existingValidation.getString("CREATED_BY");
					break;
				}
			}

			// check uniqueness of name
			Document isUnique = collection
					.find(Filters.and(
							Filters.elemMatch("VALIDATIONS",
									Filters.and(Filters.ne("VALIDATION_ID", validations.getValidationId()),
											Filters.eq("NAME", validations.getName()))),
							Filters.eq("NAME", moduleName)))
					.first();
			if (isUnique != null) {
				throw new BadRequestException("VALIDATION_EXISTS");
			}

			MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
			for (String roleId : validations.getRoles()) {
				if (!ObjectId.isValid(roleId)) {
					throw new BadRequestException("INVALID_ROLE_ID");
				}
				Document roleDocument = rolesCollection.find(Filters.eq("_id", new ObjectId(roleId))).first();
				if (roleDocument == null) {
					throw new BadRequestException("INVALID_ROLE_ID");
				}
			}

			if (!isValidConditions(validations.getValidations(), collectionName, moduleName)) {
				throw new BadRequestException("INVALID_CONDITION");
			}
			if (!checkValidOperator(validations.getValidations(), module)) {
				throw new BadRequestException("INVALID_OPERATOR");
			}
			if (!checkValidRelationshipValue(validations.getValidations(), module, companyId)) {
				throw new BadRequestException("INVALID_VALUE");
			}

			if (isUniqueOperator(validations.getValidations(), moduleName, companyId, collectionName)) {
				validations.setDateUpdated(new Timestamp(new Date().getTime()));
				validations.setLastUpdatedBy(userId);
				String payload = new ObjectMapper().writeValueAsString(validations).toString();
				collection.findOneAndUpdate(Filters.eq("NAME", moduleName),
						Updates.pull("VALIDATIONS", Filters.eq("VALIDATION_ID", validations.getValidationId())));

				Document json = Document.parse(payload);
				json.put("DATE_CREATED", dateCreated);
				json.put("DATE_UPDATED", new Date());
				json.put("CREATED_BY", createdBy);
				collection.updateOne(Filters.eq("NAME", moduleName), Updates.addToSet("VALIDATIONS", json));

				log.trace("Exit ModuleValidationService.putValidations()  moduleName: " + moduleName);
				return validations;
			} else {
				throw new BadRequestException("INVALID_CONDITION");
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@DeleteMapping("/modules/{module_id}/validations/{validation_id}")
	public ResponseEntity<Document> deleteValidation(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId, @PathVariable("validation_id") String validationId) {
		try {

			log.trace("Enter ModuleValidationService.deleteValidation() moduleId: " + moduleId);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");
			String userRole = user.getString("ROLE");

			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			String collectionName = "modules_" + companyId;
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
			if (module == null) {
				throw new ForbiddenException("MODULE_NOT_EXISTS");
			}

			Document existingValidation = collection
					.find(Filters.elemMatch("VALIDATIONS", Filters.eq("VALIDATION_ID", validationId))).first();

			if (existingValidation == null) {
				throw new BadRequestException("VALIDATION_NOT_EXISTS");
			}

			String moduleName = module.getString("NAME");
			collection.findOneAndUpdate(Filters.eq("NAME", moduleName),
					Updates.pull("VALIDATIONS", Filters.eq("VALIDATION_ID", validationId)));
			log.trace("Exit ModuleValidationService.deleteValidation() moduleId: " + moduleId);
			return new ResponseEntity<>(HttpStatus.OK);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	public JSONArray sortValidations(JSONArray validations, String sort, String order) throws JSONException {
		log.trace("Enter ModuleValidationService.sortValidations()  sort: " + sort + ", order: " + order);
		JSONArray sortedValidations = new JSONArray();

		List<JSONObject> jsonValues = new ArrayList<JSONObject>();
		for (int i = 0; i < validations.length(); i++) {
			jsonValues.add(validations.getJSONObject(i));
		}

		JSONObject validation = validations.getJSONObject(0);
		List<String> validKeys = new ArrayList<String>();
		for (String key : validation.keySet()) {
			validKeys.add(key);
			validKeys.add("DISPLAY");
		}
		if (!validKeys.contains(sort)) {
			throw new BadRequestException("INVALID_SORT");
		}
		String keyName = sort;
		Collections.sort(jsonValues, new Comparator<JSONObject>() {

			@Override
			public int compare(JSONObject a, JSONObject b) {
				String valA = new String();
				String valB = new String();

				try {
					if (keyName.equals("DISPLAY")) {
						JSONObject a1 = (JSONObject) a.get("NAME");
						valA = (String) a1.get("DISPLAY");
						JSONObject a2 = (JSONObject) b.get("NAME");
						valB = (String) a2.get("DISPLAY");
					} else {
						valA = (String) a.get(keyName);
						valB = (String) b.get(keyName);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				if (order.equals("desc")) {
					return valB.compareToIgnoreCase(valA);
				}
				return valA.compareToIgnoreCase(valB);
			}
		});

		for (int i = 0; i < validations.length(); i++) {
			sortedValidations.put(jsonValues.get(i));
		}
		log.trace("Exit ModuleValidationService.sortValidations()  sort: " + sort + ", order: " + order);
		return sortedValidations;
	}

	public boolean isValidConditions(List<Validation> validations, String collectionName, String moduleName) {
		try {
			log.trace("Enter ModuleValidationService.isValidConditions() moduleName: " + moduleName
					+ ", collectionName:" + collectionName);
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			Document module = collection.find(Filters.eq("NAME", moduleName)).first();
			ArrayList<Document> fieldDocuments = (ArrayList) module.get("FIELDS");
			ArrayList<String> fields = new ArrayList<String>();
			Hashtable<String, String> fieldsToDataTypeMap = new Hashtable<String, String>();
			for (Document document : fieldDocuments) {
				fields.add(document.getString("FIELD_ID"));
				Document dataType = (Document) document.get("DATA_TYPE");
				fieldsToDataTypeMap.put(document.getString("FIELD_ID"), dataType.getString("DISPLAY"));
			}
			for (Validation validation : validations) {
				String field = validation.getCondition();
				if (!fields.contains(field)) {
					return false;
				}
			}
			log.trace("Exit ModuleValidationService.isValidConditions() moduleName: " + moduleName + ", collectionName:"
					+ collectionName);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			throw new BadRequestException("INTERNAL_ERROR");
		}

	}

	public boolean isUniqueOperator(List<Validation> validations, String moduleName, String companyId,
			String moduleCollectionName) {

		try {
			log.trace("Enter ModuleValidationService.isUniqueOperator() moduleName: " + moduleName + ", companyId:"
					+ companyId);
			String collectionName = moduleName.replaceAll("\\s+", "_") + "_" + companyId;
			// Retrieving a collection
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			MongoCollection<Document> modulecollection = mongoTemplate.getCollection(moduleCollectionName);
			Document module = modulecollection.find(Filters.eq("NAME", moduleName)).first();
			ArrayList<Document> fieldDocuments = (ArrayList) module.get("FIELDS");

			for (Validation validation : validations) {
				if (validation.getOperator().equals("IS_UNIQUE")) {
					for (Document fieldDoc : fieldDocuments) {
						if (fieldDoc.getString("FIELD_ID").equals(validation.getCondition())) {
							String name = fieldDoc.getString("NAME");
							String value = validation.getConditionValue();
							ArrayList<Document> documents = collection.find(Filters.eq(name, value))
									.into(new ArrayList<>());
							if (documents.size() > 1) {
								return false;
							}
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new BadRequestException("INTERNAL_ERROR");
		}
		log.trace("Exit ModuleValidationService.isUniqueOperator() moduleName: " + moduleName + ", companyId:"
				+ companyId);
		return true;
	}

	public boolean checkValidOperator(List<Validation> validations, Document module) {
		try {
			List<Document> fields = (List<Document>) module.get("FIELDS");
			List<String> relationshipManyToMany = new ArrayList<String>();
			Hashtable<String, String> fieldsToDataTypeMap = new Hashtable<String, String>();
			for (Document field : fields) {
				String fieldId = field.getString("FIELD_ID");
				Document dataType = (Document) field.get("DATA_TYPE");
				if (field.containsKey("RELATIONSHIP_TYPE") && field.get("RELATIONSHIP_TYPE") != null
						&& field.getString("RELATIONSHIP_TYPE").equalsIgnoreCase("Many to Many")) {
					relationshipManyToMany.add(fieldId);
				}
				fieldsToDataTypeMap.put(fieldId, dataType.getString("DISPLAY"));
			}

			for (Validation validation : validations) {

				String conditionFieldId = validation.getCondition();
				String conditionOperator = validation.getOperator();
				String dataTypeOfConditionField = fieldsToDataTypeMap.get(conditionFieldId);
				if (validation.getOperator().equalsIgnoreCase("IS_UNIQUE")
						&& dataTypeOfConditionField.equalsIgnoreCase("DISCUSSION")) {
					return false;
				}
				if (dataTypeOfConditionField.equalsIgnoreCase("Number")) {
					if (!global.validNumericOperators.contains(conditionOperator)) {
						return false;
					}
				} else if (dataTypeOfConditionField.equalsIgnoreCase("Date/Time")
						|| dataTypeOfConditionField.equalsIgnoreCase("Date")
						|| dataTypeOfConditionField.equalsIgnoreCase("Time")) {
					if (!global.validDateOperators.contains(conditionOperator)) {
						return false;
					}
				} else if (dataTypeOfConditionField.equalsIgnoreCase("Checkbox")
						|| (dataTypeOfConditionField.equals("Relationship")
								&& !relationshipManyToMany.contains(conditionFieldId))) {
					if (!global.validRelationOperators.contains(conditionOperator)) {
						return false;
					}
				} else if (relationshipManyToMany.contains(conditionFieldId)
						|| dataTypeOfConditionField.equalsIgnoreCase("List Text")) {
					if (conditionOperator.equals("EQUALS_TO") || conditionOperator.equals("NOT_EQUALS_TO")) {
						return false;
					}
				}
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return true;
	}

	public boolean checkValidRelationshipValue(List<Validation> validations, Document module, String companyId) {
		try {
			List<Document> fields = (List<Document>) module.get("FIELDS");

			for (Validation validation : validations) {

				String conditionFieldId = validation.getCondition();
				String conditionOperator = validation.getOperator();
				String conditionValue = validation.getConditionValue();
				for (Document field : fields) {
					String fieldId = field.getString("FIELD_ID");
					Document dataType = (Document) field.get("DATA_TYPE");
					if (validation.getCondition().equals(fieldId)) {
						if (dataType.getString("DISPLAY").equalsIgnoreCase("Relationship")) {
							// To get the collection based on company id.
							MongoCollection<Document> modulecollection = mongoTemplate
									.getCollection("modules_" + companyId);
							String moduleId = field.getString("MODULE");
							// To get which modules the field belongs to
							Document moduleDocument = modulecollection.find(Filters.eq("_id", new ObjectId(moduleId)))
									.first();
							String moduleName = moduleDocument.getString("NAME");
							boolean isValid = false;
							// To check if the field belongs to user module or not
							if (moduleName.equalsIgnoreCase("Users")) {
								Pattern pattern = Pattern.compile("\\{\\{(.*)\\}\\}");
								Matcher matcher = pattern.matcher(conditionValue);
								if (matcher.find()) {
									if (matcher.group(1).equals("CURRENT_USER")) {
										isValid = true;
									}
								}
							}

							if (!isValid) {
								if (!ObjectId.isValid(conditionValue)) {
									throw new BadRequestException("NOT_VALID_RELATIONSHIP_VALUE");
								}

								MongoCollection<Document> relationshipCollection = mongoTemplate
										.getCollection("modules_" + companyId);

								Document relationshipModule = relationshipCollection
										.find(Filters.eq("_id", new ObjectId(field.getString("MODULE")))).first();
								MongoCollection<Document> collection = mongoTemplate
										.getCollection(relationshipModule.getString("NAME") + "_" + companyId);
								Document entry = collection.find(Filters.eq("_id", new ObjectId(conditionValue)))
										.first();
								if (entry == null) {
									throw new BadRequestException("NOT_VALID_RELATIONSHIP_VALUE");
								}
							}
						}
					}
				}
			}
			return true;

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return true;
	}

}
