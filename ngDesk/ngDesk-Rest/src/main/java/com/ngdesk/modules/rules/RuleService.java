package com.ngdesk.modules.rules;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.bson.BsonArray;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.ngdesk.Authentication;
import com.ngdesk.Global;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.ForbiddenException;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.roles.RoleService;

@RestController
@Component
public class RuleService {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Global global;

	@Autowired
	private Authentication auth;
	@Autowired
	private RoleService roleService;

	private final Logger log = LoggerFactory.getLogger(RuleService.class);

	@GetMapping("/modules/{module_id}/rules")
	public List<Rule> getRules(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId) {

		List<Rule> rules = new ArrayList<Rule>();

		try {
			log.trace("Enter RuleService.getRules()  moduleId: " + moduleId);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String companyId = user.getString("COMPANY_ID");
			String collectionName = "modules_" + companyId;

			// Retrieving a collection
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			// Get Document
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

			ArrayList<Document> ruleDocuments = (ArrayList) module.get("FIELD_RULES");

			for (Document document : ruleDocuments) {
				Rule rule = new ObjectMapper().readValue(document.toJson(), Rule.class);
				rules.add(rule);
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
		log.trace("Exit RuleService.getRules() moduleId: " + moduleId);
		return rules;
	}

	@PutMapping("/modules/{module_id}/rules")
	public List<Rule> putRules(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId, @Valid @RequestBody FieldRules rules) {

		try {
			log.trace("Enter RuleService.putRules() moduleId: " + moduleId);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String companyId = user.getString("COMPANY_ID");

			String payload = new ObjectMapper().writeValueAsString(rules.getRules()).toString();
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
			if (!roleService.isAuthorizedForModule(userId, "PUT", moduleId, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			if (isUniqueRuleName(rules.getRules())) {
				if (isValidConditions(rules.getRules(), collectionName, moduleName)) {
					if (isUniqueOperator(rules.getRules(), moduleName, companyId, collectionName)) {
						if (isValidActions(rules.getRules(), companyId, moduleName)) {
							BsonArray ruleDocument = BsonArray.parse(payload);
							collection.updateOne(Filters.eq("NAME", moduleName),
									Updates.set("FIELD_RULES", ruleDocument));
							log.trace("Exit RuleService.putRules()  moduleName: " + moduleName);
							return rules.getRules();
						} else {
							throw new BadRequestException("INVALID_ACTION");
						}
					} else {
						throw new BadRequestException("INVALID_CONDITION");
					}
				} else {
					throw new BadRequestException("INVALID_CONDITION");
				}
			} else {
				throw new BadRequestException("RULE_NAME_EXISTS");
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

	private boolean isValidActions(List<Rule> rules, String companyId, String moduleName) {

		try {
			log.trace("Enter RuleService.isValidActions() moduleName: " + moduleName + ", companyId: " + companyId);
			MongoCollection<Document> collection = mongoTemplate.getCollection("modules_" + companyId);

			for (Rule rule : rules) {

				List<Action> actions = rule.getActions();

				for (Action action : actions) {

					String fieldId = action.getField();

					Document module = collection.find(Filters.and(Filters.eq("NAME", moduleName),
							Filters.elemMatch("FIELDS", Filters.eq("FIELD_ID", fieldId)))).first();

					if (module == null) {
						return false;
					}

				}

			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}
		log.trace("Exit RuleService.isValidActions() moduleName: " + moduleName + ", companyId: " + companyId);
		return true;
	}

	public boolean isValidConditions(List<Rule> rules, String collectionName, String moduleName) {
		try {
			log.trace("Enter RuleService.isValidConditions() moduleName: " + moduleName + ", collectionName: "
					+ collectionName);
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			Document module = collection.find(Filters.eq("NAME", moduleName)).first();
			ArrayList<Document> fieldDocuments = (ArrayList) module.get("FIELDS");
			ArrayList<String> fields = new ArrayList<String>();
			for (Document document : fieldDocuments) {
				fields.add(document.getString("FIELD_ID"));
			}
			for (Rule rule : rules) {
				for (Condition condition : rule.getConditions()) {
					String fieldId = condition.getCondition();
					if (!fields.contains(fieldId)) {
						return false;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}
		log.trace("Exit RuleService.isValidConditions() moduleName: " + moduleName + ", collectionName: "
				+ collectionName);
		return true;
	}

	public boolean isUniqueOperator(List<Rule> rules, String moduleName, String companyId,
			String moduleCollectionName) {

		try {
			log.trace("Enter RuleService.isUniqueOperator() moduleName: " + moduleName + ", companyId: " + companyId);
			String collectionName = moduleName.replaceAll("\\s+", "_") + "_" + companyId;
			// Retrieving a collection
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			MongoCollection<Document> modulecollection = mongoTemplate.getCollection(moduleCollectionName);
			Document module = modulecollection.find(Filters.eq("NAME", moduleName)).first();
			ArrayList<Document> fieldDocuments = (ArrayList) module.get("FIELDS");

			for (Rule rule : rules) {
				for (Condition condition : rule.getConditions()) {
					if (condition.getOpearator().equals("is unique")) {
						for (Document fieldDoc : fieldDocuments) {
							if (fieldDoc.getString("FIELD_ID").equals(condition.getCondition())) {
								String name = fieldDoc.getString("NAME");
								String value = condition.getConditionValue();
								ArrayList<Document> documents = collection.find(Filters.eq(name, value))
										.into(new ArrayList<>());
								if (documents.size() > 1) {
									return false;
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new BadRequestException("INTERNAL_ERROR");
		}
		log.trace("Exit RuleService.isUniqueOperator() moduleName: " + moduleName + ", companyId: " + companyId);
		return true;
	}

	public boolean isUniqueRuleName(List<Rule> rules) {
		log.trace("Enter RuleService.isUniqueRuleName()");
		List<String> Rulenames = new ArrayList<String>();

		for (Rule rule : rules) {
			if (Rulenames.contains(rule.getName())) {
				return false;
			} else {
				Rulenames.add(rule.getName());
			}
		}
		log.trace("Exit RuleService.isUniqueRuleName()");
		return true;
	}

}
