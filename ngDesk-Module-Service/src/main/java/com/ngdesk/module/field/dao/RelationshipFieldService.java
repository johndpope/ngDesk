package com.ngdesk.module.field.dao;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.module.dao.Module;
import com.ngdesk.repositories.ModuleRepository;

@Service
public class RelationshipFieldService {

	@Autowired
	ModuleRepository moduleRepository;

	@Autowired
	AuthManager authManager;

	public void validateDatatype(ModuleField field1, ModuleField field2) {

		if (!field1.getDataType().getDisplay().equals("Relationship")) {
			String[] vars = { field1.getName() };
			throw new BadRequestException("RELATIONSHIP_FIELDS_ONLY", vars);
		}
		if (!field2.getDataType().getDisplay().equals("Relationship")) {
			String[] vars = { field2.getName() };
			throw new BadRequestException("RELATIONSHIP_FIELDS_ONLY", vars);
		}

	}

	public void validateRelationshipModules(ModuleField field1, ModuleField field2) {
		if (field1.getModule() == null) {
			throw new BadRequestException("MODULE_NOT_FOUND", null);
		}
		if (field2.getModule() == null) {
			throw new BadRequestException("RELATED_MODULE_NOT_FOUND", null);
		}
	}

	public void validateFieldsSize(Module module) {
		if (module.getFields().size() >= 100) {
			String[] vars = { module.getName() };
			throw new BadRequestException("FIELDS_SIZE_EXCEEDED", vars);
		}
	}

	public void validateRelationshipType(ModuleField field) {
		List<String> relationshipTypes = Arrays.asList("One to Many", "One to One", "Many to Many");
		if (field.getRelationshipType() == null || !relationshipTypes.contains(field.getRelationshipType())) {
			String[] vars = { field.getName() };
			throw new BadRequestException("RELATIONSHIP_TYPE_REQUIRED", vars);
		}
	}

	public void validateInheritanceMapping(Module module, ModuleField field) {
		String companyId = authManager.getUserDetails().getCompanyId();
		if (field.getInheritanceMapping() != null) {

			if (!field.getRelationshipType().equals("One to One")
					&& !field.getRelationshipType().equals("Many to One")) {
				throw new BadRequestException("INHERITANCE_MAPPING_RELATIONSHIP_TYPE_INVALID", null);
			}

			Map<String, String> inheritanceMapping = field.getInheritanceMapping();

			if (field.getModule() == null) {
				throw new BadRequestException("RELATED_MODULE_NOT_FOUND", null);
			}
			Optional<Module> optionalRelatedModule = moduleRepository.findById(field.getModule(),
					"modules_" + companyId);
			if (optionalRelatedModule.isEmpty()) {
				throw new BadRequestException("RELATED_MODULE_NOT_FOUND", null);
			}

			Module relatedModule = optionalRelatedModule.get();

			for (String key : inheritanceMapping.keySet()) {

				ModuleField fieldLhs = relatedModule.getFields().stream()
						.filter(moduleField -> moduleField.getFieldId().equals(key)).findFirst().orElse(null);
				if (fieldLhs == null) {
					throw new BadRequestException("INHERITANCE_FIELD_NOT_FOUND", null);
				}

				ModuleField fieldRhs = module.getFields().stream()
						.filter(moduleField -> moduleField.getFieldId().equals(inheritanceMapping.get(key))).findFirst()
						.orElse(null);
				if (fieldRhs == null) {
					throw new BadRequestException("INHERITANCE_FIELD_NOT_FOUND", null);
				}

				if (fieldLhs.getDataType().getDisplay().equals("Relationship")
						|| fieldRhs.getDataType().getDisplay().equals("Relationship")) {
					throw new BadRequestException("RELATIONSHIP_PROHIBITED_INHERITANCE", null);
				}
			}
		}
	}

	public void validatePrimaryDisplayField(ModuleField field, Module relatedModule) {
		String primaryDisplayField = field.getPrimaryField();
		Optional<ModuleField> optionalField = relatedModule.getFields().stream()
				.filter(moduleField -> moduleField.getFieldId().equals(primaryDisplayField)).findFirst();

		List<String> notAllowedPrimaryTypes = List.of("Relationship", "Phone", "Date", "Date/Time", "Time", "Button",
				"Time Window", "Approval", "Zoom", "Password", "File Upload", "Aggregate", "File Preview");
		if (optionalField.isPresent()) {
			ModuleField relatedField = optionalField.get();
			if (notAllowedPrimaryTypes.contains(relatedField.getDataType().getDisplay())) {
				String[] vars = { relatedField.getName() };
				throw new BadRequestException("INVALID_PRIMARY_DISPLAY_FIELD", vars);
			}
		}

	}

}
