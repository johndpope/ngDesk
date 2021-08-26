package com.ngdesk.report.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.report.module.dao.Module;
import com.ngdesk.report.module.dao.ModuleField;
import com.ngdesk.repositories.ModuleEntryRepository;
import com.ngdesk.repositories.ModulesRepository;

@Component
public class ReportService {

	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	AuthManager authManager;

	@Autowired
	private ModuleEntryRepository moduleEntryRepository;

	public void validateFilter(Report report, Module module) {
		for (Filter filter : report.getFilters()) {
			Optional<ModuleField> optional = module.getFields().stream()
					.filter(moduleField -> moduleField.getFieldId().equals(filter.getField().getFieldId())).findFirst();
			if (optional.isEmpty()) {
				throw new BadRequestException("FILTER_FILED_INVALID", null);
			}
			ModuleField filterField = module.getFields().stream()
					.filter(field -> field.getFieldId().equals(filter.getField().getFieldId())).findFirst().get();
			String displayDatatype = filterField.getDataType().getDisplay();
			if (displayDatatype.equalsIgnoreCase("Time Window")) {
				if (!filter.getOperator().equalsIgnoreCase("EQUALS_TO")) {
					throw new BadRequestException("FILTER_OPERATOR_INVALID", null);
				}
				String value = filter.getValue();
				String number = value.replaceAll("[^\\d]", "");
				if (!value.equalsIgnoreCase("days(current_date-" + number + ")")
						&& !value.equalsIgnoreCase("months(current_date-" + number + ")")) {
					throw new BadRequestException("FILTER_VALUE_INVALID", null);
				}
			}

		}
	}

	public String aggregationValue(Report report, Module module, String fieldName, String entryId, String companyId) {

		ModuleField currentField = module.getFields().stream().filter(field -> field.getName().equals(fieldName))
				.findFirst().get();

		if (currentField == null) {
			String[] vars = { module.getName() };
			throw new BadRequestException("FIELD_INVALID", vars);
		}

		ModuleField aggregationField = module.getFields().stream()
				.filter(field -> field.getFieldId().equals(currentField.getAggregationField())).findFirst().get();

		if (aggregationField == null) {
			throw new BadRequestException("INVALID_AGGREGATION_FIELD", null);
		}
		
		Module relatedModule = modulesRepository.findById(aggregationField.getModule(), "modules_" + companyId).get();

		if (relatedModule == null) {
			throw new BadRequestException("INVALID_RELATED_MODULE", null);
		}

		ModuleField aggregationRelatedField = relatedModule.getFields().stream()
				.filter(field -> field.getFieldId().equals(currentField.getAggregationRelatedField())).findFirst()
				.get();

		if (aggregationRelatedField == null) {
			throw new BadRequestException("INVALID_RELATED_AGGREGATION_FIELD", null);
		}
		System.out.println(currentField.getName());
		System.out.println(currentField.getAggregationType());

		ModuleField relationshipField = relatedModule.getFields().stream()
				.filter(field -> field.getFieldId().equals(aggregationField.getRelationshipField())).findFirst().get();
		if (relationshipField == null) {
			throw new BadRequestException("INVALID_RELATED_AGGREGATION_FIELD", null);
		}

		Map<String, Object> entry = moduleEntryRepository.findAllEntriesWithGivenValue(report,
				relationshipField.getName(), entryId, aggregationRelatedField.getName(),
				relatedModule.getName().replaceAll("\\s+", "_") + "_" + companyId);
		if (entry != null) {
			return entry.get(aggregationRelatedField.getName()).toString();
		} else {
			return null;
		}
	}

	public String relationshipValue(Map<String, ModuleField> relationFields, ReportField field, List<Module> modules,
			String companyId, Map<String, Object> document, String fieldName) {

		// RELATIONFIELD
		ModuleField relationField = relationFields.get(field.getFieldId());
		String primaryDisplayField = relationField.getPrimaryDisplayField();
		Module relatedModule = modules.stream()
				.filter(relationModule -> relationModule.getModuleId().equals(relationField.getModule())).findFirst()
				.orElse(null);
		List<ModuleField> relationModuleFields = relatedModule.getFields();
		String relationModuleName = relatedModule.getName();

		Optional<List<Map<String, Object>>> optionalRelatedModuleEntries = moduleEntryRepository
				.findAllEntries(relationModuleName.replaceAll("\\s+", "_") + "_" + companyId);
		Map<String, Object> relatedModuleEntry = new HashMap<String, Object>();
		if (optionalRelatedModuleEntries.isPresent()) {
			relatedModuleEntry = optionalRelatedModuleEntries.get().stream()
					.filter(entry -> entry.get("_id").toString().equals(document.get(fieldName).toString())).findFirst()
					.orElse(null);
		}
		String rowValue = null;

		for (ModuleField relatedField : relationModuleFields) {
			if (relatedField.getFieldId().equals(primaryDisplayField)) {
				rowValue = relatedModuleEntry.get(relatedField.getName()).toString();
				break;
			}
		}
		return rowValue;

	}
}
