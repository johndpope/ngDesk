package com.ngdesk.module.field.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.module.dao.Module;
import com.ngdesk.module.elastic.dao.ElasticService;
import com.ngdesk.module.role.dao.RoleService;
import com.ngdesk.repositories.FieldRepository;
import com.ngdesk.repositories.ModuleEntryRepository;
import com.ngdesk.repositories.ModuleRepository;
import com.ngdesk.repositories.WorkflowRepository;
import com.ngdesk.workflow.dao.Workflow;

@Component
public class FieldValidator {

	@Autowired
	ModuleRepository moduleRepository;

	@Autowired
	AuthManager authManager;

	@Autowired
	RoleService roleService;

	@Autowired
	WorkflowRepository workflowRepository;

	@Autowired
	ModuleFieldService moduleFieldService;

	@Autowired
	FieldRepository fieldRepository;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Autowired
	ElasticService elasticService;

	public ModuleField isValidField(ModuleField moduleField, Module module) {

		switch (moduleField.getDataType().getDisplay()) {
		case "Currency Exchange":
			isValidCurrencyExchange(module, moduleField);
			break;
		case "Formula":
			isValidFormulaField(module, moduleField);
			break;
		case "List Formula":
			isValidListFormula(module, moduleField);
			break;
		case "Picklist":
		case "Picklist (Multi-Select)":
			isValidPicklistValues(moduleField);
			break;
		case "Aggregate":
			isValidAggregation(moduleField, module);
			break;
		case "Button":
			isValidButtonDataType(moduleField, module);
			break;
		case "Discussion":
			isValidDiscussionField(module, moduleField);
			break;
		case "Chronometer":
			moduleField = isValidChronometer(moduleField);
		case "Checkbox":
			isValidFieldsMapping(module, moduleField);
		case "Receipt Capture":
			isValidReceiptCapture(module, moduleField);

		default:
			break;
		}

		if (!moduleField.getName().equalsIgnoreCase("DISCOVERY_MAP")) {

			if (moduleField.getDataType().getDisplay().equals("Picklist (Multi-Select)")) {
				if (moduleField.getPicklistValues().size() < 1) {
					throw new BadRequestException("PICKLIST_VALUES_EMPTY", null);
				}
			}

		}

		return moduleField;
	}

	private void isValidCurrencyExchange(Module module, ModuleField moduleField) {
		if (moduleField.getToCurrency() == null) {
			throw new BadRequestException("TO_CURRENCY_NOT_EMPTY", null);
		}

		ModuleField toCurrencyField = module.getFields().stream()
				.filter(field -> field.getFieldId().equals(moduleField.getToCurrency())).findFirst().orElse(null);
		if (toCurrencyField == null) {
			String vars[] = { module.getName() };
			throw new BadRequestException("TO_CURRENCY_NOT_FOUND", vars);
		}

		if (moduleField.getFromCurrency() == null) {
			throw new BadRequestException("FROM_CURRENCY_NOT_EMPTY", null);
		}

		ModuleField fromCurrencyField = module.getFields().stream()
				.filter(field -> field.getFieldId().equals(moduleField.getFromCurrency())).findFirst().orElse(null);
		if (fromCurrencyField == null) {
			String vars[] = { module.getName() };
			throw new BadRequestException("FROM_CURRENCY_NOT_FOUND", vars);
		}

		if (moduleField.getDateIncurred() == null) {
			throw new BadRequestException("DATE_INCURRED_NOT_EMPTY", null);
		}

		ModuleField dateField = module.getFields().stream()
				.filter(field -> field.getFieldId().equals(moduleField.getDateIncurred())).findFirst().orElse(null);
		if (dateField == null) {
			String vars[] = { module.getName() };
			throw new BadRequestException("DATE_INCURRED_NOT_FOUND", vars);
		}
	}

	public void isValidDiscussionField(Module module, ModuleField moduleField) {
		Optional<ModuleField> optionalDiscussionField = module.getFields().stream()
				.filter(field -> field.getDataType().getDisplay().equals("Discussion")
						&& !field.getFieldId().equals(moduleField.getFieldId()))
				.findFirst();
		if (optionalDiscussionField.isPresent()) {
			String[] vars = { module.getName() };
			throw new BadRequestException("DUPLICATE_DISCUSSION_FIELD", vars);
		}

	}

	public void isValidAggregation(ModuleField field, Module module) {
		if (field.getAggregationField() == null) {
			String[] vars = { "AGGREGATION_FIELD" };
			throw new BadRequestException("DAO_VARIABLE_REQUIRED", vars);
		}

		if (field.getAggregationType() == null) {
			String[] vars = { "AGGREGATION_TYPE" };
			throw new BadRequestException("DAO_VARIABLE_REQUIRED", vars);
		}

		if (field.getAggregationRelatedField() == null) {
			String[] vars = { "AGGREGATION_RELATED_FIELD" };
			throw new BadRequestException("DAO_VARIABLE_REQUIRED", vars);
		}

		ModuleField aggregationField = module.getFields().stream()
				.filter(moduleField -> moduleField.getFieldId().equals(field.getAggregationField())
						&& moduleField.getDataType().getDisplay().equalsIgnoreCase("Relationship"))
				.findFirst().orElse(null);
		if (aggregationField == null) {
			String[] vars = { "AGGREGATION_FIELD" };
			throw new BadRequestException("DAO_NOT_FOUND", vars);
		}

		Optional<Module> optionalAggregationModule = moduleRepository.findById(aggregationField.getModule(),
				"modules_" + authManager.getUserDetails().getCompanyId());

		Module aggregationModule = optionalAggregationModule.get();

		ModuleField aggregationRelatedField = aggregationModule.getFields().stream()
				.filter(moduleField -> moduleField.getFieldId().equals(field.getAggregationRelatedField())).findAny()
				.orElse(null);

		if (aggregationRelatedField == null) {
			String[] vars = { "AGGREGATION_RELATED_FIELD" };
			throw new BadRequestException("DAO_NOT_FOUND", vars);
		}

		String backendDataType = aggregationRelatedField.getDataType().getBackend();
		String displayDataType = aggregationRelatedField.getDataType().getDisplay();

		if (!backendDataType.equals("Float") && !backendDataType.equals("Integer") && !backendDataType.equals("Double")
				&& !displayDataType.equals("Formula")) {
			throw new BadRequestException("INVALID_AGGREGATION_RELATIONSHIP_FIELD", null);
		}

	}

	public void isValidPicklistValues(ModuleField field) {
		List<String> picklistValues = field.getPicklistValues();
		Set<String> uniqueValues = new HashSet<String>(picklistValues);
		if (picklistValues.size() < 1) {
			throw new BadRequestException("PICKLIST_VALUES_EMPTY", null);
		}
		if (picklistValues.size() != uniqueValues.size()) {
			throw new BadRequestException("PICKLIST_VALUES_NOT_UNIQUE", null);
		}
	}

	public void isValidDataType(DataType dataType) {
		List<String> displayDatatypes = List.of("Auto Number", "Formula", "Relationship", "Currency", "Email",
				"Geolocation", "Number", "Percent", "Phone", "Picklist", "Picklist (Multi-Select)", "Text", "Text Area",
				"Text Area Long", "Text Area Rich", "Time", "URL", "File Upload", "Payment", "Discussion", "Checkbox",
				"Date", "Date/Time", "Street 1", "Street 2", "City", "Country", "State", "Zipcode", "List Text",
				"Chronometer", "ID", "Address", "Button", "Workflow Stages", "Aggregate", "Approval", "File Preview",
				"Zoom", "Image", "Condition", "PDF", "Time Window", "Currency Exchange", "Receipt Capture", "Password",
				"List Formula");

		List<String> backendDataTypes = List.of("String", "Array", "Float", "json", "Integer", "Double", "Boolean",
				"Date", "BLOB", "Button", "Aggregate", "Zoom", "Approval");

		if (!displayDatatypes.contains(dataType.getDisplay())) {
			String[] displayType = { dataType.getDisplay() };
			throw new BadRequestException("INVALID_DISPLAY_DATA_TYPE", displayType);
		}

		if (!backendDataTypes.contains(dataType.getBackend())) {
			String[] backendType = { dataType.getBackend() };
			throw new BadRequestException("INVALID_BACKEND_DATA_TYPE", backendType);
		}
	}

	public void checkForRestrictedName(String fieldName) {
		String[] restrictedFieldNames = new String[] { "DATE_CREATED", "DATE_UPDATED", "LAST_UPDATED_BY", "CREATED_BY",
				"EFFECTIVE_FROM", "EFFECTIVE_TO", "SOURCE_TYPE", "CHANNEL" };

		if (Arrays.asList(restrictedFieldNames).contains(fieldName)) {
			String[] vars = { fieldName };
			throw new BadRequestException("FIELD_NAME_RESTRICTED", vars);
		}
	}

	public void isValidButtonDataType(ModuleField field, Module module) {

		if (field.getWorkflow() == null) {
			String[] vars = { "WORKFLOW" };
			throw new BadRequestException("DAO_VARIABLE_REQUIRED", vars);
		}

		Optional<Workflow> optionalWorkflow = workflowRepository.getWorkflowByModule(field.getWorkflow(),
				module.getModuleId(), authManager.getUserDetails().getCompanyId(), "module_workflows");
		if (optionalWorkflow.isEmpty()) {
			String[] vars = { "WORKFLOW" };
			throw new BadRequestException("DAO_NOT_FOUND", vars);
		}

	}

	public void isValidDispayAndBackendDataType(DataType dataType) {
		String[] displayTypes = { "Email", "Phone", "Picklist", "Text", "Text Area", "Text Area Long", "Text Area Rich",
				"URL", "Street 1", "Street 2", "City", "Country", "State", "ID", "Address", "Zipcode", "Condition",
				"Password", "List Formula" };
		String[] data = { dataType.getBackend(), dataType.getDisplay() };
		for (String display : displayTypes) {
			if (dataType.getDisplay().equals(display) && !dataType.getBackend().equals("String")) {
				throw new BadRequestException("INVALID_BACKEND_DATA_TYPE_FOR_DISPLAY_DATA_TYPE", data);
			}
		}
		if (dataType.getDisplay().equals("Auto Number") || dataType.getDisplay().equals("Number")) {
			if (!dataType.getBackend().equals("Integer")) {
				throw new BadRequestException("INVALID_BACKEND_DATA_TYPE_FOR_DISPLAY_DATA_TYPE", data);
			}
		} else if (dataType.getDisplay().equals("List Text") && !dataType.getBackend().equals("Array")) {
			throw new BadRequestException("INVALID_BACKEND_DATA_TYPE_FOR_DISPLAY_DATA_TYPE", data);

		} else if (dataType.getDisplay().equals("Currency") && !dataType.getBackend().equals("Float")) {
			throw new BadRequestException("INVALID_BACKEND_DATA_TYPE_FOR_DISPLAY_DATA_TYPE", data);

		} else if (dataType.getDisplay().equals("Zoom") && !dataType.getBackend().equals("Zoom")) {
			throw new BadRequestException("INVALID_BACKEND_DATA_TYPE_FOR_DISPLAY_DATA_TYPE", data);

		} else if (dataType.getDisplay().equals("Percent") && !dataType.getBackend().equals("Double")) {
			throw new BadRequestException("INVALID_BACKEND_DATA_TYPE_FOR_DISPLAY_DATA_TYPE", data);

		} else if (((dataType.getDisplay().equals("File Upload")) || (dataType.getDisplay().equals("Receipt Capture")))
				&& !dataType.getBackend().equals("BLOB")) {
			throw new BadRequestException("INVALID_BACKEND_DATA_TYPE_FOR_DISPLAY_DATA_TYPE", data);

		} else if (dataType.getDisplay().equals("Geolocation") && !dataType.getBackend().equals("json")) {
			throw new BadRequestException("INVALID_BACKEND_DATA_TYPE_FOR_DISPLAY_DATA_TYPE", data);

		} else if (dataType.getDisplay().equals("Checkbox") && !dataType.getBackend().equals("Boolean")) {
			throw new BadRequestException("INVALID_BACKEND_DATA_TYPE_FOR_DISPLAY_DATA_TYPE", data);

		} else if (dataType.getDisplay().equals("Image") && !dataType.getBackend().equals("BLOB")) {
			throw new BadRequestException("INVALID_BACKEND_DATA_TYPE_FOR_DISPLAY_DATA_TYPE", data);

		} else if (dataType.getDisplay().equals("Picklist (Multi-Select)") && !dataType.getBackend().equals("Array")) {
			throw new BadRequestException("INVALID_BACKEND_DATA_TYPE_FOR_DISPLAY_DATA_TYPE", data);

		} else if (dataType.getDisplay().equals("Date") || dataType.getDisplay().equals("Date/Time")
				|| dataType.getDisplay().equals("Time")) {
			if (!dataType.getBackend().equals("Date")) {
				throw new BadRequestException("INVALID_BACKEND_DATA_TYPE_FOR_DISPLAY_DATA_TYPE", data);
			}
		} else if (dataType.getDisplay().equals("Time Window") && !dataType.getBackend().equals("Timestamp")) {
			throw new BadRequestException("INVALID_BACKEND_DATA_TYPE_FOR_DISPLAY_DATA_TYPE", data);
		} else if (dataType.getDisplay().equals("Currency Exchange") && !dataType.getBackend().equals("Float")) {
			throw new BadRequestException("INVALID_BACKEND_DATA_TYPE_FOR_DISPLAY_DATA_TYPE", data);
		}
	}

	public void isValidFieldSize(Module module) {
		if (module.getFields().size() >= 100) {
			throw new BadRequestException("REACHED_MAXIMUM_FIELDS_LIMIT", null);
		}
	}

	public void setValidFieldName(ModuleField moduleField) {
		String fieldName = moduleField.getName();
		fieldName = fieldName.trim();
		fieldName = fieldName.toUpperCase();
		fieldName = fieldName.replaceAll("\\s+", "_");

		moduleField.setName(fieldName);
	}

	public ModuleField isValidChronometer(ModuleField field) {
		if (field.getDefaultValue() != null) {

			String regex = "^(\\d+mo)?(\\d+w)?(\\d+d)?(\\d+h)?(\\d+m)?$";
			String value = field.getDefaultValue();
			String valueWithoutminus = value.replaceAll("\\-", "");
			String valueWithoutSpace = valueWithoutminus.replaceAll("\\s+", "");
			Pattern p = Pattern.compile(regex, Pattern.DOTALL);
			Matcher m = p.matcher(valueWithoutSpace);
			if (!m.find()) {
				throw new BadRequestException("DEFAULT_CHRONOMETER_VALUE_INVALID", null);
			}

			long chronometerValueInSecond = 0;
			if (valueWithoutSpace.length() == 0 || valueWithoutSpace.charAt(0) == '-') {
				field.setDefaultValue(null);
			} else if (valueWithoutSpace.length() != 0) {
				chronometerValueInSecond = moduleFieldService.chronometerValueConversionInSeconds(valueWithoutSpace);
				field.setDefaultValue(chronometerValueInSecond + "");
			}
		}
		return field;
	}

	public void isValidFieldName(String fieldName) {

		if (!Character.isLetter(fieldName.charAt(0))) {
			throw new BadRequestException("FIELD_NAME_MUST_BE_CHARACTER", null);
		}

	}

	public void generateAutoNumberForExistingEntries(ModuleField moduleField, Module module, String companyId) {
		if (moduleField.getDataType().getDisplay().equals("Auto Number")) {
			if (moduleField.getAutonumberGeneration() == null) {
				return;
			}
			if (moduleField.getAutonumberGeneration() == true) {

				String collectionName = module.getName().replaceAll("\\s+", "_") + "_" + companyId;
				Optional<List<Map>> optionalEntries = moduleEntryRepository
						.findAllEntriesByCollectionName(collectionName);
				List<Map> entries = optionalEntries.get();

				int autoNumber = 0;
				if (moduleField.getAutonumberStartingNumber() >= 1) {
					autoNumber = moduleField.getAutonumberStartingNumber();
				} else {
					autoNumber = 1;
				}

				for (Map<String, Object> entry : entries) {
					entry.put(moduleField.getName().toUpperCase().trim() + "_ID", autoNumber);

					moduleEntryRepository.updateEntry(entry, collectionName);
					elasticService.postIntoElastic(module, companyId, entry);
					autoNumber++;
				}
			}
		}
	}

	public void isValidFieldsMapping(Module module, ModuleField moduleField) {

		Map<String, String> fieldsMapping = moduleField.getFieldsMapping();

		if (fieldsMapping == null) {
			return;
		}
		String[] vars = { moduleField.getDataType().getDisplay() };

		if (!moduleField.getDataType().getDisplay().equals("Checkbox")) {
			throw new BadRequestException("CHECKBOX_DISPLAY_TYPE_REQUIRED", vars);
		}

		for (String key : fieldsMapping.keySet()) {

			ModuleField field1 = module.getFields().stream().filter(field -> field.getFieldId().equals(key)).findFirst()
					.orElse(null);
			if (field1 == null) {
				throw new BadRequestException("FIELDS_MAPPING_FIELD_ID_NOT_FOUND", null);
			}

			ModuleField field2 = module.getFields().stream()
					.filter(field -> field.getFieldId().equals(fieldsMapping.get(key))).findFirst().orElse(null);
			if (field2 == null) {
				throw new BadRequestException("FIELDS_MAPPING_FIELD_ID_NOT_FOUND", null);
			}

			String[] displayType = { field1.getDataType().getDisplay(), field2.getDataType().getDisplay() };
			String[] backendType = { field1.getDataType().getBackend(), field2.getDataType().getBackend() };

			if (!field1.getDataType().getDisplay().equals(field2.getDataType().getDisplay())) {
				throw new BadRequestException("FIELDS_MAPPING_HAS_DIFFERENT_DISPLAY_TYPES", displayType);
			}
			if (!field1.getDataType().getBackend().equals(field2.getDataType().getBackend())) {
				throw new BadRequestException("FIELDS_MAPPING_HAS_DIFFERENT_BACKEND_TYPES", backendType);
			}
		}
	}

	public void validateZoomDataType(ModuleField moduleField, Module module) {
		if (moduleField.getDataType().getDisplay().equals("Zoom")) {
			ModuleField fields = module.getFields().stream()
					.filter(field -> field.getDataType().getDisplay().equals("Zoom")).findFirst().orElse(null);
			if (fields != null) {
				throw new BadRequestException("ZOOM_DATA_TYPE_EXIST", null);
			}
		}
	}

	public void isValidFormulaField(Module module, ModuleField moduleField) {
		if (moduleField.getDataType().getDisplay().equals("Formula")) {
			if (moduleField.getFormula() == null || moduleField.getFormula().isBlank()) {
				throw new BadRequestException("FORMULA_FIELD_EMPTY", null);
			}

			String formula = moduleField.getFormula();
			isValidFormula(module, moduleField, formula);
		}
	}

	public String isValidFormula(Module module, ModuleField moduleField, String formula) {
		Optional<List<Module>> optionalModules = moduleRepository
				.findAllModules("modules_" + authManager.getUserDetails().getCompanyId());
		List<Module> modules = optionalModules.get();
		List<ModuleField> moduleFields = module.getFields();
		List<String> displayDatatypes = List.of("Auto Number", "Currency", "Number", "Text", "Street 1", "Street 2",
				"City", "Country", "State", "Zipcode", "Chronometer", "Email", "Picklist", "Formula",
				"Currency Exchange", "List Formula");
		List<String> stringDisplayDataTypes = List.of("Text", "Street 1", "Street 2", "City", "Country", "State",
				"Zipcode", "Chronometer", "Email", "Picklist");

		if (!validateBrackets(formula)) {
			throw new BadRequestException("BRACKETS_NOT_CLOSED_PROPERLY", null);
		}

		String regExpPos = "([\\(]+[\\/*+-])|([\\/*+-][\\)]+)|(^[\\/+*-])|([\\/+*-]$)|([\\/+*-][\\/+*-]+)";
		Pattern pattern = Pattern.compile(regExpPos);
		Matcher matcher = pattern.matcher(formula);
		if (matcher.find()) {
			String[] vars = { matcher.group() };
			throw new BadRequestException("OPERATOR_POSITION_NOT_VALID", vars);
		}

		formula = getFormulaRecursively(formula, module);
		int stringFieldCount = 0;
		String reg = "\\{\\{(?i)(inputMessage[_a-zA-Z0-9\\.\\-]+)\\}\\}";
		pattern = Pattern.compile(reg);
		matcher = pattern.matcher(formula);
		while (matcher.find()) {
			String path = matcher.group(1).split("(?i)inputMessage\\.")[1];
			String[] fields = path.split("\\.");
			String fieldDisplayType;
			if (fields.length == 1) {
				if (validateCustomOperators(fields[0])) {
					continue;
				}

				Optional<ModuleField> optionalField = moduleFields.stream()
						.filter(field -> field.getName().equals(fields[0])).findFirst();
				if (optionalField.isEmpty()) {
					String[] vars = { fields[0], module.getName() };
					throw new BadRequestException("FIELD_NOT_FOUND", vars);
				}
				ModuleField field = optionalField.get();
				if (field.getDataType().getDisplay().equals("List Formula")) {
					List<ListFormulaField> listFormulaFields = field.getListFormula();
					for (ListFormulaField listFormulaField : listFormulaFields) {

						matcher = pattern.matcher(listFormulaField.getFormula());
						while (matcher.find()) {
							String listFormulaPath = matcher.group(1).split("(?i)inputMessage\\.")[1];
							String[] listfields = listFormulaPath.split("\\.");
							if (Arrays.asList(listfields).contains(moduleField.getName())) {
								String[] vars = { moduleField.getName() };
								throw new BadRequestException("FIELD_CANNOT_BE_LOOPED", vars);
							}
						}
					}
				}
				if (field.getDataType().getDisplay().equals("Relationship")) {
					String[] vars = { fields[0] };
					throw new BadRequestException("FIELD_CANNOT_END_BY_RELATIONSHIP", vars);

				} else if (!displayDatatypes.contains(field.getDataType().getDisplay())) {
					String[] vars = { fields[0] };
					throw new BadRequestException("DISPALY_TYPE_NOT_VALID_FOR_FORMULA", vars);
				}
				fieldDisplayType = field.getDataType().getDisplay();
			} else {
				fieldDisplayType = validateFormulaRecursively(module, modules, path, displayDatatypes);
			}

			if (stringDisplayDataTypes.contains(fieldDisplayType)) {
				stringFieldCount += 1;
			}
		}
		if (stringFieldCount > 0) {
			String regExpOp = "[\\/\\*\\-]";
			pattern = Pattern.compile(regExpOp);
			matcher = pattern.matcher(formula);
			if (matcher.results().count() > 0) {
				throw new BadRequestException("FOR_STRING_OPERATORS_NOT_ALLOWED", null);
			}
			return "String";

		}
		return "Number";
	}

	public String validateFormulaRecursively(Module module, List<Module> modules, String path,
			List<String> displayDatatypes) {
		String[] fields = path.split("\\.");
		List<ModuleField> moduleFields = module.getFields();
		Optional<ModuleField> optionalField = moduleFields.stream().filter(field -> field.getName().equals(fields[0]))
				.findFirst();

		if (optionalField.isEmpty()) {
			String[] vars = { fields[0], module.getName() };
			throw new BadRequestException("FIELD_NOT_FOUND", vars);
		}
		ModuleField moduleField = optionalField.get();
		if (!moduleField.getDataType().getDisplay().equals("Relationship")) {
			String[] vars = { fields[0] };
			throw new BadRequestException("NOT_VALID_RELATIONSHIP_FIELD", vars);

		} else if (moduleField.getRelationshipType().equals("One to Many")
				|| moduleField.getRelationshipType().equals("Many to Many")) {
			String[] vars = { fields[0] };
			throw new BadRequestException("NOT_VALID_RELATIONSHIP_TYPE", vars);

		} else if (moduleField.getName().equals("TEAMS")) {
			// Should handle
			return "";
		}

		Optional<Module> optionalRelatedModule = modules.stream()
				.filter(rmodule -> rmodule.getModuleId().equals(moduleField.getModule())).findFirst();
		if (optionalRelatedModule.isEmpty()) {
			String[] vars = { fields[0] };
			throw new BadRequestException("INVALID_MODULE", vars);
		}

		Module relatedModule = optionalRelatedModule.get();
		if (fields.length == 2) {
			List<ModuleField> relatedModuleFields = relatedModule.getFields();
			Optional<ModuleField> optionalRelatedField = relatedModuleFields.stream()
					.filter(relatedField -> relatedField.getName().equals(fields[1])).findFirst();

			if (optionalRelatedField.isEmpty()) {
				String[] vars = { fields[1], relatedModule.getName() };
				throw new BadRequestException("FIELD_NOT_FOUND", vars);
			}
			ModuleField relatedfield = optionalRelatedField.get();
			if (relatedfield.getDataType().getDisplay().equals("Relationship")) {
				String[] vars = { fields[1] };
				throw new BadRequestException("FIELD_CANNOT_END_BY_RELATIONSHIP", vars);

			} else if (relatedfield.getDataType().getDisplay().equals("Formula")) {
				String[] vars = { fields[1] };
				throw new BadRequestException("DISPALY_TYPE_NOT_VALID_FOR_FORMULA", vars);

			} else if (relatedfield.getDataType().getDisplay().equals("List Formula")) {
				String[] vars = { fields[1] };
				throw new BadRequestException("DISPLAY_TYPE_NOT_VALID_FOR_LIST_FORMULA", vars);

			} else if (!displayDatatypes.contains(relatedfield.getDataType().getDisplay())) {
				String[] vars = { fields[1] };
				throw new BadRequestException("DISPALY_TYPE_NOT_VALID_FOR_FORMULA", vars);

			}
			return relatedfield.getDataType().getDisplay();
		}
		if (fields.length > 2) {
			return validateFormulaRecursively(relatedModule, modules, path.split(fields[0] + "\\.")[1],
					displayDatatypes);
		}
		return "";
	}

	public boolean validateBrackets(String s) {
		Stack<Character> stack = new Stack<Character>();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '[' || c == '(' || c == '{') {
				stack.push(c);
			} else if (c == ']') {
				if (stack.isEmpty() || stack.pop() != '[') {
					return false;
				}
			} else if (c == ')') {
				if (stack.isEmpty() || stack.pop() != '(') {
					return false;
				}
			} else if (c == '}') {
				if (stack.isEmpty() || stack.pop() != '{') {
					return false;
				}
			}
		}
		return stack.isEmpty();
	}

	public boolean validateCustomOperators(String section) {
		List<String> customOperators = List.of("BLANK_SPACE");
		if (customOperators.contains(section)) {
			return true;
		}
		return false;
	}

	public String getFormulaRecursively(String formula, Module module) {
		List<ModuleField> moduleFields = module.getFields();

		String reg = "\\{\\{(?i)(inputMessage[_a-zA-Z0-9\\.\\-]+)\\}\\}";
		Pattern pattern = Pattern.compile(reg);
		Matcher matcher = pattern.matcher(formula);
		while (matcher.find()) {
			String path = matcher.group(1).split("(?i)inputMessage\\.")[1];
			String[] fields = path.split("\\.");

			if (!validateCustomOperators(fields[0])) {

				ModuleField moduleField = moduleFields.stream().filter(field -> field.getName().equals(fields[0]))
						.findFirst().orElse(null);
				if (moduleField == null) {
					String[] vars = { fields[0], module.getName() };
					throw new BadRequestException("FIELD_NOT_FOUND", vars);
				}

				if (moduleField.getDataType().getDisplay().equals("Formula")) {
					String updatedFormula = getFormulaRecursively(moduleField.getFormula(), module);
					formula = formula.replaceAll("\\{\\{" + matcher.group(1) + "\\}\\}", "(" + updatedFormula + ")");
				}
			}
		}

		return formula;
	}

	private void isValidReceiptCapture(Module module, ModuleField moduleField) {
		if (moduleField.getDataType().getDisplay().equals("Receipt Capture")) {
			ModuleField receiptCapturefield = module.getFields().stream()
					.filter(field -> field.getDataType().getDisplay().equals("Receipt Capture")).findFirst()
					.orElse(null);
			if (receiptCapturefield != null && !receiptCapturefield.getName().equals(moduleField.getName())) {
				throw new BadRequestException("RECEIPT_CAPTURE_DATA_TYPE_EXIST", null);
			}
		}
	}

	public void isValidListFormula(Module module, ModuleField moduleField) {

		if (moduleField.getListFormula() == null || moduleField.getListFormula().isEmpty()) {
			throw new BadRequestException("LIST_FORMULA_EMPTY", null);
		}
		List<ListFormulaField> listFormulas = moduleField.getListFormula();

		ModuleField previousField = module.getFields().stream()
				.filter(field -> field.getFieldId().equals(moduleField.getFieldId())).findAny().orElse(null);
		
		List<String> formulaTypes = new ArrayList<String>();

		for (ListFormulaField listFormula : listFormulas) {
			if (listFormula.getFormula().isEmpty() || listFormula.getFormulaName().isEmpty()
					|| listFormula.getFormulaLabel().isEmpty()) {
				throw new BadRequestException("LIST_FORMULA_FIELDS_EMPTY", null);
			}
			isFormulaNameDuplicate(listFormula);
			if (previousField != null) {
				List<ListFormulaField> previousListFormulas = previousField.getListFormula();
				isFormulaNameChanged(listFormulas, previousListFormulas);
			}
			String formula = listFormula.getFormula();
			String type = isValidFormula(module, moduleField, formula);
			if(!formulaTypes.contains(type)) {
				formulaTypes.add(type);
			}
			
		}
		if(formulaTypes.size() >1) {
			throw new BadRequestException("MIXED_FORMULA_TYPES", null);
		}

	}

	public void isFormulaNameChanged(List<ListFormulaField> listFormulas, List<ListFormulaField> previousListFormulas) {

		List<String> listFormulasNames = new ArrayList<String>();
		List<String> previousListFormulasNames = new ArrayList<String>();
		listFormulas.forEach(field -> {
			listFormulasNames.add(field.getFormulaName());
		});
		previousListFormulas.forEach(field -> {
			previousListFormulasNames.add(field.getFormulaName());
		});
		if (previousListFormulasNames.size() > listFormulas.size()) {
			throw new BadRequestException("LIST_FORMULA_NAME_DELETED", null);
		} else if (previousListFormulasNames.size() < listFormulas.size()) {
			if (!listFormulasNames.containsAll(previousListFormulasNames)) {
				throw new BadRequestException("LIST_FORMULA_NAME_CHANGED", null);
			}
		} else {
			if (!listFormulasNames.equals(previousListFormulasNames)) {
				throw new BadRequestException("LIST_FORMULA_NAME_CHANGED", null);
			}
		}

	}

	public void isFormulaNameDuplicate(ListFormulaField listFormula) {

		List<String> listFormulasNames = new ArrayList<String>();
		if (!listFormulasNames.contains(listFormula.getFormulaName())) {
			listFormulasNames.add(listFormula.getFormulaName());
		} else {
			String[] vars = { listFormula.getFormulaName() };
			throw new BadRequestException("LIST_FORMULA_NAME_DUPLICATE", vars);
		}

	}

}
