package com.ngdesk.repositories.modules.data;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.limit;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.http.util.Asserts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.commons.models.OrderBy;
import com.ngdesk.graphql.modules.dao.Condition;
import com.ngdesk.graphql.modules.dao.Module;
import com.ngdesk.graphql.modules.dao.ModuleField;
import com.ngdesk.graphql.modules.dao.ModulesService;
import com.ngdesk.graphql.role.layout.dao.RoleService;
import com.ngdesk.repositories.modules.ModulesRepository;

@Repository
public class CustomModuleEntryRepositoryImpl implements CustomModuleEntryRepository {

	@Autowired
	private MongoOperations mongoOperations;

	@Autowired
	private AuthManager authManager;

	@Autowired
	private ModulesRepository modulesRepository;

	@Autowired
	private ModulesService modulesService;

	@Autowired
	private RoleService roleService;

	@Override
	public List<Map<String, Object>> findEntriesByIds(List<String> ids, String collectionName) {
		Assert.notNull(ids, "The given ids must not be null!");
		Assert.notNull(collectionName, "The given collectionName must not be null!");
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("_id").in(ids),
				Criteria.where("EFFECTIVE_TO").is(null));
		Query query = new Query(criteria);
		if (collectionName.contains("Users_")) {
			query.fields().exclude("PASSWORD");
		}
		query.fields().exclude("META_DATA");

		return mongoOperations.find(query, (Class<Map<String, Object>>) (Class) Map.class, collectionName);
	}

	@Override
	public Optional<Map<String, Object>> findEntryById(String entryId, String collectionName) {
		Assert.notNull(entryId, "The given id must not be null!");
		Assert.notNull(collectionName, "The given collectionName must not be null!");
		Criteria criteria = new Criteria();
		if (collectionName.equals("companies")) {
			criteria.andOperator(Criteria.where("_id").is(entryId));
		} else {
			criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("_id").is(entryId),
					Criteria.where("EFFECTIVE_TO").is(null));
		}
		Query query = new Query(criteria);
		if (collectionName.contains("Users_")) {
			query.fields().exclude("PASSWORD");
		}
		return Optional.ofNullable(mongoOperations.findOne(query, Map.class, collectionName));
	}

	@Override
	public int getCount(String collectionName) {
		Assert.notNull(collectionName, "The given collectionName must not be null!");
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("EFFECTIVE_TO").is(null));
		Query query = new Query(criteria);
		return (int) mongoOperations.count(query, collectionName);
	}

	@Override
	public int getCountForLayouts(List<Module> modules, List<ModuleField> allFields, List<Condition> conditions,
			String collectionName, Set<String> teamIds) {
		Assert.notNull(collectionName, "The given collectionName must not be null!");
		Assert.notNull(allFields, "The given fields must not be null!");
		Criteria criteria = new Criteria();
		if (!roleService.isSystemAdmin(authManager.getUserDetails().getRole())
				&& !collectionName.equalsIgnoreCase("Teams_" + authManager.getUserDetails().getCompanyId())) {
			criteria.andOperator(Criteria.where("TEAMS").in(teamIds), buildConditions(modules, conditions, allFields));
		} else {
			criteria = buildConditions(modules, conditions, allFields);
		}
		Query query = new Query(criteria);
		query.fields().exclude("PASSWORD");
		return (int) mongoOperations.count(query, collectionName);
	}

	@Override
	public List<Map<String, Object>> findEntriesForLayout(List<Module> modules, List<ModuleField> allFields,
			List<Condition> conditions, Pageable pageable, String collectionName, Set<String> teamIds,
			Module currentModule) {
		Assert.notNull(allFields, "The given fields must not be null!");
		Assert.notNull(collectionName, "The given collectionName must not be null!");
		Criteria criteria = new Criteria();
		if (!roleService.isSystemAdmin(authManager.getUserDetails().getRole())
				&& !collectionName.equalsIgnoreCase("Teams_" + authManager.getUserDetails().getCompanyId())) {
			criteria.andOperator(Criteria.where("TEAMS").in(teamIds), buildConditions(modules, conditions, allFields));
		} else {
			criteria = buildConditions(modules, conditions, allFields);
		}
		Query query = new Query(criteria);
		if (pageable != null) {
			query.with(pageable);
		}
		query.fields().exclude("PASSWORD");
		return mongoOperations.find(query, (Class<Map<String, Object>>) (Class) Map.class, collectionName);
	}

	@Override
	public List<Map<String, Object>> findEntriesByVariable(List<Module> modules, List<ModuleField> allFields,
			List<Condition> conditions, Pageable pageable, String variable, String value, String collectionName) {
		Assert.notNull(variable, "The given variable must not be null!");
		Assert.notNull(collectionName, "The given collectionName must not be null!");

		Criteria criteria = buildConditions(modules, conditions, allFields);
		Query query = new Query(criteria).with(pageable);
		if (!value.equals("%OneToManyUnmapped%")) {
			query.addCriteria(Criteria.where(variable).is(value));
		} else {
			query.addCriteria(Criteria.where(variable).exists(false));
		}
		return mongoOperations.find(query, (Class<Map<String, Object>>) (Class) Map.class, collectionName);
	}

	@Override
	public int getOneToManyCountValue(List<Module> modules, List<ModuleField> allFields, List<Condition> conditions,
			String variable, String value, String collectionName) {
		Assert.notNull(variable, "The given variable must not be null!");
		Assert.notNull(collectionName, "The given collectionName must not be null!");
		Criteria criteria = buildConditions(modules, conditions, allFields);
		Query query = new Query(criteria);
		if (!value.equals("%OneToManyUnmapped%")) {
			query.addCriteria(Criteria.where(variable).is(value));
		} else {
			query.addCriteria(Criteria.where(variable).exists(false));
		}
		return (int) mongoOperations.count(query, collectionName);
	}

	@Override
	public int getCountForWidgets(List<Module> modules, List<ModuleField> allFields, List<Condition> conditions,
			String collectionName, Integer limit, Boolean limitEntries) {
		Assert.notNull(collectionName, "The given collectionName must not be null!");
		Assert.notNull(allFields, "The given fields must not be null!");
		Criteria criteria = buildConditions(modules, conditions, allFields);
		Query query = new Query(criteria);
		query.skip(0);
		if (limitEntries == true) {
			query.limit(limit);
		}
		return (int) mongoOperations.count(query, collectionName);
	}

	@Override
	public int getCountForField(List<Module> modules, List<ModuleField> allFields, String fieldName, Object fieldValue,
			Integer limit, List<Condition> conditions, Boolean limitEntries, String collectionName) {
		Assert.notNull(collectionName, "The given collectionName must not be null!");
		Criteria criteria = buildConditions(modules, conditions, allFields);
		Query query = new Query(criteria);
		query.addCriteria(Criteria.where(fieldName).is(fieldValue));
		query.skip(0);
		if (limitEntries) {
			query.limit(limit);
		}
		return (int) mongoOperations.count(query, collectionName);
	}

	@Override
	public List<Map<String, Object>> getAllEntriesField(List<Module> modules, List<ModuleField> allFields,
			String fieldName, List<Object> fieldValue, List<Condition> conditions, String collectionName,
			Boolean limitEntries, Integer limit) {
		Assert.notNull(collectionName, "The given collectionName must not be null!");
		Criteria criteria = buildConditions(modules, conditions, allFields);
		Query query = new Query(criteria);
		query.addCriteria(Criteria.where(fieldName).in(fieldValue));
		query.fields().include(fieldName);
		query.skip(0);
		if (limitEntries) {
			query.limit(limit);
		}

		return mongoOperations.find(query, (Class<Map<String, Object>>) (Class) Map.class, collectionName);
	}

	@Override
	public List<Map<String, Object>> findAllByAggregationField(List<Module> modules, List<ModuleField> allFields,
			String aggFieldName, Integer limit, List<Condition> conditions, Boolean limitEntries,
			List<Object> uniqueValues, String fieldName, String type, OrderBy orderBy, String collectionName) {
		Asserts.notNull(collectionName, "collectionName must not be null");
		Criteria criteria = new Criteria();
		criteria.andOperator(buildConditions(modules, conditions, allFields),
				Criteria.where(fieldName).in(uniqueValues));
		Aggregation aggregation = null;
		SortOperation sortAgg = null;
		if (orderBy.getOrder().equalsIgnoreCase("asc")) {
			sortAgg = sort(Sort.Direction.ASC, orderBy.getColumn());
		} else {
			sortAgg = sort(Sort.Direction.DESC, orderBy.getColumn());
		}
		if (type.equals("sum")) {
			if (limitEntries) {
				aggregation = newAggregation(match(criteria), sortAgg, limit(limit),
						group(fieldName).sum(aggFieldName).as(aggFieldName), project(aggFieldName));

			} else {
				aggregation = newAggregation(match(criteria), sortAgg,
						group(fieldName).sum(aggFieldName).as(aggFieldName), project(aggFieldName));
			}

		} else if (type.equals("min")) {
			if (limitEntries) {
				aggregation = newAggregation(match(criteria), sortAgg,
						group(fieldName).min(aggFieldName).as(aggFieldName), project(aggFieldName));
			} else {
				aggregation = newAggregation(match(criteria), sortAgg,
						group(fieldName).min(aggFieldName).as(aggFieldName), project(aggFieldName));
			}

		} else if (type.equals("max")) {
			if (limitEntries) {
				aggregation = newAggregation(match(criteria), sortAgg,
						group(fieldName).max(aggFieldName).as(aggFieldName), project(aggFieldName));
			} else {
				aggregation = newAggregation(match(criteria), sortAgg,
						group(fieldName).max(aggFieldName).as(aggFieldName), project(aggFieldName));
			}
		} else if (type.equals("average")) {
			if (limitEntries) {
				aggregation = newAggregation(match(criteria), sortAgg, limit(limit),
						group(fieldName).avg(aggFieldName).as(aggFieldName), project(aggFieldName));
			} else {
				aggregation = newAggregation(match(criteria), sortAgg,
						group(fieldName).avg(aggFieldName).as(aggFieldName), project(aggFieldName));
			}
		}

		AggregationResults<Map<String, Object>> groupResults = mongoOperations.aggregate(aggregation, collectionName,
				(Class<Map<String, Object>>) (Class) Map.class);

		return groupResults.getMappedResults();

	}

	@Override
	public List<Map<String, Object>> findAllByScoreCardAggregationField(List<Module> modules,
			List<ModuleField> allFields, String aggFieldName, Integer limit, List<Condition> conditions,
			Boolean limitEntries, String type, OrderBy orderBy, String collectionName) {
		Asserts.notNull(collectionName, "collectionName must not be null");
		Criteria criteria = buildConditions(modules, conditions, allFields);

		Aggregation aggregation = null;
		SortOperation sortAgg = null;
		if (orderBy.getOrder().equalsIgnoreCase("asc")) {
			sortAgg = sort(Sort.Direction.ASC, orderBy.getColumn());
		} else {
			sortAgg = sort(Sort.Direction.DESC, orderBy.getColumn());
		}
		if (type.equals("sum")) {
			if (limitEntries) {
				aggregation = newAggregation(match(criteria), sortAgg, limit(limit),
						group().sum(aggFieldName).as(aggFieldName), project(aggFieldName));
			} else {
				aggregation = newAggregation(match(criteria), sortAgg, group().sum(aggFieldName).as(aggFieldName),
						project(aggFieldName));
			}

		} else if (type.equals("min")) {
			if (limitEntries) {
				aggregation = newAggregation(match(criteria), sortAgg, limit(limit),
						group().min(aggFieldName).as(aggFieldName), project(aggFieldName));
			} else {
				aggregation = newAggregation(match(criteria), sortAgg, group().min(aggFieldName).as(aggFieldName),
						project(aggFieldName));
			}

		} else if (type.equals("max")) {
			if (limitEntries) {
				aggregation = newAggregation(match(criteria), sortAgg, limit(limit),
						group().max(aggFieldName).as(aggFieldName), project(aggFieldName));
			} else {
				aggregation = newAggregation(match(criteria), sortAgg, group().max(aggFieldName).as(aggFieldName),
						project(aggFieldName));
			}

		} else if (type.equals("average")) {
			if (limitEntries) {
				aggregation = newAggregation(match(criteria), sortAgg, limit(limit),
						group().avg(aggFieldName).as(aggFieldName), project(aggFieldName));
			} else {
				aggregation = newAggregation(match(criteria), sortAgg, group().avg(aggFieldName).as(aggFieldName),
						project(aggFieldName));
			}

		}
		AggregationResults<Map<String, Object>> groupResults = mongoOperations.aggregate(aggregation, collectionName,
				(Class<Map<String, Object>>) (Class) Map.class);

		return groupResults.getMappedResults();
	}

	@Override
	public List<Object> findDistinctEntriyValues(String fieldName, String collectionName) {
		Asserts.notNull(collectionName, "collectionName must not be null");
		Asserts.notNull(fieldName, "fieldName must not be null");
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("EFFECTIVE_TO").is(null));
		Query query = new Query(criteria);
		if (collectionName.contains("Users_")) {
			query.fields().exclude("PASSWORD");
		}
		query.fields().exclude("META_DATA");

		return mongoOperations.findDistinct(query, fieldName, collectionName, Object.class);
	}

	private Criteria buildConditions(List<Module> modules, List<Condition> conditions, List<ModuleField> fields) {

		List<Criteria> allCriteria = buildCriterias(modules, "All", conditions, fields);
		List<Criteria> anyCriteria = buildCriterias(modules, "Any", conditions, fields);

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

	private List<Criteria> buildCriterias(List<Module> modules, String requirementType, List<Condition> conditions,
			List<ModuleField> fields) {
		try {

			Asserts.notNull(requirementType, "Requirement type must not be null");
			Asserts.notNull(conditions, "Conditions must not be null");
			Asserts.notNull(fields, "Fields must not be null");
			List<Condition> filteredCondition = conditions.stream()
					.filter(condition -> condition.getRequirementType().equalsIgnoreCase(requirementType))
					.collect(Collectors.toList());
			List<Criteria> criterias = new ArrayList<Criteria>();

			for (Condition condition : filteredCondition) {
				if (condition.getRequirementType().equalsIgnoreCase(requirementType)) {
					String fieldId = condition.getCondition();
					String value = condition.getConditionValue();

					String reg = "\\{\\{(.*)\\}\\}";
					Pattern r1 = Pattern.compile(reg);
					ModuleField conditionField = fields.stream().filter(field -> field.getFieldId().equals(fieldId))
							.findFirst().get();

					String displayDatatype = conditionField.getDataType().getDisplay();
					String backendDatatype = conditionField.getDataType().getBackend();
					boolean isString = false;
					boolean isInteger = false;
					boolean isBoolean = false;
					boolean isFloat = false;

					String fieldName = conditionField.getName();
					String displayDataType = conditionField.getDataType().getDisplay();
					SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSSX");
					Date dateValue = new Date();

					if (value != null) {
						Matcher m1 = r1.matcher(value);
						if (m1.find()) {
							if (conditionField.getDataType().getDisplay().equals("Relationship")) {
								String relatedModuleId = conditionField.getModule();
								if (relatedModuleId != null) {
									Module relatedModule = modules.stream()
											.filter(module -> module.getModuleId().equals(relatedModuleId)).findFirst()
											.orElse(null);
									if (relatedModule != null) {
										if (relatedModule.getName().equalsIgnoreCase("Contacts")) {
											value = authManager.getUserDetails().getAttributes().get("CONTACT")
													.toString();
										} else if (relatedModule.getName().equalsIgnoreCase("Users")) {
											value = authManager.getUserDetails().getUserId();
										} else {
											value = authManager.getUserDetails().getAttributes().get("CONTACT")
													.toString();
										}
									}
								}
							}
						}
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
					if (backendDatatype.equalsIgnoreCase("Float")) {
						isFloat = true;
					}
					switch (condition.getOpearator()) {
					case "EQUALS_TO":
						if (isInteger) {
							criterias.add(Criteria.where(fieldName).is(Integer.parseInt(value)));
						} else if (isBoolean) {
							criterias.add(Criteria.where(fieldName).is(Boolean.parseBoolean(value)));
						} else if (displayDatatype.equalsIgnoreCase("Time Window")) {
							Calendar oldCalendar = Calendar.getInstance();
							String[] pattern = value.split("\\(");
							String currentDate[] = pattern[1].split("-");
							if (currentDate[0].equals("current_date")) {
								value = currentDate[1].replaceAll("[^\\d]", "");
								if (pattern[0].equalsIgnoreCase("days")) {
									Date date = new Date();
									oldCalendar.setTime(date);
									oldCalendar.add(Calendar.DATE, -Integer.parseInt(value));
									criterias.add(Criteria.where("DATE_CREATED").gt(oldCalendar.getTime()).lt(date));
								} else if (pattern[0].equalsIgnoreCase("months")) {
									Date date = new Date();
									oldCalendar.setTime(date);
									oldCalendar.add(oldCalendar.MONTH, -Integer.parseInt(value));
									criterias.add(Criteria.where("DATE_CREATED").gt(oldCalendar.getTime()).lt(date));
								}
							}
						} else if (displayDatatype.equalsIgnoreCase("Approval")) {
							criterias.add(Criteria.where("APPROVAL.STATUS").is(value));
						} else if (isFloat) {
							Float floatValue = Float.valueOf(value.toString());
							if (floatValue % 1 != 0) {
								criterias.add(Criteria.where(fieldName).is(floatValue));
							} else {
								criterias.add(Criteria.where(fieldName).is(floatValue.intValue()));
							}
						} else {
							criterias.add(Criteria.where(fieldName).is(value));
						}
						break;
					case "NOT_EQUALS_TO":
						if (isInteger) {
							criterias.add(Criteria.where(fieldName).ne(Integer.parseInt(value)));
						} else if (isBoolean) {
							criterias.add(Criteria.where(fieldName).ne(Boolean.parseBoolean(value)));
						} else if (isFloat) {
							criterias.add(Criteria.where(fieldName).ne(Float.parseFloat(value)));
						} else {
							criterias.add(Criteria.where(fieldName).ne(value));
						}
						break;
					case "GREATER_THAN":
						if (displayDataType.equals("Date/Time") || displayDataType.equals("Date")
								|| displayDataType.equals("Time")) {
							dateValue = formatter.parse(value);
							criterias.add(Criteria.where(fieldName).gt(dateValue));
						} else if (isFloat) {
							criterias.add(Criteria.where(fieldName).gt(Float.parseFloat(value)));
						} else {
							criterias.add(Criteria.where(fieldName).gt(value));
						}
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
						if (displayDataType.equals("Date/Time") || displayDataType.equals("Date")
								|| displayDataType.equals("Time")) {
							dateValue = formatter.parse(value);
							criterias.add(Criteria.where(fieldName).lt(dateValue));
						} else if (isFloat) {
							criterias.add(Criteria.where(fieldName).lt(Float.parseFloat(value)));
						} else {
							criterias.add(Criteria.where(fieldName).lt(value));
						}
						break;
					case "DAYS_BEFORE_TODAY":
						if (displayDataType.equals("Date/Time") || displayDataType.equals("Date")
								|| displayDataType.equals("Time")) {
							Date date = new Date();
							Calendar oldCalendar = Calendar.getInstance();
							oldCalendar.setTime(date);
							oldCalendar.add(Calendar.DATE, -Integer.parseInt(value));
							criterias.add(Criteria.where(fieldName).gt(oldCalendar.getTime()).lt(date));
						}
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
						if ((displayDatatype.equalsIgnoreCase("Relationship"))
								&& (conditionField.getRelationshipType().equalsIgnoreCase("one to many"))) {
							Optional<Module> relatedModule = modulesRepository.findById(conditionField.getModule(),
									"modules_" + authManager.getUserDetails().getCompanyId());
							if (relatedModule.isPresent()) {
								Optional<ModuleField> relatedfield = relatedModule.get().getFields().stream().filter(
										field -> field.getFieldId().equals(conditionField.getRelationshipField()))
										.findFirst();
								if (relatedfield.isPresent()) {
									List<String> entryIds = findDistinctEntries(relatedfield.get().getName(),
											modulesService.getCollectionName(relatedModule.get().getName(),
													authManager.getUserDetails().getCompanyId()));
									criterias.add(Criteria.where("_id").in(entryIds));
								}
							}
						} else {
							criterias.add(Criteria.where(fieldName).exists(true));
						}
						break;
					case "DOES_NOT_EXIST":
						criterias.add(Criteria.where(fieldName).exists(false));
						break;
					}
				}
			}
			return criterias;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<Map<String, Object>> findEntries(Pageable pageable, Set<String> teamIds, String collectionName) {
		Asserts.notNull(pageable, "Pageable must not be null");
		Asserts.notNull(collectionName, "collectionName must not be null");
		Criteria criteria = new Criteria();
		if (!roleService.isSystemAdmin(authManager.getUserDetails().getRole())
				&& !collectionName.equalsIgnoreCase("Teams_" + authManager.getUserDetails().getCompanyId())) {
			criteria.andOperator(Criteria.where("TEAMS").in(teamIds), Criteria.where("DELETED").is(false),
					Criteria.where("EFFECTIVE_TO").is(null));
		} else {
			criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("EFFECTIVE_TO").is(null));
		}
		Query query = new Query(criteria).with(pageable);
		if (collectionName.contains("Users_")) {
			query.fields().exclude("PASSWORD");
		}
		return mongoOperations.find(query, (Class<Map<String, Object>>) (Class) Map.class, collectionName);
	}

	@Override
	public List<Map<String, Object>> findEntriesWithSearch(List<String> entryIds, Pageable pageable,
			String collectionName) {
		Asserts.notNull(pageable, "Pageable must not be null");
		Asserts.notNull(collectionName, "collectionName must not be null");
		Asserts.notNull(entryIds, "entryIds must not be null");
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("EFFECTIVE_TO").is(null),
				Criteria.where("_id").in(entryIds));
		Query query = new Query(criteria).with(pageable);
		if (collectionName.contains("Users_")) {
			query.fields().exclude("PASSWORD");
		}
		return mongoOperations.find(query, (Class<Map<String, Object>>) (Class) Map.class, collectionName);
	}

	@Override
	public List<Map<String, Object>> findUnmappedEntriesWithSearch(List<String> entryIds, Pageable pageable,
			String collectionName, String fieldName) {
		Asserts.notNull(pageable, "Pageable must not be null");
		Asserts.notNull(collectionName, "collectionName must not be null");
		Asserts.notNull(entryIds, "entryIds must not be null");
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("EFFECTIVE_TO").is(null),
				Criteria.where("_id").in(entryIds));
		Query query = new Query(criteria).with(pageable);
		if (collectionName.contains("Users_")) {
			query.fields().exclude("PASSWORD");
		}
		query.addCriteria(Criteria.where(fieldName).exists(false));
		return mongoOperations.find(query, (Class<Map<String, Object>>) (Class) Map.class, collectionName);
	}

	@Override
	public List<Map<String, Object>> findAllTeamsOfCurrentUser(String companyId, Boolean isAdmin) {
		String collectionName = "Teams_" + companyId;
		String userId = authManager.getUserDetails().getUserId();

		Criteria criteria = new Criteria();

		if (!isAdmin) {
			criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("EFFECTIVE_TO").is(null),
					Criteria.where("USERS").in(userId));
		} else {
			criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("EFFECTIVE_TO").is(null));
		}

		Query query = new Query(criteria);
		query.fields().include("NAME");

		return mongoOperations.find(query, (Class<Map<String, Object>>) (Class) Map.class, collectionName);

	}

	@Override
	public int getCountForSearch(List<String> entryIds, String collectionName) {
		Assert.notNull(collectionName, "The given collectionName must not be null!");

		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("EFFECTIVE_TO").is(null),
				Criteria.where("_id").in(entryIds));
		Query query = new Query(criteria);

		return (int) mongoOperations.count(query, collectionName);
	}

	@Override
	public int getCountForUnmappedEntriesSearch(List<String> entryIds, String collectionName, String fieldName) {
		Assert.notNull(collectionName, "The given collectionName must not be null!");

		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("EFFECTIVE_TO").is(null),
				Criteria.where("_id").in(entryIds));
		Query query = new Query(criteria);
		query.addCriteria(Criteria.where(fieldName).exists(false));
		return (int) mongoOperations.count(query, collectionName);
	}

	@Override
	public List<String> findEntriesDistinctValues(String fieldName, Pageable pageable, String collectionName) {
		Asserts.notNull(pageable, "Pageable must not be null");
		Asserts.notNull(collectionName, "collectionName must not be null");
		Asserts.notNull(fieldName, "fieldName must not be null");
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("EFFECTIVE_TO").is(null));
		Query query = new Query(criteria).with(pageable);
		if (collectionName.contains("Users_")) {
			query.fields().exclude("PASSWORD");
		}
		query.fields().exclude("META_DATA");

		return mongoOperations.findDistinct(query, fieldName, collectionName, String.class);
	}

	@Override
	public List<String> findEntriesDistinctValuesWithSearch(List<String> entryIds, String fieldName, Pageable pageable,
			String collectionName) {
		Asserts.notNull(pageable, "Pageable must not be null");
		Asserts.notNull(collectionName, "collectionName must not be null");
		Asserts.notNull(entryIds, "entryIds must not be null");
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("EFFECTIVE_TO").is(null),
				Criteria.where("_id").in(entryIds));
		Query query = new Query(criteria).with(pageable);
		if (collectionName.contains("Users_")) {
			query.fields().exclude("PASSWORD");
		}
		query.fields().exclude("META_DATA");
		return mongoOperations.findDistinct(query, fieldName, collectionName, String.class);

	}

	@Override
	public List<Map<String, Object>> findAllEntriesWithGivenValue(String fieldName, String value,
			String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("EFFECTIVE_TO").is(null),
				Criteria.where(fieldName).is(value));
		Query query = new Query(criteria);
		return mongoOperations.find(query, (Class<Map<String, Object>>) (Class) Map.class, collectionName);
	}

	public List<String> findDistinctEntries(String fieldName, String collectionName) {
		Asserts.notNull(collectionName, "collectionName must not be null");
		Asserts.notNull(fieldName, "fieldName must not be null");
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("EFFECTIVE_TO").is(null));
		Query query = new Query(criteria);
		if (collectionName.contains("Users_")) {
			query.fields().exclude("PASSWORD");
		}
		query.fields().exclude("META_DATA");

		return mongoOperations.findDistinct(query, fieldName, collectionName, String.class);
	}

	@Override
	public List<Map<String, Object>> findEntriesWithSearchIncludingConditions(List<String> entryIds,
			List<Condition> conditions, List<Module> modules, List<ModuleField> allFields, Pageable pageable,
			String collectionName) {
		Asserts.notNull(pageable, "Pageable must not be null");
		Asserts.notNull(collectionName, "collectionName must not be null");
		Asserts.notNull(entryIds, "entryIds must not be null");
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("EFFECTIVE_TO").is(null),
				Criteria.where("_id").in(entryIds), buildConditions(modules, conditions, allFields));
		Query query = new Query(criteria).with(pageable);
		if (collectionName.contains("Users_")) {
			query.fields().exclude("PASSWORD");
		}
		query.fields().exclude("META_DATA");
		return mongoOperations.find(query, (Class<Map<String, Object>>) (Class) Map.class, collectionName);
	}

	public List<Map<String, Object>> findAllByDashboardRelationship(String primaryDisplayField, String collectionName) {
		Asserts.notNull(collectionName, "collectionName must not be null");
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("EFFECTIVE_TO").is(null));
		Query query = new Query(criteria);
		query.fields().include(primaryDisplayField);

		return mongoOperations.find(query, (Class<Map<String, Object>>) (Class) Map.class, collectionName);
	}

	@Override
	public Optional<Map<String, Object>> findFirstEntryByFieldValue(String fieldName, String fieldValue,
			String collectionName) {
		Assert.notNull(fieldName, "The given field name must not be null!");
		Assert.notNull(fieldValue, "The given field value must not be null!");
		Assert.notNull(collectionName, "The given collectionName must not be null!");
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where(fieldName).is(fieldValue),
				Criteria.where("EFFECTIVE_TO").is(null));
		Query query = new Query(criteria);
		if (collectionName.contains("Users_")) {
			query.fields().exclude("PASSWORD");
		}
		return Optional.ofNullable(mongoOperations.findOne(query, Map.class, collectionName));
	}

	@Override
	public int getCountForSearchIncludingConditions(List<String> entryIds, List<Condition> conditions,
			List<Module> modules, List<ModuleField> allFields, String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("EFFECTIVE_TO").is(null),
				Criteria.where("_id").in(entryIds), buildConditions(modules, conditions, allFields));
		Query query = new Query(criteria);

		return (int) mongoOperations.count(query, collectionName);
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
	public Optional<Map<String, Object>> findAggregationFieldValue(String fieldName, String value,
			String aggregationField, String aggregationType, Set<String> teamIds, List<Condition> conditions,
			List<Module> modules, List<ModuleField> allFields, String collectionName) {

		Criteria criteria = new Criteria();

		if (!roleService.isSystemAdmin(authManager.getUserDetails().getRole())
				&& !collectionName.equalsIgnoreCase("Teams_" + authManager.getUserDetails().getCompanyId())) {
			if (conditions != null) {
				criteria.andOperator(Criteria.where("TEAMS").in(teamIds), Criteria.where("DELETED").is(false),
						Criteria.where("EFFECTIVE_TO").is(null), Criteria.where(fieldName).is(value),
						buildConditions(modules, conditions, allFields));
			} else {
				criteria.andOperator(Criteria.where("TEAMS").in(teamIds), Criteria.where("DELETED").is(false),
						Criteria.where("EFFECTIVE_TO").is(null), Criteria.where(fieldName).is(value));
			}

		} else {
			if (conditions != null) {
				criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("EFFECTIVE_TO").is(null),
						Criteria.where(fieldName).is(value), buildConditions(modules, conditions, allFields));
			} else {
				criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("EFFECTIVE_TO").is(null),
						Criteria.where(fieldName).is(value));
			}
		}
		Aggregation aggregation = null;
		if (aggregationType.equals("sum")) {
			aggregation = newAggregation(match(criteria), group().sum(aggregationField).as(aggregationField));

		} else if (aggregationType.equals("min")) {
			aggregation = newAggregation(match(criteria), group().min(aggregationField).as(aggregationField));

		} else if (aggregationType.equals("max")) {
			aggregation = newAggregation(match(criteria), group().max(aggregationField).as(aggregationField));

		}
		AggregationResults<Map<String, Object>> groupResults = mongoOperations.aggregate(aggregation, collectionName,
				(Class<Map<String, Object>>) (Class) Map.class);

		return Optional.ofNullable(groupResults.getUniqueMappedResult());

	}

	@Override
	public Optional<Map<String, Object>> getPublicTeams(String collectionName) {
		Criteria criteria = Criteria.where("NAME").is("Public");
		Query query = new Query(criteria);
		return Optional.ofNullable(mongoOperations.findOne(query, Map.class, collectionName));
	}

	@Override
	public Optional<List<Map<String, Object>>> findEntriesWithConditions(List<Condition> conditionsList,
			Pageable pageable, String collectionName, List<Module> modules, List<ModuleField> fields,
			Set<String> teamIds) {
		Criteria criteria = new Criteria();
		if (!collectionName.contains("Users_")
				&& !collectionName.equalsIgnoreCase("Teams_" + authManager.getUserDetails().getCompanyId())) {
			criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("EFFECTIVE_TO").is(null),
					Criteria.where("TEAMS").in(teamIds), buildConditions(modules, conditionsList, fields));
		}

		Query query = new Query().with(pageable);
		if (collectionName.contains("Users_")) {
			criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("EFFECTIVE_TO").is(null),
					Criteria.where("EMAIL_ADDRESS").ne("ghost@ngdesk.com"),
					Criteria.where("EMAIL_ADDRESS").ne("system@ngdesk.com"), Criteria.where("TEAMS").in(teamIds),
					buildConditions(modules, conditionsList, fields));
			query.fields().exclude("PASSWORD");
		}
		query.addCriteria(criteria);
		query.fields().exclude("META_DATA");
		return Optional.ofNullable(
				mongoOperations.find(query, (Class<Map<String, Object>>) (Class) Map.class, collectionName));
	}
}