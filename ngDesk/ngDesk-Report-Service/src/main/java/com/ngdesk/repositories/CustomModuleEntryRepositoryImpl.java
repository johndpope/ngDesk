package com.ngdesk.repositories;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;

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
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.Assert;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.report.dao.Filter;
import com.ngdesk.report.dao.Report;
import com.ngdesk.report.module.dao.Module;
import com.ngdesk.report.module.dao.ModuleField;
import com.ngdesk.report.module.dao.ModuleService;
import com.ngdesk.report.role.RoleService;

public class CustomModuleEntryRepositoryImpl implements CustomModuleEntryRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Autowired
	AuthManager authManager;

	@Autowired
	private ModulesRepository modulesRepository;

	@Autowired
	private ModuleService modulesService;

	@Autowired
	RoleService roleService;

	@Override
	public List<Map<String, Object>> findEntriesByVariable(List<Module> modules, List<ModuleField> allFields,
			List<Filter> filters, Pageable pageable, String collectionName, Report report, Set<String> teamIds) {

		Assert.notNull(collectionName, "The given collectionName must not be null!");
		Criteria criteria = new Criteria();
		if (!roleService.isSystemAdmin(authManager.getUserDetails().getRole())
				&& !collectionName.equalsIgnoreCase("Teams_" + authManager.getUserDetails().getCompanyId())) {
			criteria.andOperator(Criteria.where("TEAMS").in(teamIds),
					buildFilters(modules, filters, allFields, report));
		} else {
			criteria = buildFilters(modules, filters, allFields, report);
		}
		Query query = new Query(criteria).with(pageable);
		return mongoOperations.find(query, (Class<Map<String, Object>>) (Class) Map.class, collectionName);
	}

	@Override
	public int reportCount(List<Module> modules, List<Filter> filters, List<ModuleField> fields, String collectionName,
			Report report) {
		Criteria criteria = buildFilters(modules, filters, fields, report);

		return (int) mongoOperations.count(new Query(criteria), collectionName);
	}

	private Criteria buildFilters(List<Module> modules, List<Filter> filters, List<ModuleField> fields, Report report) {

		List<Criteria> allCriteria = buildCriterias(modules, "All", filters, fields);
		List<Criteria> anyCriteria = buildCriterias(modules, "Any", filters, fields);

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

	private List<Criteria> buildCriterias(List<Module> modules, String requirementType, List<Filter> filters,
			List<ModuleField> fields) {
		Asserts.notNull(requirementType, "Requirement type must not be null");
		Asserts.notNull(filters, "Filters must not be null");
		Asserts.notNull(fields, "Fields must not be null");
		List<Criteria> criterias = new ArrayList<Criteria>();

		if (filters != null) {
			List<Filter> filteredCondition = filters.stream()
					.filter(condition -> condition.getRequirementType().equalsIgnoreCase(requirementType))
					.collect(Collectors.toList());

			for (Filter filter : filteredCondition) {
				if (filter.getRequirementType().equalsIgnoreCase(requirementType)) {
					String fieldId = filter.getField().getFieldId();
					String value = filter.getValue();

					String reg = "\\{\\{(.*)\\}\\}";
					Pattern r1 = Pattern.compile(reg);
					ModuleField conditionField = fields.stream().filter(field -> field.getFieldId().equals(fieldId))
							.findFirst().get();

					String displayDatatype = conditionField.getDataType().getDisplay();
					String backendDatatype = conditionField.getDataType().getBackend();
					boolean isString = false;
					boolean isInteger = false;
					boolean isBoolean = false;

					String fieldName = conditionField.getName();

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

					switch (filter.getOperator()) {
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
		}

		return criterias;

	}

	@Override
	public Optional<List<Map<String, Object>>> findAllEntries(String collectionName) {
		Assert.notNull(collectionName, "The given collectionName must not be null!");

		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("EFFECTIVE_TO").is(null));
		Query query = new Query(criteria);

		return Optional.ofNullable(
				mongoOperations.find(query, (Class<Map<String, Object>>) (Class) Map.class, collectionName));
	}

	public List<String> findDistinctEntries(String fieldName, String collectionName) {

		Asserts.notNull(collectionName, "collectionName must not be null");
		Asserts.notNull(fieldName, "fieldName must not be null");
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("EFFECTIVE_TO").is(null));
		Query query = new Query(criteria);
		query.fields().exclude("PASSWORD");
		query.fields().exclude("META_DATA");

		return mongoOperations.findDistinct(query, fieldName, collectionName, String.class);
	}

	@Override
	public Optional<List<Map<String, Object>>> findAllTeamsOfCurrentUser(String companyId) {
		String collectionName = "Teams_" + companyId;
		String userId = authManager.getUserDetails().getUserId();

		Criteria criteria = new Criteria();

		if (!roleService.isSystemAdmin(authManager.getUserDetails().getRole())) {
			criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("EFFECTIVE_TO").is(null),
					Criteria.where("USERS").in(userId));
		} else {
			criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("EFFECTIVE_TO").is(null));
		}

		Query query = new Query(criteria);
		query.fields().include("NAME");

		return Optional.ofNullable(
				mongoOperations.find(query, (Class<Map<String, Object>>) (Class) Map.class, collectionName));

	}

	@Override
	public Optional<List<Map<String, Object>>> findAllTeamsOfGivenUser(String userId, String companyId) {
		String collectionName = "Teams_" + companyId;

		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("EFFECTIVE_TO").is(null),
				Criteria.where("USERS").in(userId));

		Query query = new Query(criteria);
		query.fields().include("NAME");

		return Optional.ofNullable(
				mongoOperations.find(query, (Class<Map<String, Object>>) (Class) Map.class, collectionName));
	}

	@Override
	public List<Map<String, Object>> findEntriesByRole(List<Module> modules, List<ModuleField> allFields,
			List<Filter> filters, Pageable pageable, String collectionName, Report report, String role,
			Set<String> teamIds, String companyId) {
		Criteria criteria = new Criteria();
		if (!role.equals("SystemAdmin") && !collectionName.equalsIgnoreCase("Teams_" + companyId)) {
			criteria.andOperator(Criteria.where("TEAMS").in(teamIds),
					buildFilters(modules, filters, allFields, report));
		} else {
			criteria = buildFilters(modules, filters, allFields, report);
		}
		Query query = new Query(criteria).with(pageable);
		return mongoOperations.find(query, (Class<Map<String, Object>>) (Class) Map.class, collectionName);
	}

	@Override
	public Map<String, Object> findAllEntriesWithGivenValue(Report report, String fieldName, String value,
			String aggregationField, String collectionName) {

		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("EFFECTIVE_TO").is(null),
				Criteria.where(fieldName).is(value));

		SortOperation sortAgg = null;
		if (report.getOrder().equalsIgnoreCase("asc")) {
			sortAgg = sort(Sort.Direction.ASC, report.getSortBy().getFieldId());
		} else {
			sortAgg = sort(Sort.Direction.DESC, report.getSortBy().getFieldId());
		}

		Aggregation aggregation = newAggregation(match(criteria), sortAgg,
				group(fieldName).sum(aggregationField).as(aggregationField));

		AggregationResults<Map<String, Object>> groupResults = mongoOperations.aggregate(aggregation, collectionName,
				(Class<Map<String, Object>>) (Class) Map.class);
		return groupResults.getUniqueMappedResult();

	}
}
