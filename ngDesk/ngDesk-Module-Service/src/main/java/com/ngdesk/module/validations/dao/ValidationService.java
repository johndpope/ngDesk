package com.ngdesk.module.validations.dao;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.module.dao.Module;
import com.ngdesk.module.dao.ModuleService;
import com.ngdesk.module.field.dao.ModuleField;
import com.ngdesk.repositories.ModuleEntryRepository;
import com.ngdesk.repositories.ModuleRepository;

@Service
public class ValidationService {

	@Autowired
	ModuleRepository moduleRepository;

	@Autowired
	ModuleService moduleService;

	@Autowired
	AuthManager authManager;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	public ModuleValidation validateAndGetModuleValidation(String validationId, Module module) {
		Optional<ModuleValidation> optionalModuleValidation = module.getValidations().stream()
				.filter(validation -> validation.getValidationId().equalsIgnoreCase(validationId)).findFirst();

		if (optionalModuleValidation.isEmpty()) {
			String[] vars = { "MODULE_VALIDATION" };
			throw new BadRequestException("DAO_NOT_FOUND", vars);
		}

		return optionalModuleValidation.get();
	}

	public void duplicateNameCheck(String validationName, Module module) {
		Optional<ModuleValidation> optionalValidation = module.getValidations().stream()
				.filter(validation -> validation.getName().equalsIgnoreCase(validationName)).findFirst();

		if (optionalValidation.isPresent()) {
			String[] vars = { "VALIDATION", "NAME" };
			throw new BadRequestException("DAO_VARIABLE_ALREADY_EXISTS", vars);
		}
	}

	public void isValidCondition(ModuleValidation moduleValidation, Module module) {

		for (Validation validation : moduleValidation.getValidations()) {
			Optional<ModuleField> optionalModuleField = module.getFields().stream()
					.filter(field -> field.getFieldId().equalsIgnoreCase(validation.getCondition())).findFirst();
			if (optionalModuleField.isEmpty()) {
				throw new BadRequestException("CONDITION_FIELD_NOT_FOUND", null);
			}
		}

	}

	public void isValidOperator(ModuleValidation moduleValidation, Module module) {

		List<String> validNumericOperators = List.of("GREATER_THAN", "LESS_THAN", "EQUALS_TO", "NOT_EQUALS_TO");
		List<String> validDateOperators = List.of("GREATER_THAN", "LESS_THAN", "EXISTS", "DOES_NOT_EXIST");
		List<String> validRelationOperators = List.of("EQUALS_TO", "NOT_EQUALS_TO");

		List<String> relationshipManyToMany = new ArrayList<String>();
		Hashtable<String, String> fieldsToDataTypeMap = new Hashtable<String, String>();

		module.getFields().forEach(field -> {
			fieldsToDataTypeMap.put(field.getFieldId(), field.getDataType().getDisplay());
			if (field.getRelationshipType() != null && field.getRelationshipType().equalsIgnoreCase("Many to Many")) {
				relationshipManyToMany.add(field.getFieldId());
			}
		});

		for (Validation validation : moduleValidation.getValidations()) {
			String conditionFieldId = validation.getCondition();
			String conditionOperator = validation.getOperator();
			String dataTypeOfConditionField = fieldsToDataTypeMap.get(conditionFieldId);

			String[] vars = { dataTypeOfConditionField };

			if (validation.getOperator().equalsIgnoreCase("IS_UNIQUE")
					&& dataTypeOfConditionField.equalsIgnoreCase("DISCUSSION")) {
				throw new BadRequestException("INVALID_OPERATOR_FOR_DATA_TYPE", vars);
			}
			if (dataTypeOfConditionField.equalsIgnoreCase("Number")) {
				if (!validNumericOperators.contains(conditionOperator)) {
					throw new BadRequestException("INVALID_OPERATOR_FOR_DATA_TYPE", vars);
				}
			} else if (dataTypeOfConditionField.equalsIgnoreCase("Date/Time")
					|| dataTypeOfConditionField.equalsIgnoreCase("Date")
					|| dataTypeOfConditionField.equalsIgnoreCase("Time")) {
				if (!validDateOperators.contains(conditionOperator)) {
					throw new BadRequestException("INVALID_OPERATOR_FOR_DATA_TYPE", vars);
				}
			} else if (dataTypeOfConditionField.equalsIgnoreCase("Checkbox")
					|| (dataTypeOfConditionField.equals("Relationship")
							&& !relationshipManyToMany.contains(conditionFieldId))) {
				if (!validRelationOperators.contains(conditionOperator)) {
					throw new BadRequestException("INVALID_OPERATOR_FOR_DATA_TYPE", vars);
				}
			} else if (relationshipManyToMany.contains(conditionFieldId)
					|| dataTypeOfConditionField.equalsIgnoreCase("List Text")) {
				if (conditionOperator.equals("EQUALS_TO") || conditionOperator.equals("NOT_EQUALS_TO")) {
					throw new BadRequestException("INVALID_OPERATOR_FOR_DATA_TYPE", vars);
				}
			}
		}
	}

	public void isValidRelationshipValue(ModuleValidation moduleValidation, Module module) {
		for (Validation validation : moduleValidation.getValidations()) {
			ModuleField moduleField = module.getFields().stream()
					.filter(field -> field.getFieldId().equalsIgnoreCase(validation.getCondition())
							&& field.getDataType().getDisplay().equalsIgnoreCase("Relationship"))
					.findFirst().orElse(null);
			if (moduleField == null) {
				continue;
			}
			Optional<Module> optionalRelatedModule = moduleRepository.findById(moduleField.getModule(),
					"modules_" + authManager.getUserDetails().getCompanyId());
			if (optionalRelatedModule.isEmpty()) {
				String[] vars = { moduleField.getName() };
				throw new BadRequestException("RELATIONSHIP_MODULE_INVALID", vars);
			}

			Module relatedModule = optionalRelatedModule.get();

			String collectionName = moduleService.getCollectionName(relatedModule.getName(),
					authManager.getUserDetails().getCompanyId());

			Optional<Map<String, Object>> optionalEntry = moduleEntryRepository
					.findEntryById(validation.getConditionValue(), collectionName);

			if (optionalEntry.isEmpty()) {
				String[] vars = { moduleField.getDisplayLabel() };
				throw new BadRequestException("RELATIONSHIP_VALUE_NOT_FOUND", vars);
			}
		}
	}

}
