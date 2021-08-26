package com.ngdesk.module.slas.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.module.dao.Module;
import com.ngdesk.module.field.dao.DataType;
import com.ngdesk.module.field.dao.ModuleField;
import com.ngdesk.module.task.dao.Relationship;
import com.ngdesk.repositories.ModuleRepository;
import com.ngdesk.repositories.SLARepository;
import com.ngdesk.workflow.dao.Workflow;

@Component
public class SLAsBeforeSaveListener extends AbstractMongoEventListener<SLA> {

	@Autowired
	ModuleRepository moduleRepository;

	@Autowired
	SLARepository slaRepository;

	@Override
	public void onBeforeConvert(BeforeConvertEvent<SLA> event) {

		SLA sla = event.getSource();
		checkValidModuleId(sla);
		checkForDuplicateName(sla);
		checkValidationForViolation(sla);
		checkValidationForWorkflowId(sla);
		checkValidationForSlaExpiry(sla);
		checkValidationForBusinessRestrictions(sla);
		checkValidationforRecurrence(sla);

	}

	private void checkValidModuleId(SLA sla) {

		String moduleId = sla.getModuleId();
		Optional<Module> optionalModule = moduleRepository.findById(moduleId, "modules_" + sla.getCompanyId());
		if (optionalModule.isEmpty()) {
			throw new BadRequestException("MODULE_ID_INVALID", null);
		}

	}

	private void checkForDuplicateName(SLA sla) {

		if (sla.getSlaId() == null) {
			Optional<SLA> optionalSla = slaRepository.findDuplicateSlaName(sla.getName(), sla.getCompanyId(),
					sla.getModuleId());
			if (optionalSla.isPresent()) {
				throw new BadRequestException("SLA_NAME_ALREADY_EXISTS", null);
			}
		} else {
			Optional<SLA> optionalSla = slaRepository.findOtherSlaWithDuplicateName(sla.getName(), sla.getCompanyId(),
					sla.getSlaId(), sla.getModuleId());
			if (optionalSla.isPresent()) {
				throw new BadRequestException("SLA_NAME_ALREADY_EXISTS", null);
			}
		}

	}

	private void checkValidationForViolation(SLA sla) {

		String moduleId = sla.getModuleId();
		Optional<Module> optionalModule = moduleRepository.findById(moduleId, "modules_" + sla.getCompanyId());
		Module module = optionalModule.get();
		List<ModuleField> moduleFields = module.getFields();
		List<String> fieldIds = new ArrayList<String>();
		List<String> notSupportedViolationFields = new ArrayList<String>();
		List<String> discussionFields = new ArrayList<String>();
		Map<String, Object> fieldToDataTypeMap = new HashMap<String, Object>();
		Violation violation = sla.getViolation();
		List<String> chronometerFieldIds = new ArrayList<String>();
		for (ModuleField moduleField : moduleFields) {
			String fieldId = moduleField.getFieldId();
			fieldIds.add(fieldId);
			DataType datatype = moduleField.getDataType();
			fieldToDataTypeMap.put(fieldId, moduleField.getDisplayLabel());
			String dataType = datatype.getDisplay();
			if (dataType.equals("Chronometer")) {
				chronometerFieldIds.add(moduleField.getFieldId());
			}
			if (dataType.equalsIgnoreCase("Auto Number") || dataType.equalsIgnoreCase("Picklist (Multi-Select)")) {
				notSupportedViolationFields.add(fieldId);
			} else if (dataType.equalsIgnoreCase("Relationship")) {
				String relationshipType = moduleField.getRelationshipType();
				if (relationshipType.equalsIgnoreCase("one to many")
						|| relationshipType.equalsIgnoreCase("many to many")) {
					notSupportedViolationFields.add(fieldId);
				}
			} else if (dataType.equalsIgnoreCase("Discussion")) {
				discussionFields.add(fieldId);
			}

			if (dataType.equalsIgnoreCase("Picklist") && violation.getCondition().equals(fieldId)
					&& violation.getOperator().equalsIgnoreCase("HAS_BEEN")) {
				List<String> picklistValues = (List<String>) moduleField.getPicklistValues();
				if (!picklistValues.contains(violation.getConditionValue())) {
					throw new BadRequestException("PICKLIST_VALUE_INVALID", null);
				}
			}
		}
		String companyId = sla.getCompanyId();
		isValidFieldAndCondition(violation.getCondition(), fieldIds, notSupportedViolationFields, discussionFields,
				violation.getOperator(), violation.getConditionValue(), fieldToDataTypeMap);
		if (discussionFields.contains(violation.getCondition())) {
			teamExists(violation.getConditionValue(), companyId);
		}

	}

	private void teamExists(String value, String companyId) {
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			List<SLARelationship> values = mapper.readValue(value,
					mapper.getTypeFactory().constructCollectionType(List.class, SLARelationship.class));
			List<String> dataIds = new ArrayList<String>();

			values.forEach(relationShipValue -> {
				map.put(relationShipValue.getDataId(), relationShipValue.getName());
				dataIds.add(relationShipValue.getDataId());

			});
			for (String dataId : dataIds) {
				String name = map.get(dataId).toString();
				Optional<Map<String, Object>> optionalTeam = slaRepository.findTeamByTeamId(dataId, name,
						"Teams_" + companyId);
				if (!optionalTeam.isPresent()) {
					String[] vars = { name };
					throw new BadRequestException("TEAM_DOES_NOT_EXIST", vars);
				}

			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void isValidFieldAndCondition(String conditionFieldId, List<String> fieldIds,
			List<String> notSupportedFields, List<String> discussionFields, String operator, String conditionValue,
			Map<String, Object> fieldToDataTypeMap) {

		if (discussionFields != null) {
			if (!fieldIds.contains(conditionFieldId)) {
				throw new BadRequestException("FIELD_DOES_NOT_EXIST", null);

			} else if (notSupportedFields.contains(conditionFieldId)) {
				String[] vars = { fieldToDataTypeMap.get(conditionFieldId).toString() };
				throw new BadRequestException("RESTRICTED_SLA_FIELD", vars);

			} else if (!discussionFields.contains(conditionFieldId)
					&& operator.equalsIgnoreCase("HAS_NOT_BEEN_REPLIED_BY")) {
				String[] vars = { fieldToDataTypeMap.get(conditionFieldId).toString() };
				throw new BadRequestException("INVALID_OPERATOR_SLA", vars);

			} else if (discussionFields.contains(conditionFieldId)
					&& !operator.equalsIgnoreCase("HAS_NOT_BEEN_REPLIED_BY")) {
				String[] vars = { fieldToDataTypeMap.get(conditionFieldId).toString() };
				throw new BadRequestException("INVALID_OPERATOR_SLA", vars);
			}
		} else if (!fieldIds.contains(conditionFieldId)) {
			throw new BadRequestException("FIELD_DOES_NOT_EXIST", null);

		} else if (notSupportedFields.contains(conditionFieldId)) {
			String[] vars = { fieldToDataTypeMap.get(conditionFieldId).toString() };
			throw new BadRequestException("RESTRICTED_SLA_FIELD", vars);
		}

	}

	private void checkValidationForWorkflowId(SLA sla) {

		Optional<Workflow> optionalWorkflow = slaRepository.findWorkflowByModuleIdAndWorkflowId(sla.getWorkflow(),
				sla.getModuleId(), sla.getCompanyId());
		if (optionalWorkflow.isEmpty()) {
			throw new BadRequestException("INVALID_SLA_WORKFLOW", null);
		}

	}

	private void checkValidationForSlaExpiry(SLA sla) {

		if (sla.getSlaExpiry() < 0) {
			throw new BadRequestException("INVALID_SLA_EXPIRY", null);
		}

	}

	private void checkValidationForBusinessRestrictions(SLA sla) {

		if (sla.getIsRestricted() != null && sla.getIsRestricted() == true) {
			if (sla.getBusinessRules() == null) {
				throw new BadRequestException("BUSINESS_RESTRICTIONS_REQUIRED", null);
			}
			SLABusinessRules businessRules = sla.getBusinessRules();
			if (businessRules.getRestrictionType() == null) {
				throw new BadRequestException("RESTRICTION_TYPE_REQUIRED", null);
			}
			if (businessRules.getRestrictions() == null) {
				throw new BadRequestException("RESTRICTIONS_REQUIRED", null);
			}
			String restrictionType = businessRules.getRestrictionType();
			List<SLARestriction> restrictions = businessRules.getRestrictions();
			if (restrictionType.equalsIgnoreCase("Day")) {
				for (SLARestriction restriction : restrictions) {
					if (restriction.getStartTime() == null || restriction.getEndTime() == null) {
						throw new BadRequestException("START_TIME_AND_END_TIME_REQUIRED", null);
					}
				}
			} else if (restrictionType.equalsIgnoreCase("Week")) {
				for (SLARestriction restriction : restrictions) {
					if (restriction.getStartTime() == null || restriction.getEndTime() == null) {
						throw new BadRequestException("START_TIME_AND_END_TIME_REQUIRED", null);
					} else if (restriction.getStartDay() == null || restriction.getEndDay() == null) {
						throw new BadRequestException("START_DAY_AND_END_DAY_REQUIRED", null);
					}
				}
			}

		}

	}

	private void checkValidationforRecurrence(SLA sla) {

		if (sla.getIsRecurring() != null && sla.getIsRecurring() == true) {
			if (sla.getRecurrence() != null) {
				Recurrence recurrence = sla.getRecurrence();
				if (recurrence.getIntervalTime() == null) {
					throw new BadRequestException("INTERVAL_TIME_REQUIRED", null);
				} else if (recurrence.getMaxRecurrence() == null) {
					throw new BadRequestException("MAX_RECURRENCE_REQUIRED", null);
				}
			} else {
				throw new BadRequestException("MAX_RECURRENCE_REQUIRED_AND_INTERVAL_REQUIRED", null);
			}
		}

	}
}
