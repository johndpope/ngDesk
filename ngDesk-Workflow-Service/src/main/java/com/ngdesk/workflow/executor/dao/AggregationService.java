package com.ngdesk.workflow.executor.dao;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.http.util.Asserts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import com.ngdesk.data.dao.WorkflowExecutionInstance;
import com.ngdesk.repositories.ModuleEntryRepository;
import com.ngdesk.repositories.ModulesRepository;
import com.ngdesk.workflow.module.dao.Condition;
import com.ngdesk.workflow.module.dao.Module;
import com.ngdesk.workflow.module.dao.ModuleField;
import com.ngdesk.workflow.module.dao.ModulesService;

@Component
public class AggregationService {

	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	ModulesService modulesService;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	public Float getAggregationValue(WorkflowExecutionInstance instance, List<Condition> conditions,
			ModuleField currentField, String fieldName, String value, Module module, String fieldToAggregate) {

		Criteria criteria = new Criteria();

		// TODO: PASS FIELDS FROM PARENT MODULE WHEN WE USE PARENT CHILD RELATIONSHIP
		List<ModuleField> allFields = module.getFields();

		if (conditions != null && conditions.size() > 0) {
			criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("EFFECTIVE_TO").is(null),
					Criteria.where(fieldName).is(value), buildConditions(conditions, allFields, instance));
		} else {
			criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("EFFECTIVE_TO").is(null),
					Criteria.where(fieldName).is(value));
		}

		String collectionName = modulesService.getCollectionName(module.getName(),
				instance.getCompany().getCompanyId());

		Optional<Map<String, Object>> aggregationEntry = moduleEntryRepository.findAggregationFieldValue(fieldName,
				value, fieldToAggregate, currentField.getAggregationType(), criteria, collectionName);
		if (aggregationEntry.isPresent()) {
			if (aggregationEntry.get().get(fieldToAggregate) != null) {
				Float value1 = Float.parseFloat(aggregationEntry.get().get(fieldToAggregate).toString());
				value1 = (float) (Math.round(value1 * 100.0) / 100.0);
				return value1;
			}
		}

		return null;
	}

	private Criteria buildConditions(List<Condition> conditions, List<ModuleField> fields,
			WorkflowExecutionInstance instance) {

		List<Criteria> allCriteria = buildCriterias("All", conditions, fields, instance);
		List<Criteria> anyCriteria = buildCriterias("Any", conditions, fields, instance);

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

	private List<Criteria> buildCriterias(String requirementType, List<Condition> conditions, List<ModuleField> fields,
			WorkflowExecutionInstance instance) {
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
									"modules_" + instance.getCompany().getCompanyId());
							if (relatedModule.isPresent()) {
								Optional<ModuleField> relatedfield = relatedModule.get().getFields().stream().filter(
										field -> field.getFieldId().equals(conditionField.getRelationshipField()))
										.findFirst();
								if (relatedfield.isPresent()) {
									List<String> entryIds = moduleEntryRepository.findDistinctEntries(
											relatedfield.get().getName(),
											modulesService.getCollectionName(relatedModule.get().getName(),
													instance.getCompany().getCompanyId()));
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

}
