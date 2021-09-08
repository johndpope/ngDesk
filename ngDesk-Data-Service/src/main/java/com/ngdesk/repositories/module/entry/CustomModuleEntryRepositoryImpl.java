package com.ngdesk.repositories.module.entry;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.http.util.Asserts;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.AccumulatorOperators;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperationContext;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.Field;
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.SkipOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.data.dao.DiscussionMessage;
import com.ngdesk.data.dao.Sender;
import com.ngdesk.data.modules.dao.Condition;
import com.ngdesk.data.modules.dao.Module;
import com.ngdesk.data.modules.dao.ModuleField;
import com.ngdesk.data.modules.dao.ModuleService;
import com.ngdesk.data.roles.dao.RolesService;

@Repository
public class CustomModuleEntryRepositoryImpl implements CustomModuleEntryRepository {

	@Autowired
	private MongoOperations mongoOperations;

	@Autowired
	private ModuleService moduleService;

	@Autowired
	private AuthManager authManager;

	@Autowired
	private RolesService rolesService;

	@Override
	public Optional<Map<String, Object>> findEntryById(String entryId, String collectionName) {
		Assert.notNull(entryId, "The given id must not be null!");
		Assert.notNull(collectionName, "The given collectionName must not be null!");
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("_id").is(entryId),
				Criteria.where("EFFECTIVE_TO").is(null));
		Query query = new Query(criteria);
		query.fields().exclude("PASSWORD");
		return Optional.ofNullable(mongoOperations.findOne(query, Map.class, collectionName));
	}

	@Override
	public int getCountOfCollectionForAutonumber(String collectionName) {
		Assert.notNull(collectionName, "The given collectionName must not be null!");
		return (int) mongoOperations.count(new Query(Criteria.where("EFFECTIVE_TO").is(null)), collectionName);
	}

	public int getNextAutoNumber(String fieldName, String collectionName) {
		Assert.notNull(collectionName, "The given collectionName must not be null!");
		Assert.notNull(fieldName, "The given fieldName must not be null!");

		try {
			Query query = new Query(Criteria.where("EFFECTIVE_TO").is(null))
					.with(Sort.by(Sort.Direction.DESC, fieldName));
			Optional<Map<String, Object>> optionalEntry = Optional
					.ofNullable(mongoOperations.findOne(query, Map.class, collectionName));

			if (optionalEntry.isPresent()) {
				Map<String, Object> entry = optionalEntry.get();
				if (entry.get(fieldName) != null) {
					return ((int) entry.get(fieldName)) + 1;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 1;
	}

	@Override
	public Optional<Map<String, Object>> findUniqueEntryForPost(String variable, Object value, String collectionName) {
		Assert.notNull(variable, "The given variable must not be null!");
		Assert.notNull(value, "The given value must not be null!");
		Assert.notNull(collectionName, "The given collectionName must not be null!");

		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where(variable).is(value),
				Criteria.where("EFFECTIVE_TO").is(null));

		Query query = new Query(criteria);
		return Optional.ofNullable(mongoOperations.findOne(query, Map.class, collectionName));
	}

	@Override
	public Optional<Map<String, Object>> findUniqueEntryForPut(String variable, Object value, String collectionName,
			String dataId) {
		Assert.notNull(variable, "The given variable must not be null!");
		Assert.notNull(value, "The given value must not be null!");
		Assert.notNull(collectionName, "The given collectionName must not be null!");
		Assert.notNull(variable, "The given dataId must not be null!");

		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where(variable).is(value),
				Criteria.where("EFFECTIVE_TO").is(null), Criteria.where("_id").ne(dataId));

		Query query = new Query(criteria);
		return Optional.ofNullable(mongoOperations.findOne(query, Map.class, collectionName));
	}

	@Override
	public Optional<Map<String, Object>> updateEntry(Map<String, Object> entry, String collectionName) {
		Assert.notNull(entry, "Entry must not be null");
		Assert.notNull(entry.get("_id"), "Data ID must not be null");
		Query query = new Query();
		query.addCriteria(Criteria.where("_id").is(entry.get("_id").toString()));

		return Optional.ofNullable(mongoOperations.findAndReplace(query, entry, collectionName));
	}

	@Override
	public Optional<List<Map<String, Object>>> findAllEntriesByFieldName(List<String> values, String fieldName,
			String collectionName) {
		Assert.notNull(values, "Values must not be null");
		Assert.notNull(fieldName, "Field name must not be null");
		Assert.notNull(collectionName, "Collection name must not be null");

		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where(fieldName).in(values),
				Criteria.where("EFFECTIVE_TO").is(null));

		Query query = new Query(criteria);
		return Optional.ofNullable(
				mongoOperations.find(query, (Class<Map<String, Object>>) (Class) Map.class, collectionName));
	}

	public Optional<List<Map<String, Object>>> findAllDefaultEntriesByModuleId(String moduleId, String collectionName) {
		Query query = new Query(Criteria.where("MODULE_ID").is(moduleId));
		return Optional.ofNullable(
				mongoOperations.find(query, (Class<Map<String, Object>>) (Class) Map.class, collectionName));
	}

	@Override
	public long getSystemAdminCount(String systemAdminRoleId, String collectionName) {
		Assert.notNull(systemAdminRoleId, "System admin role Id must not be null");
		Assert.notNull(collectionName, "Collection name must not be null");
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("ROLE").is(systemAdminRoleId),
				Criteria.where("EFFECTIVE_TO").is(null));
		Query query = new Query(criteria);

		return mongoOperations.count(query, collectionName);
	}

	@Override
	public Optional<Map<String, Object>> getAggregationValues(Aggregation aggregation, String collectionName) {
		Assert.notNull(aggregation, "Aggregation must not be null");
		Assert.notNull(collectionName, "Collection name must not be null");

		AggregationResults<Map> aggregateResult = mongoOperations.aggregate(aggregation, collectionName, Map.class);
		return Optional.ofNullable(aggregateResult.getUniqueMappedResult());
	}

	private Page<Map<String, Object>> findAllWithAggregation(Pageable pageable, List<AggregationOperation> operation,
			String collectionName, long count) {

		Asserts.notNull(pageable, "Pageable must not be null");
		Asserts.notNull(operation, "Operation must not be null");
		Asserts.notNull(collectionName, "Collection Name must not be null");

		operation = addPaginationParameters(pageable, operation);
		operation.add(Aggregation.project().andExclude("_id", "PASSWORD", "META_DATA"));

		Aggregation aggregation = Aggregation.newAggregation(operation);

		List<Map<String, Object>> listEntries = mongoOperations
				.aggregate(aggregation, collectionName, (Class<Map<String, Object>>) (Class) Map.class)
				.getMappedResults();

		return new PageImpl<Map<String, Object>>(listEntries, pageable, count);
	}

	public Page<Map<String, Object>> findAllWithCondition(Pageable pageable, List<Condition> conditions,
			Module currentModule, List<AggregationOperation> operations) {

		Assert.notNull(pageable, "Page parameters must not be null");
		Asserts.notNull(conditions, "Conditions must not be null");

		String companyId = authManager.getUserDetails().getCompanyId();

		List<ModuleField> fields = moduleService.getAllFields(currentModule, companyId);
		String collectionName = currentModule.getName().replaceAll("\\s+", "_") + "_" + companyId;

		Criteria finalCriteria = buildConditions(conditions, fields);
		AggregationOperation match = Aggregation.match(finalCriteria);
		operations.add(match);

		List<AggregationOperation> countOperation = new ArrayList<AggregationOperation>();
		countOperation.add(match);
		long count = getCount(countOperation, collectionName);

		operations = buildAggregationForAggregate(operations, currentModule);
		operations = buildAggregationForRelationship(currentModule, operations);
		operations = buildAggregationForManyToMany(currentModule, operations);
		operations = buildAggregationForWorkflowExecution(operations);

		return findAllWithAggregation(pageable, operations, collectionName, count);
	}

	private long getCount(List<AggregationOperation> operation, String collectionName) {

		List<AggregationOperation> countOperation = new ArrayList<AggregationOperation>();
		countOperation.addAll(operation);
		countOperation.add(Aggregation.group().count().as("count"));
		// COUNT AGGREGATION
		Aggregation countAggregation = Aggregation.newAggregation(countOperation);

		AggregationResults<Map<String, Object>> countResults = mongoOperations.aggregate(countAggregation,
				collectionName, (Class<Map<String, Object>>) (Class) Map.class);

		long count = 0;
		if (countResults.getMappedResults().size() > 0) {
			count = Long.parseLong(countResults.getMappedResults().get(0).get("count").toString());
		}

		return count;
	}

	@Override
	public Optional<Map<String, Object>> findEntryWithAggregation(Module currentModule, String entryId) {

		List<AggregationOperation> operation = new ArrayList<AggregationOperation>();
		String collectionName = moduleService.getCollectionName(currentModule.getName(),
				authManager.getUserDetails().getCompanyId());

		operation.add(Aggregation.match(new Criteria().andOperator(Criteria.where("_id").is(entryId),
				Criteria.where("DELETED").is(false), Criteria.where("EFFECTIVE_TO").is(null))));

		operation = buildInheritanceAggregation(currentModule, operation);

		Aggregation aggregation = Aggregation.newAggregation(operation);

		return getAggregationValues(aggregation, collectionName);
	}

	@Override
	public Optional<Map<String, Object>> findEntryWithAggregationRelationship(Module currentModule, String entryId) {

		List<AggregationOperation> operations = new ArrayList<AggregationOperation>();
		String collectionName = moduleService.getCollectionName(currentModule.getName(),
				authManager.getUserDetails().getCompanyId());

		operations.add(Aggregation.match(new Criteria().andOperator(Criteria.where("_id").is(entryId),
				Criteria.where("DELETED").is(false), Criteria.where("EFFECTIVE_TO").is(null))));

		operations = buildInheritanceAggregation(currentModule, operations);

		operations = buildAggregationForAggregate(operations, currentModule);
		operations = buildAggregationForRelationship(currentModule, operations);
		operations = buildAggregationForManyToMany(currentModule, operations);
		operations = buildAggregationForWorkflowExecution(operations);

		Aggregation aggregation = Aggregation.newAggregation(operations);
		return getAggregationValues(aggregation, collectionName);
	}

	@Override
	public Page<Map<String, Object>> findAllPaginatedEntriesByIds(Pageable pageable, Module currentModule,
			List<String> entryIds) {
		List<AggregationOperation> operation = new ArrayList<AggregationOperation>();

		operation.add(Aggregation.match(new Criteria().andOperator(Criteria.where("_id").in(entryIds),
				Criteria.where("DELETED").is(false), Criteria.where("EFFECTIVE_TO").is(null))));

		String collectionName = moduleService.getCollectionName(currentModule.getName(),
				authManager.getUserDetails().getCompanyId());

		long count = getCount(operation, collectionName);

		operation = addSortParameter(operation, pageable);

		operation = buildInheritanceAggregation(currentModule, operation);

		operation = buildAggregationForAggregate(operation, currentModule);
		operation = buildAggregationForRelationship(currentModule, operation);
		operation = buildAggregationForManyToMany(currentModule, operation);
		operation = buildAggregationForWorkflowExecution(operation);

		return findAllWithAggregation(pageable, operation, collectionName, count);
	}

	@Override
	public Page<Map<String, Object>> findAllPaginatedEntries(Pageable pageable, Module currentModule) {
		List<AggregationOperation> operation = new ArrayList<AggregationOperation>();

		String collectionName = moduleService.getCollectionName(currentModule.getName(),
				authManager.getUserDetails().getCompanyId());
		operation.add(Aggregation.match(new Criteria().andOperator(Criteria.where("DELETED").is(false),
				Criteria.where("EFFECTIVE_TO").is(null))));

		long count = getCount(operation, collectionName);

		operation = addSortParameter(operation, pageable);

		operation = buildInheritanceAggregation(currentModule, operation);

		operation = buildAggregationForAggregate(operation, currentModule);
		operation = buildAggregationForRelationship(currentModule, operation);
		operation = buildAggregationForManyToMany(currentModule, operation);
		operation = buildAggregationForWorkflowExecution(operation);

		return findAllWithAggregation(pageable, operation, collectionName, count);
	}

	public Page<Map<String, Object>> findAllEntriesWithCondition(Pageable pageable, Module currentModule,
			List<Condition> conditions, List<String> entryIdsFromSearch) {
		List<AggregationOperation> operation = new ArrayList<AggregationOperation>();

		operation = addSortParameter(operation, pageable);

		operation = addSearchParameter(operation, entryIdsFromSearch);

		operation = buildInheritanceAggregation(currentModule, operation);

		return findAllWithCondition(pageable, conditions, currentModule, operation);
	}

	@Override
	public Page<Map<String, Object>> findAllListLayoutEntries(Pageable pageable, Module currentModule,
			List<Condition> conditions, List<String> fieldIds) {

		List<AggregationOperation> operations = new ArrayList<AggregationOperation>();
		List<ModuleField> fields = moduleService.getAllFields(currentModule,
				authManager.getUserDetails().getCompanyId());

		List<String> teamIds = getAllTeamsOfCurrentUser();

		if (!rolesService.isSystemAdmin(authManager.getUserDetails().getRole())
				&& !currentModule.getName().equalsIgnoreCase("Teams")) {
			operations.add(Aggregation.match(Criteria.where("TEAMS").in(teamIds)));
		}

		operations = addSortParameter(operations, pageable);
		operations = buildInheritanceAggregation(currentModule, operations);

		return findAllWithCondition(pageable, conditions, currentModule, operations);

	}

	private List<AggregationOperation> addSortParameter(List<AggregationOperation> operation, Pageable pageable) {
		if (pageable.getSort().isSorted()) {
			operation.add(Aggregation.sort(pageable.getSort()));
		}
		return operation;
	}

	private List<Criteria> buildCriterias(String requirementType, List<Condition> conditions,
			List<ModuleField> fields) {

		Asserts.notNull(requirementType, "Requirement type must not be null");
		Asserts.notNull(conditions, "Conditions must not be null");
		Asserts.notNull(fields, "Fields must not be null");

		String userId = authManager.getUserDetails().getUserId();

		List<Condition> filteredCondition = conditions.stream()
				.filter(condition -> condition.getRequirementType().equals(requirementType))
				.collect(Collectors.toList());
		List<Criteria> criterias = new ArrayList<Criteria>();

		for (Condition condition : filteredCondition) {
			if (condition.getRequirementType().equalsIgnoreCase(requirementType)) {
				String fieldId = condition.getCondition();
				String value = condition.getConditionValue();

				String reg = "\\{\\{(.*)\\}\\}";
				Pattern r1 = Pattern.compile(reg);
				Matcher m1 = r1.matcher(value);
				ModuleField conditionField = fields.stream().filter(field -> field.getFieldId().equals(fieldId))
						.findFirst().get();

				String displayDatatype = conditionField.getDataType().getDisplay();
				String backendDatatype = conditionField.getDataType().getBackend();
				boolean isString = false;
				boolean isInteger = false;
				boolean isBoolean = false;

				String fieldName = conditionField.getName();

				if (conditionField.getInheritedField() != null) {
					if (conditionField.getInheritedField()) {
						fieldName = conditionField.getInheritanceLevel() + "." + conditionField.getName();
					}
				}
				if (m1.find()) {
					value = authManager.getUserDetails().getAttributes().get("CONTACT").toString();
				}

				if (displayDatatype.equals("Number") || displayDatatype.equals("Auto Number")
						|| displayDatatype.equals("Chronometer") || displayDatatype.equals("Formula")) {
					isInteger = true;
				}
				if (backendDatatype.equalsIgnoreCase("Boolean")) {
					isBoolean = true;
				}
				if (backendDatatype.equalsIgnoreCase("String") || displayDatatype.equals("Text")) {
					isString = true;
				}

				switch (condition.getOpearator()) {
				case "EQUALS_TO":
					if (isInteger) {
						criterias.add(Criteria.where(fieldName).is(Integer.parseInt(value)));
					} else if (isBoolean) {
						criterias.add(Criteria.where(fieldName).is(Boolean.parseBoolean(value)));
					} else {
						criterias.add(Criteria.where(fieldName).is(value));
					}
					break;
				case "NOT_EQUALS_TO":
					if (isInteger) {
						criterias.add(Criteria.where(fieldName).ne(Integer.parseInt(value)));
					} else if (isBoolean) {
						criterias.add(Criteria.where(fieldName).ne(Boolean.parseBoolean(value)));
					} else {
						criterias.add(Criteria.where(fieldName).ne(value));
					}
					break;
				case "GREATER_THAN":
					criterias.add(Criteria.where(fieldName).gt(value));
					break;
				case "LENGTH_IS_GREATER_THAN":
					if (isString) {
						criterias.add(Criteria.where(fieldName).regex("^.{" + value + ",}$"));
					}
					break;
				case "LENGTH_IS_LESS_THAN":
					if (isString) {
						criterias.add(Criteria.where(fieldName).regex("^.{0," + value + "}$"));
					}
					break;
				case "LESS_THAN":
					criterias.add(Criteria.where(fieldName).lt(value));
					break;
				case "CONTAINS":
					criterias.add(Criteria.where(fieldName).regex(Pattern.compile(".*" + value + ".*")));
					break;
				case "DOES_NOT_CONTAIN":
					criterias.add(Criteria.where(fieldName).not().regex(Pattern.compile(".*" + value + ".*")));
					break;
				case "REGEX":
					criterias.add(Criteria.where(fieldName).regex(value));
					break;
				case "EXISTS":
					criterias.add(Criteria.where(fieldName).exists(true));
					break;
				case "DOES_NOT_EXIST":
					criterias.add(Criteria.where(fieldName).exists(false));
					break;
				}
			}
		}
		return criterias;
	}

	private AggregationOperation convertStringToOjectId(String fieldName) {

		Asserts.notNull(fieldName, "Field Name must not be null");

		AggregationOperation operation = new AggregationOperation() {

			@Override
			public Document toDocument(AggregationOperationContext context) {

				return new Document("$addFields",
						new Document(fieldName + "_ID", new Document("$toObjectId", "$" + fieldName)));
			}
		};

		return operation;
	}

	private AggregationOperation buildLookupOperation(String parentCollectionName, String fieldInChild,
			String fieldInParent, String outputName) {

		Asserts.notNull(parentCollectionName, "Parent collection name must not be null");
		Asserts.notNull(fieldInChild, "Field in child module must not be null");
		Asserts.notNull(fieldInParent, "Field in parent module must not be null");
		Asserts.notNull(outputName, "Output Name must not be null");

		AggregationOperation lookup = LookupOperation.newLookup().from(parentCollectionName)
				.localField(fieldInChild + "_ID").foreignField(fieldInParent).as(outputName);

		return lookup;
	}

	private AggregationOperation convertObjectIdToString(String fieldName, String fieldToAdd) {
		AggregationOperation operation = new AggregationOperation() {

			@Override
			public Document toDocument(AggregationOperationContext context) {
				return new Document("$addFields", new Document(fieldToAdd, new Document("$toString", "$" + fieldName)));
			}
		};
		return operation;
	}

	private List<AggregationOperation> buildInheritanceAggregation(Module currentModule,
			List<AggregationOperation> operation) {

		Asserts.notNull(currentModule, "Current Module must not be null");
		Asserts.notNull(operation, "Operation must not be null");

		String companyId = authManager.getUserDetails().getCompanyId();
		List<Module> moduleFamily = moduleService.getModuleFamily(currentModule.getModuleId(), companyId);

		AggregationOperation match = Aggregation.match(new Criteria().andOperator(Criteria.where("DELETED").is(false),
				Criteria.where("EFFECTIVE_TO").is(null)));

		operation.add(match);

		List<Module> filteredModules = moduleFamily.stream()
				.filter(module -> !module.getModuleId().equals(currentModule.getModuleId()))
				.collect(Collectors.toList());

		for (int i = filteredModules.size() - 1; i >= 0; i--) {
			Module module = filteredModules.get(i);
			String parentCollectionName = module.getName().replaceAll("\\s+", "_") + "_" + companyId;
			String fieldInChild = module.getSingularName().toUpperCase().replaceAll("\\s+", "_");
			String fieldInParent = "_id";
			String outputName = "output" + i;

			operation.add(convertStringToOjectId(fieldInChild));
			if (filteredModules.size() - 1 == i) {
				fieldInChild = module.getSingularName().toUpperCase().replaceAll("\\s+", "_");
			} else {
				fieldInChild = "output" + (i + 1) + "." + module.getName().toUpperCase().replaceAll("\\s+", "_");
				operation.add(convertStringToOjectId(fieldInChild));
			}

			operation.add(buildLookupOperation(parentCollectionName, fieldInChild, fieldInParent, outputName));
			operation.add(Aggregation.unwind(outputName));

			operation.add(
					Aggregation.match(new Criteria().andOperator(Criteria.where(outputName + "." + "DELETED").is(false),
							Criteria.where(outputName + "." + "EFFECTIVE_TO").is(null))));
		}

		List<String> projectionFields = buildProjection(moduleService.getAllFields(currentModule, companyId));
		AggregationOperation projection = Aggregation
				.project(projectionFields.toArray(new String[projectionFields.size()])).andExclude("_id");
		operation.add(convertObjectIdToString("_id", "DATA_ID"));
		operation.add(projection);

		return operation;
	}

	private List<String> buildProjection(List<ModuleField> fields) {

		Asserts.notNull(fields, "Fields must not be null");

		List<String> projectionFields = new ArrayList<String>();

		String[] ignored = { "DATE_CREATED", "DATE_UPDATED", "EFFECTIVE_FROM", "CREATED_BY", "LAST_UPDATED_BY",
				"DELETED", "DATA_ID", "SOURCE_TYPE", "CHANNEL", "TEAMS", "EFFECTIVE_TO", "WORKFLOW_STAGES" };

		List<String> ignoredFields = Arrays.asList(ignored);

		for (ModuleField field : fields) {

			if (field.getInheritedField() != null) {
				if (field.getInheritedField() && !ignoredFields.contains(field.getName())) {
					projectionFields.add(field.getInheritanceLevel() + "." + field.getName());
				}
			} else {
				projectionFields.add(field.getName());
			}
		}
		return projectionFields;
	}

	private List<AggregationOperation> addPaginationParameters(Pageable pageable,
			List<AggregationOperation> operation) {

		Asserts.notNull(pageable, "Pageable must not be null");
		Asserts.notNull(operation, "Operation must not be null");

		AggregationOperation skip = new SkipOperation((pageable.getPageNumber()) * pageable.getPageSize());
		operation.add(skip);
		operation.add(Aggregation.limit(pageable.getPageSize()));
		return operation;
	}

	private List<String> getAllTeamsOfCurrentUser() {

		String userCollectionName = "Users_" + authManager.getUserDetails().getCompanyId();
		String teamsCollectionName = "Teams_" + authManager.getUserDetails().getCompanyId();

		List<String> userIds = new ArrayList<String>();
		List<String> teamIds = new ArrayList<String>();

		userIds.add(authManager.getUserDetails().getUserId());

		Optional<List<Map<String, Object>>> optionalUsers = findAllEntriesByFieldName(
				Arrays.asList(authManager.getUserDetails().getUserId()), "REPORTS_TO", userCollectionName);

		if (!optionalUsers.isEmpty()) {
			List<Map<String, Object>> users = optionalUsers.get();
			users.forEach(user -> userIds.add(user.get("_id").toString()));
		}

		Optional<List<Map<String, Object>>> optionalTeams = findAllEntriesByFieldName(userIds, "USERS",
				teamsCollectionName);

		if (optionalTeams.isPresent()) {
			List<Map<String, Object>> teams = optionalTeams.get();
			teams.forEach(team -> teamIds.add(team.get("_id").toString()));
		}

		return teamIds;
	}

	@Override
	public List<Map<String, Object>> findTeamsByIds(List<String> teamIds, String collectionName) {
		Assert.notNull(teamIds, "The given teamIds must not be null!");
		Assert.notNull(collectionName, "The given collectionName must not be null!");

		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("_id").in(teamIds),
				Criteria.where("EFFECTIVE_TO").is(null));

		return mongoOperations.find(new Query(criteria), (Class<Map<String, Object>>) (Class) Map.class,
				collectionName);
	}

	@Override
	public Optional<Map<String, Object>> findEntryByFieldName(String fieldName, Object value, String collectionName) {
		Assert.notNull(value, "The given value must not be null!");
		Assert.notNull(collectionName, "The given collectionName must not be null!");

		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where(fieldName).is(value),
				Criteria.where("EFFECTIVE_TO").is(null));

		return Optional.ofNullable(mongoOperations.findOne(new Query(criteria), Map.class, collectionName));
	}

	@Override
	public Optional<Map<String, Object>> findEntryByAlternatePrimaryKeys(Map<String, Object> keyValuePairs,
			String collectionName) {

		Assert.notNull(keyValuePairs, "The given key value pairs must not be null!");
		Assert.notNull(collectionName, "The given collectionName must not be null!");
		Assert.isTrue(keyValuePairs.size() > 0, "There must be at least one alternate primary key");

		List<Criteria> criterias = new ArrayList<Criteria>();
		for (String key : keyValuePairs.keySet()) {
			criterias.add(Criteria.where(key).is(keyValuePairs.get(key)));
		}
		Criteria criteria = new Criteria();
		criteria = criteria.andOperator(criterias.toArray(new Criteria[criterias.size()]));

		return Optional.ofNullable(mongoOperations.findOne(new Query(criteria), Map.class, collectionName));
	}

	@Override
	public Optional<List<Map<String, Object>>> findAllTeamsOfCurrentUser() {

		String collectionName = "Teams_" + authManager.getUserDetails().getCompanyId();
		String userId = authManager.getUserDetails().getUserId();

		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("EFFECTIVE_TO").is(null),
				Criteria.where("USERS").in(userId));

		AggregationOperation match = Aggregation.match(criteria);
		AggregationOperation project = Aggregation.project("_id");

		Aggregation aggregation = Aggregation.newAggregation(match, project);

		AggregationResults<Map<String, Object>> aggregationResults = mongoOperations.aggregate(aggregation,
				collectionName, (Class<Map<String, Object>>) (Class) Map.class);

		return Optional.ofNullable(aggregationResults.getMappedResults());
	}

	private List<AggregationOperation> addSearchParameter(List<AggregationOperation> operation,
			List<String> entryIdsFromSearch) {
		Asserts.notNull(operation, "Operation must not be null");
		Asserts.notNull(entryIdsFromSearch, "Entry Ids must not be null");

		if (entryIdsFromSearch.size() > 0) {
			operation.add(Aggregation.match(Criteria.where("_id").in(entryIdsFromSearch)));
		}

		return operation;
	}

	private List<AggregationOperation> buildAggregationForRelationship(Module module,
			List<AggregationOperation> operation) {

		List<ModuleField> fields = moduleService.getAllFields(module, authManager.getUserDetails().getCompanyId());
		List<String> projectionFields = buildProjection(
				moduleService.getAllFields(module, authManager.getUserDetails().getCompanyId()));

		List<ModuleField> relationshipFields = fields.stream()
				.filter(field -> field.getDataType().getDisplay().equalsIgnoreCase("Relationship")
						&& !field.getRelationshipType().equalsIgnoreCase("One To Many")
						&& !field.getRelationshipType().equalsIgnoreCase("Many To Many"))
				.collect(Collectors.toList());

		List<Field> newFieldsToInclude = new ArrayList<Field>();
		List<AggregationOperation> objectConversionOperations = new ArrayList<AggregationOperation>();
		List<String> relationshipFieldNames = new ArrayList<String>();

		for (ModuleField relationshipField : relationshipFields) {
			Module relationshipModule = moduleService.getRelationshipModule(relationshipField,
					authManager.getUserDetails().getCompanyId());
			String collectionName = moduleService.getCollectionName(relationshipModule.getName(),
					authManager.getUserDetails().getCompanyId());

			String primaryDisplayFieldName = getPrimaryDisplayFieldName(relationshipModule, relationshipField);

			AggregationOperation addField = convertStringToOjectId(relationshipField.getName());
			operation.add(addField);
			// LOOKUP RELATIONSHIP
			AggregationOperation lookupOperation = buildLookupOperation(collectionName, relationshipField.getName(),
					"_id", relationshipField.getName() + "_OBJ");
			operation.add(lookupOperation);

			operation.add(Aggregation.unwind(relationshipField.getName() + "_OBJ", true));

			// ADD NEW PRIMARY DISPLAY FIELD TO RELATIONSHIP FIELD
			newFieldsToInclude.add(Fields.field(relationshipField.getName() + ".PRIMARY_DISPLAY_FIELD",
					relationshipField.getName() + "_OBJ." + primaryDisplayFieldName));
			newFieldsToInclude.add(
					Fields.field(relationshipField.getName() + ".DATA_ID", relationshipField.getName() + ".DATA_ID"));

			// ADD TO OBJECT ID CONVERSION
			objectConversionOperations.add(convertObjectIdToString(relationshipField.getName() + "_OBJ._id",
					relationshipField.getName() + ".DATA_ID"));

			if (relationshipField.getInheritanceLevel() != null && !relationshipField.getInheritanceLevel().isBlank()) {
				relationshipFieldNames.add(relationshipField.getInheritanceLevel() + "." + relationshipField.getName());
			} else {
				relationshipFieldNames.add(relationshipField.getName());
			}

		}

		relationshipFieldNames.add("LAST_UPDATED_BY");
		relationshipFieldNames.add("CREATED_BY");

		// REMOVE RELATIONSHIP FIELDS FROM PROJECT AS WE ADD IT IN INCLUDE
		projectionFields.removeAll(relationshipFieldNames);

		// CONVERT _id TO STRING
		operation.addAll(objectConversionOperations);

		// ADD THE AGGREGATION TO OPERATIONS ARRAY
		operation.add(Aggregation.project(projectionFields.toArray(new String[projectionFields.size()]))
				.andInclude(Fields.from(newFieldsToInclude.toArray(new Field[newFieldsToInclude.size()]))));

		return operation;
	}

	private String getPrimaryDisplayFieldName(Module relationshipModule, ModuleField relationshipField) {
		List<ModuleField> allFields = relationshipModule.getFields();

		Optional<ModuleField> optionalPrimaryDisplayField = allFields.stream()
				.filter(field -> field.getFieldId().equals(relationshipField.getPrimaryDisplayField())).findAny();

		if (optionalPrimaryDisplayField.isEmpty()) {
			String[] vars = { relationshipModule.getName(), relationshipField.getName() };
			throw new BadRequestException("PRIMARY_DISPLAY_FIELD_NOT_FOUND", vars);
		}

		ModuleField primaryDisplayField = optionalPrimaryDisplayField.get();

		return primaryDisplayField.getName();
	}

	@Override
	public Page<Map<String, Object>> findAllRelationshipEntries(Pageable pageable, Module relationshipModule,
			ModuleField field, List<String> entryIds) {
		Asserts.notNull(pageable, "Pagination object must not be null");
		Asserts.notNull(relationshipModule, "Relationship module must not be null");
		Asserts.notNull(field, "Field must not be null");

		String collectionName = moduleService.getCollectionName(relationshipModule.getName(),
				authManager.getUserDetails().getCompanyId());

		List<Criteria> criterias = new ArrayList<Criteria>();

		List<Condition> conditions = getDataFilter(relationshipModule, field);
		List<ModuleField> allFields = moduleService.getAllFields(relationshipModule,
				authManager.getUserDetails().getCompanyId());

		criterias.add(buildConditions(conditions, allFields));

		if (relationshipModule.getName().equalsIgnoreCase("Users")) {
			criterias.add(restrictedUsersCriteria());
		} else if (relationshipModule.getName().equalsIgnoreCase("Teams")) {
			criterias.add(restrictedTeamsCriteria());
		}

		if (entryIds != null) {
			criterias.add(Criteria.where("_id").in(entryIds));
		}
		Criteria finalCriteria = new Criteria().andOperator(criterias.toArray(new Criteria[criterias.size()]));

		List<AggregationOperation> operation = new ArrayList<AggregationOperation>();
		operation.add(Aggregation.match(finalCriteria));

		long count = getCount(operation, collectionName);

		operation = buildInheritanceAggregation(relationshipModule, operation);

		String primaryDisplayFieldName = getPrimaryDisplayFieldName(relationshipModule, field);
		operation.add(Aggregation.project().andInclude(Fields.from(Fields.field("DATA_ID", "DATA_ID"),
				Fields.field("PRIMARY_DISPLAY_FIELD", primaryDisplayFieldName))));

		operation = addSortParameter(operation, pageable);

		return findAllWithAggregation(pageable, operation, collectionName, count);
	}

	private Criteria buildConditions(List<Condition> conditions, List<ModuleField> fields) {
		List<Criteria> allCriteria = buildCriterias("All", conditions, fields);
		List<Criteria> anyCriteria = buildCriterias("Any", conditions, fields);

		allCriteria.add(Criteria.where("DELETED").is(false));
		allCriteria.add(Criteria.where("EFFECTIVE_TO").is(null));

		Criteria finalCriteria = new Criteria();

		if (allCriteria.size() > 0 && anyCriteria.size() > 0) {
			finalCriteria.andOperator(allCriteria.toArray(new Criteria[allCriteria.size()]));
			finalCriteria.orOperator(anyCriteria.toArray(new Criteria[anyCriteria.size()]));
		} else if (allCriteria.size() > 0 && anyCriteria.size() == 0) {
			finalCriteria.andOperator(allCriteria.toArray(new Criteria[allCriteria.size()]));
		}
		return finalCriteria;
	}

	private List<Condition> getDataFilter(Module relationshipModule, ModuleField relationshipField) {
		List<ModuleField> allFields = relationshipModule.getFields();

		Optional<ModuleField> optionalRelationshipField = allFields.stream()
				.filter(field -> field.getFieldId().equals(relationshipField.getRelationshipField())).findAny();

		if (optionalRelationshipField.isEmpty()) {
			return new ArrayList<Condition>();
		}

		ModuleField relatedField = optionalRelationshipField.get();
		if (relatedField.getDataFilter() != null && relatedField.getDataFilter().getConditions() != null) {
			return relatedField.getDataFilter().getConditions();
		} else {
			return new ArrayList<Condition>();
		}

	}

	private Criteria restrictedUsersCriteria() {
		String[] emails = { "ghost@ngdesk.com", "system@ngdesk.com", "probe@ngdesk.com",
				"register_controller@ngdesk.com" };
		List<String> emailIds = Arrays.asList(emails);
		return Criteria.where("EMAIL_ADDRESS").not().in(emailIds);
	}

	private Criteria restrictedTeamsCriteria() {
		String[] teamNames = { "Ghost Team", "Public" };
		List<String> teams = Arrays.asList(teamNames);
		return Criteria.where("NAME").not().in(teams);
	}

	private AggregationOperation convertArrayToObject(String fieldName) {
		AggregationOperation operation = new AggregationOperation() {

			@Override
			public Document toDocument(AggregationOperationContext context) {
				Document doc = new Document();
				doc.put("input", "$" + fieldName);
				doc.put("in", new Document("$toObjectId", "$$this"));

				return new Document("$addFields", new Document(fieldName + "_ID", new Document("$map", doc)));
			}
		};
		return operation;
	}

	private AggregationOperation convertArrayObjectToString(String fieldName) {
		AggregationOperation operation = new AggregationOperation() {

			@Override
			public Document toDocument(AggregationOperationContext context) {
				Document doc = new Document();
				doc.put("input", "$" + fieldName + "_ID");
				doc.put("in", new Document("$toString", "$$this._id"));

				return new Document("$addFields", new Document(fieldName + "_1", new Document("$map", doc)));
			}
		};
		return operation;
	}

	private List<AggregationOperation> buildAggregationForManyToMany(Module module,
			List<AggregationOperation> operation) {

		List<ModuleField> fields = moduleService.getAllFields(module, authManager.getUserDetails().getCompanyId());

		List<ModuleField> relationshipFields = fields.stream()
				.filter(field -> field.getDataType().getDisplay().equalsIgnoreCase("Relationship")
						&& field.getRelationshipType().equalsIgnoreCase("Many To Many"))
				.collect(Collectors.toList());

		List<String> excludeFields = new ArrayList<String>();

		for (ModuleField relationshipField : relationshipFields) {
			Module relationshipModule = moduleService.getRelationshipModule(relationshipField,
					authManager.getUserDetails().getCompanyId());
			String collectionName = moduleService.getCollectionName(relationshipModule.getName(),
					authManager.getUserDetails().getCompanyId());
			String primaryDisplayFieldName = getPrimaryDisplayFieldName(relationshipModule, relationshipField);

			// CONVERT TEAMIDS TO OBJECT ID
			AggregationOperation addField = convertArrayToObject(relationshipField.getName());
			operation.add(addField);

			// LOOKUP USING OBJECTID
			AggregationOperation lookupOperation = buildLookupOperation(collectionName, relationshipField.getName(),
					"_id", relationshipField.getName() + "_OBJ");
			operation.add(lookupOperation);

			// CONVERT LOOKUPS TO OUR FORMAT
			// TODO: Revisit later and optimize
			operation.add(buildManyToManyOperation(relationshipField, primaryDisplayFieldName));

			excludeFields.add(relationshipField.getName() + "_ID");
			excludeFields.add(relationshipField.getName() + "_OBJ");
		}
		// THROWS ERROR IF THE SIZE IS 0
		if (excludeFields.size() > 0) {
			operation.add(Aggregation.project().andExclude(excludeFields.toArray(new String[excludeFields.size()])));
		}

		return operation;
	}

	private AggregationOperation buildManyToManyOperation(ModuleField relationshipField,
			String primaryDisplayFieldName) {
		String fieldName = relationshipField.getName();
		AggregationOperation op = new AggregationOperation() {

			@Override
			public Document toDocument(AggregationOperationContext context) {
				return Document.parse("{\"$addFields\":{\"" + fieldName + "\":{\"$map\":{\"input\":\"$" + fieldName
						+ "_OBJ\",\"as\":\"" + fieldName.toLowerCase() + "Obj\",\"in\":{\"DATA_ID\":{\"$toString\":\"$$"
						+ fieldName.toLowerCase() + "Obj._id\"},\"PRIMARY_DISPLAY_FIELD\":\"$$"
						+ fieldName.toLowerCase() + "Obj." + primaryDisplayFieldName + "\"}}}}}");
			}
		};
		return op;
	}

	private List<AggregationOperation> buildAggregationForWorkflowExecution(List<AggregationOperation> operation) {

		String collectionName = "workflows_in_execution";
		AggregationOperation lookupOperation = buildLookupOperation(collectionName, "DATA", "DATA_ID",
				"WORKFLOW_STAGES");
		operation.add(lookupOperation);
		// TODO: Remove _id from workflows in execution before returning, can use
		// projections
		return operation;
	}

	@Override
	public void findAndPull(String fieldName, String dataId, String collectionName) {
		Asserts.notNull(fieldName, "Field name must not be null");
		Asserts.notNull(dataId, "Data Id must not be null");
		Asserts.notNull(collectionName, "Collection Name must not be null");

		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where(fieldName).is(dataId), Criteria.where("DELETED").is(false),
				Criteria.where("EFFECTIVE_TO").is(null));
		Query query = new Query();
		query.addCriteria(criteria);

		Update update = new Update();
		update.pull(fieldName, dataId);

		mongoOperations.updateMulti(query, update, collectionName);
	}

	@Override
	public void findAndUnset(String fieldName, String dataId, String collectionName) {
		Asserts.notNull(fieldName, "Field name must not be null");
		Asserts.notNull(dataId, "Data Id must not be null");
		Asserts.notNull(collectionName, "Collection Name must not be null");

		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where(fieldName).is(dataId), Criteria.where("DELETED").is(false),
				Criteria.where("EFFECTIVE_TO").is(null));
		Query query = new Query();
		query.addCriteria(criteria);

		Update update = new Update();
		update.unset(fieldName);

		mongoOperations.updateMulti(query, update, collectionName);

	}

	@Override
	public void findAndUpdateMany(String fieldName, String value, String currentValue, String collectionName) {
		Asserts.notNull(fieldName, "Field name must not be null");
//		Asserts.notNull(value, "Value must not be null");
		Asserts.notNull(collectionName, "Collection Name must not be null");

		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where(fieldName).is(currentValue), Criteria.where("DELETED").is(false),
				Criteria.where("EFFECTIVE_TO").is(null));
		Query query = new Query();
		query.addCriteria(criteria);

		Update update = new Update();
		update.set(fieldName, value);

		mongoOperations.updateMulti(query, update, collectionName);
	}

	@Override
	public void findAndAddGhost(String fieldName, String userId, String ghostUserId, String collectionName) {
		Asserts.notNull(fieldName, "Field name must not be null");
		Asserts.notNull(userId, "UserId must not be null");
		Asserts.notNull(ghostUserId, "Ghost UserId must not be null");
		Asserts.notNull(collectionName, "Collection Name must not be null");

		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where(fieldName).is(userId), Criteria.where(fieldName + ".1").exists(false),
				Criteria.where("DELETED").is(false), Criteria.where("EFFECTIVE_TO").is(null));
		Query query = new Query();
		query.addCriteria(criteria);

		Update update = new Update();
		update.addToSet(fieldName, ghostUserId);

		mongoOperations.updateMulti(query, update, collectionName);
	}

	@Override
	public Optional<List<Map<String, Object>>> findAllTeamIdsToDelete(String fieldName, String deleteUserId,
			List<String> roleNamesToFilter, String collectionName) {

		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where(fieldName).in(deleteUserId), Criteria.where(fieldName + ".1").exists(false),
				Criteria.where("DELETED").is(false), Criteria.where("EFFECTIVE_TO").is(null));
		Query query = new Query(criteria);
		query.fields().include("_id");

		return Optional.ofNullable(
				mongoOperations.find(query, (Class<Map<String, Object>>) (Class) Map.class, collectionName));
	}

	@Override
	public void deleteTeamEntries(List<String> teamIds, String fieldName, String companyId) {
		Query query = new Query(Criteria.where("_id").in(teamIds));

		Update update = new Update();
		update.set("DELETED", true);
		update.set(fieldName, new ArrayList<String>());

		mongoOperations.updateMulti(query, update, "Teams_" + companyId);
	}

	@Override
	public void updateSenderInDiscussion(String fieldName, Sender sender, String userUUID, String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("EFECTIVE_TO").is(null));

		Query query = new Query(criteria);

		Update update = new Update();
		update.set(fieldName + ".$[element].SENDER", sender)
				.filterArray(Criteria.where("element.SENDER.USER_UUID").is(userUUID));

		mongoOperations.updateMulti(query, update, collectionName);
	}

	@Override
	public Optional<List<Map<String, Object>>> findChildEntryIds(String parentName, String parentId,
			String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where(parentName).in(parentId), Criteria.where("EFFECTIVE_TO").is(null),
				Criteria.where("DELETED").is(false));
		Query query = new Query(criteria);
		query.fields().include("_id");

		return Optional.ofNullable(
				mongoOperations.find(query, (Class<Map<String, Object>>) (Class) Map.class, collectionName));
	}

	private List<AggregationOperation> buildAggregationForAggregate(List<AggregationOperation> operation,
			Module module) {

		List<ModuleField> fields = moduleService.getAllFields(module, authManager.getUserDetails().getCompanyId());

		List<ModuleField> aggregateFields = fields.stream()
				.filter(field -> field.getDataType().getDisplay().equalsIgnoreCase("Aggregate"))
				.collect(Collectors.toList());

		List<ModuleField> oneToManyFields = fields.stream()
				.filter(field -> field.getDataType().getDisplay().equalsIgnoreCase("Relationship")
						&& field.getRelationshipType().equalsIgnoreCase("One To Many"))
				.collect(Collectors.toList());
		List<String> projections = buildProjection(fields);

		ProjectionOperation project = Aggregation.project(projections.toArray(new String[projections.size()]));
		for (ModuleField aggregateField : aggregateFields) {
			Optional<ModuleField> optionalField = oneToManyFields.stream()
					.filter(field -> field.getFieldId().equalsIgnoreCase(aggregateField.getAggregationField()))
					.findFirst();

			if (optionalField.isEmpty()) {
				continue;
			}

			ModuleField oneToManyField = optionalField.get();
			Module relatedModule = moduleService.getRelationshipModule(oneToManyField,
					authManager.getUserDetails().getCompanyId());

			Optional<ModuleField> optionalFieldToAggregate = relatedModule.getFields().stream()
					.filter(field -> field.getFieldId().equalsIgnoreCase(aggregateField.getAggregationRelatedField()))
					.findFirst();

			ModuleField fieldToAggregate = optionalFieldToAggregate.get();
			Optional<ModuleField> optionalRelationshipField = relatedModule.getFields().stream()
					.filter(field -> field.getFieldId().equalsIgnoreCase(oneToManyField.getRelationshipField()))
					.findFirst();

			ModuleField relationshipField = optionalRelationshipField.get();

			String field = fieldToAggregate.getName();
			String manyToOneFieldName = relationshipField.getName();

			String collectionName = moduleService.getCollectionName(relatedModule.getName(),
					authManager.getUserDetails().getCompanyId());

			// Lookup method for aggregation by pipeline.
			AggregationOperation lookupOperation = lookupForAggregation(collectionName, "DATA", manyToOneFieldName,
					aggregateField.getName() + "_OBJ");

			operation.add(lookupOperation);

			project = generateStatement(project, aggregateField.getAggregationType(), field, aggregateField.getName());
		}
		if (aggregateFields.size() > 0) {
			operation.add(project);
		}

		return operation;
	}

	private ProjectionOperation generateStatement(ProjectionOperation project, String aggregationType,
			String fieldToAggregate, String fieldName) {
		Asserts.notNull(aggregationType, "Aggregation Type must not be null");
		switch (aggregationType.toUpperCase()) {
		case "SUM":
			return project.and(AccumulatorOperators.Sum.sumOf(fieldName + "_OBJ." + fieldToAggregate)).as(fieldName);

		case "AVG":
			return project.and(AccumulatorOperators.Avg.avgOf(fieldName + "_OBJ." + fieldToAggregate)).as(fieldName);

		case "MAX":
			return project.and(AccumulatorOperators.Max.maxOf(fieldName + "_OBJ." + fieldToAggregate)).as(fieldName);

		case "MIN":
			return project.and(AccumulatorOperators.Min.minOf(fieldName + "_OBJ." + fieldToAggregate)).as(fieldName);

		default:
			return project.and(AccumulatorOperators.Sum.sumOf(fieldName + "_OBJ." + fieldToAggregate)).as(fieldName);
		}
	}

	@Override
	public Optional<List<Map<String, Object>>> findAllEntries(String collectionName) {
		Assert.notNull(collectionName, "Collection name must not ne null");
		return Optional.ofNullable(
				mongoOperations.find(new Query(), (Class<Map<String, Object>>) (Class) Map.class, collectionName));
	}

	@Override
	public void updateSoftwareNotFound(String collectionName) {
		LocalDateTime date = LocalDateTime.now().minusDays(1);
		LocalDateTime date90DaysAgo = LocalDateTime.now().minusDays(90);
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false),
				Criteria.where("DATE_UPDATED").lte(date).gt(date90DaysAgo), Criteria.where("EFFECTIVE_TO").is(null));
		Query query = new Query(criteria);
		Update update = new Update();
		update.set("STATUS", "Not Found");
		mongoOperations.updateMulti(query, update, collectionName);
	}

	@Override
	public void updateSoftwareUninstalled(String collectionName) {
		LocalDateTime date = LocalDateTime.now().minusDays(90);
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("DATE_UPDATED").lte(date),
				Criteria.where("EFFECTIVE_TO").is(null));
		Query query = new Query(criteria);
		Update update = new Update();
		update.set("STATUS", "Uninstalled");
		mongoOperations.updateMulti(query, update, collectionName);
	}

	@Override
	public long findCountOfEntries(String collectionName) {

		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("EFFECTIVE_TO").is(null));

		return mongoOperations.count(new Query(criteria), (Class<Map<String, Object>>) (Class) Map.class,
				collectionName);
	}

	@Override
	public List<Map<String, Object>> findAllEntries(String collectionName, Pageable pageable) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("EFFECTIVE_TO").is(null));
		return mongoOperations.find(new Query(criteria).with(pageable), (Class<Map<String, Object>>) (Class) Map.class,
				collectionName);
	}

	private AggregationOperation lookupForAggregation(String parentCollectionName, String fieldInChild,
			String fieldInParent, String outputName) {

		Asserts.notNull(parentCollectionName, "Parent collection name must not be null");
		Asserts.notNull(fieldInChild, "Field in child module must not be null");
		Asserts.notNull(fieldInParent, "Field in parent module must not be null");
		Asserts.notNull(outputName, "Output Name must not be null");

		// Add EFFECTIVE_TO: null if any mismatch in data.
		String query = "{$lookup: { from: \"" + parentCollectionName + "\", let: { data_id: \"$" + fieldInChild
				+ "_ID\"}, pipeline: [ { $match: { $and:[{ \"EFFECTIVE_TO\": { $exists: false }},{ $expr:{ $and: [{ $eq: [ \"$"
				+ fieldInParent + "\",  \"$$data_id\" ] }, { $eq: [ \"$DELETED\", false ] }]}}]}}],as: \"" + outputName
				+ "\"}}";

		AggregationOperation op = new AggregationOperation() {

			@Override
			public Document toDocument(AggregationOperationContext context) {
				return Document.parse(query);
			}
		};
		return op;
	}

	@Override
	public List<Map<String, Object>> findAllSoftwareModelsForDiscoveryMap(String collectionName, String discoveryMapId,
			String platform, String language) {
		Asserts.notNull(collectionName, "collection name must not be null");

		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DISCOVERY_MAP").is(discoveryMapId),
				Criteria.where("PLATFORM").is(platform));
		criteria.orOperator(Criteria.where("LANGUAGE").is("Anything"), Criteria.where("LANGUAGE").is(language));

		return mongoOperations.find(new Query(criteria), (Class<Map<String, Object>>) (Class) Map.class,
				collectionName);
	}

	@Override
	public PageImpl<Map<String, Object>> findAllPendingInvitedUsers(Pageable pageable, String contactCollectionName,
			String userCollectionName) {
		List<AggregationOperation> operation = new ArrayList<AggregationOperation>();

		String output = "result_ad";

		AggregationOperation contactKey = convertStringToOjectId("CONTACT");

		operation.add(Aggregation.match(new Criteria().andOperator(Criteria.where("DELETED").is(false),
				Criteria.where("EFFECTIVE_TO").is(null), Criteria.where("INVITE_ACCEPTED").is(false))));
		operation.add(contactKey);

		operation.add(buildLookupOperation(contactCollectionName, "CONTACT", "_id", output));

		operation.add(Aggregation.unwind(output));

		operation.add(Aggregation.match(new Criteria().andOperator(Criteria.where("result_ad.DELETED").is(false),
				Criteria.where("result_ad.EFFECTIVE_TO").is(null))));

		operation = addSortParameter(operation, pageable);

		operation.add(Aggregation.project().andInclude(Fields.from(Fields.field("DATA_ID", "result_ad.USER"),
				Fields.field("LAST_NAME", "result_ad.LAST_NAME"), Fields.field("FIRST_NAME", "result_ad.FIRST_NAME"),
				Fields.field("EMAIL_ADDRESS", "EMAIL_ADDRESS"), Fields.field("ROLE", "ROLE"))).andExclude("_id"));

		long count = getCount(operation, userCollectionName);

		operation = addPaginationParameters(pageable, operation);

		Aggregation aggregation = Aggregation.newAggregation(operation);

		List<Map<String, Object>> contactDetails = mongoOperations
				.aggregate(aggregation, userCollectionName, (Class<Map<String, Object>>) (Class) Map.class)
				.getMappedResults();

		return new PageImpl<Map<String, Object>>(contactDetails, pageable, count);

	}

	@Override
	public void pull(String fieldName, String dataId, String collectionName, String value) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("_id").is(dataId), Criteria.where("DELETED").is(false),
				Criteria.where("EFFECTIVE_TO").is(null));
		Query query = new Query();
		query.addCriteria(criteria);

		Update update = new Update();
		update.pull(fieldName, value);

		mongoOperations.updateMulti(query, update, collectionName);
	}

	@Override
	public void push(String fieldName, String dataId, String collectionName, String value) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("_id").is(dataId), Criteria.where("DELETED").is(false),
				Criteria.where("EFFECTIVE_TO").is(null));
		Query query = new Query();
		query.addCriteria(criteria);

		Update update = new Update();
		update.push(fieldName, value);

		mongoOperations.updateMulti(query, update, collectionName);
	}

	@Override
	public void addMetadataEntry(String entryId, Map<String, Object> metaData, String collectionName) {
		Update update = new Update();
		update.set("META_DATA", metaData);
		Criteria criteria = new Criteria();
		criteria.where("_id").is(entryId);
		mongoOperations.updateFirst(new Query(Criteria.where("_id").is(entryId)), update, collectionName);

	}

	@Override
	public void updateMetadataEvents(String dataId, DiscussionMessage eventsDiscussionMessage, String collectionName) {

		Update update = new Update();
		update.addToSet("META_DATA.EVENTS", eventsDiscussionMessage);
		Criteria criteria = new Criteria();
		criteria.where("_id").is(dataId);
		mongoOperations.updateFirst(new Query(Criteria.where("_id").is(dataId)), update, collectionName);

	}

	@Override
	public Optional<List<Map<String, Object>>> findAllTeams(List<String> teamIds, String userId, String companyId) {

		Assert.notNull(userId, "id must not be null");
		Assert.notNull(companyId, "companyId must not be null");

		String teamsCollectionName = "Teams_" + companyId;

		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("_id").in(teamIds),
				Criteria.where("EFFECTIVE_TO").is(null), Criteria.where("USERS").in(userId),
				Criteria.where("NAME").ne("Global"));

		Query query = new Query(criteria);
		return Optional.ofNullable(
				mongoOperations.find(query, (Class<Map<String, Object>>) (Class) Map.class, teamsCollectionName));
	}

	@Override
	public void updateOCRToMetadata(String dataId, List<String> receipts, String fieldName, String collectionName) {
		Update update = new Update();
		update.addToSet("META_DATA." + fieldName, receipts);
		Criteria criteria = new Criteria();
		criteria.where("_id").is(dataId);
		mongoOperations.updateFirst(new Query(Criteria.where("_id").is(dataId)), update, collectionName);

	}

	@Override
	public void pullDataByVariable(String filterVariable, String filterValue, String variable, String value,
			String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where(filterVariable).is(filterValue), Criteria.where("DELETED").is(false),
				Criteria.where("EFFECTIVE_TO").is(null));
		Query query = new Query();
		query.addCriteria(criteria);

		Update update = new Update();
		update.pull(variable, value);

		mongoOperations.updateFirst(query, update, collectionName);
	}

	@Override
	public void addDataToSetByVariable(String filterVariable, String filterValue, String variable, String value,
			String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where(filterVariable).is(filterValue), Criteria.where("DELETED").is(false),
				Criteria.where("EFFECTIVE_TO").is(null));
		Query query = new Query();
		query.addCriteria(criteria);

		Update update = new Update();
		update.addToSet(variable, value);

		mongoOperations.updateFirst(query, update, collectionName);
	}

	@Override
	public Optional<Map<String, Object>> findTeamsByVariableForIsPersonal(String fieldName, String value,
			String collectionName) {
		Assert.notNull(value, "The given value must not be null!");
		Assert.notNull(collectionName, "The given collectionName must not be null!");

		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(true), Criteria.where("IS_PERSONAL").is(true),
				Criteria.where(fieldName).is(value));

		return Optional.ofNullable(mongoOperations.findOne(new Query(criteria), Map.class, collectionName));
	}

	@Override
	public Optional<Map<String, Object>> findBySortingField(String fieldName, String collectionName) {
		Assert.notNull(collectionName, "The given collectionName must not be null!");
		
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false),
				Criteria.where("EFFECTIVE_TO").is(null));
		Query query = new Query();
		query.addCriteria(criteria);
		
		return Optional.ofNullable(mongoOperations.findOne(query.with(Sort.by(Direction.DESC, fieldName)),
				Map.class, collectionName));
	}

}