package com.ngdesk.module.field.dao;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.validation.Valid;

import org.springdoc.core.converters.PageableAsQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.exceptions.ForbiddenException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.module.dao.Module;
import com.ngdesk.module.dao.ModuleService;
import com.ngdesk.module.elastic.dao.ElasticService;
import com.ngdesk.module.role.dao.RoleService;
import com.ngdesk.repositories.FieldRepository;
import com.ngdesk.repositories.ModuleRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
public class FieldAPI {

	@Autowired
	FieldValidator fieldValidator;

	@Autowired
	AuthManager authManager;

	@Autowired
	ModuleRepository moduleRepository;

	@Autowired
	ElasticService elasticService;

	@Autowired
	FieldRepository fieldsRepository;

	@Autowired
	RelationshipFieldService relationshipFieldService;

	@Autowired
	RoleService roleService;

	@Autowired
	DefaultValueValidator defaultValueValidator;

	@Autowired
	ModuleFieldService moduleFieldService;

	@Autowired
	ModuleService moduleService;

	@Autowired
	FieldAPI fieldApi;

	@Transactional
	@Operation(summary = "Post a relationship field to the module", description = "API to post a relationship field")
	@PostMapping("/modules/{module_id}/relationship/field")
	public RelationshipField postRelationshipField(@Valid @RequestBody RelationshipField relationshipField,
			@Parameter(description = "Module ID", required = true) @PathVariable("module_id") String moduleId) {

		if (!roleService.isSystemAdmin()) {
			throw new ForbiddenException("FORBIDDEN");
		}

		String companyId = authManager.getUserDetails().getCompanyId();

		Optional<Module> optionalModule = moduleRepository.findById(moduleId, "modules_" + companyId);
		if (optionalModule.isEmpty()) {
			String[] vars = { "MODULE" };
			throw new BadRequestException("DAO_NOT_FOUND", vars);
		}

		ModuleField field1 = relationshipField.getField();
		ModuleField field2 = relationshipField.getRelatedField();

		relationshipFieldService.validateDatatype(field1, field2);
		relationshipFieldService.validateRelationshipModules(field1, field2);

		Optional<Module> optionalRelatedModule = moduleRepository.findById(field1.getModule(), "modules_" + companyId);
		if (optionalRelatedModule.isEmpty()) {
			throw new BadRequestException("RELATED_MODULE_NOT_FOUND", null);
		}

		Module module = optionalModule.get();
		Module relatedModule = optionalRelatedModule.get();

		ModuleField primaryDisplayField = relatedModule.getFields().stream()
				.filter(field -> field.getFieldId().equals(field1.getPrimaryField())).findFirst().orElse(null);
		if (primaryDisplayField == null) {
			String[] vars = { field1.getName() };
			throw new BadRequestException("PRIMARY_DISPLAY_FIELD_REQUIRED", vars);
		}

		ModuleField relatedPrimaryDisplayField = module.getFields().stream()
				.filter(field -> field.getFieldId().equals(field2.getPrimaryField())).findFirst().orElse(null);
		if (relatedPrimaryDisplayField == null) {
			String[] vars = { field2.getName() };
			throw new BadRequestException("PRIMARY_DISPLAY_FIELD_REQUIRED", vars);
		}

		field1.setInternal(false);
		field2.setInternal(false);

		field1.setDateCreated(new Date());
		field1.setDateUpdated(new Date());
		field1.setCreatedBy(authManager.getUserDetails().getUserId());
		field1.setLastUpdatedBy(authManager.getUserDetails().getUserId());

		field2.setDateCreated(new Date());
		field2.setDateUpdated(new Date());
		field2.setCreatedBy(authManager.getUserDetails().getUserId());
		field2.setLastUpdatedBy(authManager.getUserDetails().getUserId());

		ModuleField duplicateField = module.getFields().stream()
				.filter(field -> field.getName().equals(field1.getName())).findFirst().orElse(null);
		if (duplicateField != null) {
			String[] vars = { field1.getName() };
			throw new BadRequestException("DUPLICATE_FIELD_NAME", vars);
		}

		ModuleField duplicateRelatedField = relatedModule.getFields().stream()
				.filter(field -> field.getName().equals(field2.getName())).findFirst().orElse(null);
		if (duplicateRelatedField != null) {
			String[] vars = { field2.getName() };
			throw new BadRequestException("DUPLICATE_FIELD_NAME", vars);
		}

		relationshipFieldService.validateFieldsSize(module);
		relationshipFieldService.validateFieldsSize(relatedModule);
		relationshipFieldService.validateRelationshipType(field1);

		String fieldId = UUID.randomUUID().toString();
		String relatedFieldId = UUID.randomUUID().toString();
		field1.setFieldId(fieldId);
		field2.setFieldId(relatedFieldId);
		field1.setRelationshipField(relatedFieldId);
		field2.setRelationshipField(fieldId);

		if (field1.getRelationshipType().equals("One to Many")) {
			field2.setRelationshipType("Many to One");
			field1.getDataType().setBackend("Array");
			field2.getDataType().setBackend("String");
		} else if (field1.getRelationshipType().equals("One to One")) {
			field2.setRelationshipType("One to One");
			field1.getDataType().setBackend("String");
			field2.getDataType().setBackend("String");
		} else if (field1.getRelationshipType().equals("Many to Many")) {
			field2.setRelationshipType("Many to Many");
			field1.getDataType().setBackend("Array");
			field2.getDataType().setBackend("Array");
		}

		relationshipFieldService.validateInheritanceMapping(relatedModule, field2);

		fieldApi.saveField(field1, module.getModuleId());
		fieldApi.saveField(field2, relatedModule.getModuleId());

		roleService.updateRolesWithNewField(module, field1);
		roleService.updateRolesWithNewField(relatedModule, field2);

		int field1size = moduleFieldService.getSizeForElastic(field1, module);
		elasticService.putMappingForNewField(authManager.getUserDetails().getCompanyId(), module.getModuleId(),
				field1.getName(), field1size + 1);

		int field2size = moduleFieldService.getSizeForElastic(field2, relatedModule);
		elasticService.putMappingForNewField(authManager.getUserDetails().getCompanyId(), relatedModule.getModuleId(),
				field2.getName(), field2size + 1);

		moduleService.publishToGraphql(authManager.getUserDetails().getCompanySubdomain());

		return relationshipField;
	}

	@Transactional
	@PostMapping("modules/{module_id}/field")
	public ModuleField postField(@Valid @RequestBody ModuleField moduleField,
			@PathVariable("module_id") String moduleId) {
		System.out.println(moduleField);
		if (!roleService.isSystemAdmin()) {
			throw new ForbiddenException("FORBIDDEN");
		}

		if (moduleField.getDataType().getDisplay().equals("Relationship")) {
			throw new BadRequestException("RELATIONSHIP_FIELDS_FORBIDDEN", null);
		}

		Module module = moduleService.validateAndGetModule(moduleId);

		fieldValidator.isValidFieldSize(module);

		String fName = moduleField.getName();
		fName = fName.trim();
		fName = fName.toUpperCase().replaceAll("\\s+", "_");
		moduleField.setName(fName);

		String fieldName = moduleField.getName();

		fieldValidator.checkForRestrictedName(fieldName);

		fieldValidator.isValidFieldName(fieldName);

		ModuleField duplicateField = module.getFields().stream().filter(field -> field.getName().equals(fieldName))
				.findFirst().orElse(null);
		if (duplicateField != null) {
			String[] vars = { moduleField.getName() };
			throw new BadRequestException("DUPLICATE_FIELD_NAME", vars);
		}

		// INITIALIZE DATE_CREATED, DATE_UPDATED, LAST_UPDATED_BY, CREATED_BY, INTERNAL
		moduleField = moduleFieldService.initializeFieldWithDefaults(moduleField);

		moduleField = fieldValidator.isValidField(moduleField, module);
		fieldValidator.isValidDataType(moduleField.getDataType());
		fieldValidator.isValidDispayAndBackendDataType(moduleField.getDataType());
		fieldValidator.validateZoomDataType(moduleField, module);

		defaultValueValidator.isValidDefaultValue(moduleField, module);

		if (moduleField.getDataType().getDisplay().equals("Address")) {
			moduleFieldService.createDefaultAddressFields(moduleField, module);
		} else {
			fieldApi.saveField(moduleField, moduleId);

			int size = moduleFieldService.getSizeForElastic(moduleField, module);

			elasticService.putMappingForNewField(authManager.getUserDetails().getCompanyId(), moduleId,
					moduleField.getName(), size + 1);

		}

		// For generating auto number
		fieldValidator.generateAutoNumberForExistingEntries(moduleField, module,
				authManager.getUserDetails().getCompanyId());
		moduleService.publishToGraphql(authManager.getUserDetails().getCompanySubdomain());
		if (!moduleField.getDataType().getDisplay().equals("Address")) {
			roleService.updateRolesWithNewField(module, moduleField);
		}

		return moduleField;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void saveField(ModuleField moduleField, String moduleId) {
		fieldsRepository.saveField("modules_" + authManager.getUserDetails().getCompanyId(), moduleField, moduleId);
	}

	@Transactional
	@PutMapping("modules/{module_id}/field")
	public ModuleField updateField(@Valid @RequestBody ModuleField moduleField,
			@PathVariable("module_id") String moduleId) {

		if (!roleService.isSystemAdmin()) {
			throw new ForbiddenException("FORBIDDEN");
		}

		Module module = moduleService.validateAndGetModule(moduleId);

		roleService.isAuthorized(authManager.getUserDetails().getRole(), "PUT", moduleId);

		String fieldId = moduleField.getFieldId();

		ModuleField existingField = module.getFields().stream().filter(field -> field.getFieldId().equals(fieldId))
				.findFirst().orElse(null);
		if (existingField == null) {
			throw new BadRequestException("FIELD_MISSING", null);
		}
		if (existingField.getInternal() != null) {
			if (existingField.getInternal()) {
				throw new BadRequestException("INTERNAL_FIELD_NOT_EDITABLE", null);
			}
		}

		if (!existingField.getDataType().getDisplay().equals(moduleField.getDataType().getDisplay())) {
			throw new BadRequestException("DATA_TYPE_MODIFIED", null);
		}
		fieldValidator.isValidDataType(moduleField.getDataType());
		fieldValidator.isValidDispayAndBackendDataType(moduleField.getDataType());

		moduleField.setDateUpdated(new Date());
		moduleField.setLastUpdatedBy(authManager.getUserDetails().getUserId());

		moduleField.setCreatedBy(existingField.getCreatedBy());
		moduleField.setDateCreated(existingField.getDateCreated());

		if (moduleField.getDataType().getDisplay().equals("Relationship")) {
			relationshipFieldService.validateInheritanceMapping(module, moduleField);
		} else {
			moduleField = fieldValidator.isValidField(moduleField, module);
		}

		defaultValueValidator.isValidDefaultValue(moduleField, module);

		if (moduleField.getDataFilter() != null) {
			moduleFieldService.postDataFilter(moduleField);
			moduleField.setDataFilter(null);
		}

		fieldsRepository.removeField(moduleId, existingField.getFieldId(),
				"modules_" + authManager.getUserDetails().getCompanyId());
		fieldsRepository.saveField("modules_" + authManager.getUserDetails().getCompanyId(), moduleField, moduleId);

		return moduleField;
	}

	@GetMapping("/{module_id}/field/{field_id}")
	public ModuleField getOneField(@PathVariable("module_id") String moduleId,
			@PathVariable("field_id") String fieldId) {
		
		Optional<Module> optionalField = moduleRepository.findById(moduleId,
				"modules_" + authManager.getUserDetails().getCompanyId());
		List<ModuleField> allFields = optionalField.get().getFields();
		System.out.println(allFields);
		ModuleField fields = allFields.stream().filter(ModuleField -> ModuleField.getFieldId().equals(fieldId))
				.findAny().get();
		
		System.out.println("All the fields getting --------> "+fields);
		return fields;

	}

	@GetMapping("/{module_id}/fields")
	@PageableAsQueryParam
	public Page<ModuleField> getAllFields(@PathVariable("module_id") String moduleId,
			@Parameter(description = "Pageable object to control pagination", required = true, hidden = true) Pageable pageable) {

		return fieldsRepository.findAllFieldsWithPagenation(pageable, moduleId,
				authManager.getUserDetails().getCompanyId());

	}
}
