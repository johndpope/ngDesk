package com.ngdesk.module.validations.dao;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ngdesk.commons.exceptions.ForbiddenException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.module.dao.Module;
import com.ngdesk.module.dao.ModuleService;
import com.ngdesk.module.role.dao.RoleService;
import com.ngdesk.repositories.ModuleValidationRepository;

@RestController
public class ValidationAPI {

	@Autowired
	ModuleService moduleService;

	@Autowired
	RoleService roleService;

	@Autowired
	AuthManager authManager;

	@Autowired
	ValidationService validationService;

	@Autowired
	ModuleValidationRepository moduleValidationRepository;

	@PostMapping("/modules/{module_id}/validation")
	public ModuleValidation postValidations(@RequestBody @Valid ModuleValidation moduleValidation,
			@PathVariable("module_id") String moduleId) {
		Module module = moduleService.validateAndGetModule(moduleId);

		if (!roleService.isSystemAdmin()) {
			throw new ForbiddenException("FORBIDDEN");
		}

		for (String roleId : moduleValidation.getRoles()) {
			roleService.isValidRoleId(roleId, "roles_" + authManager.getUserDetails().getCompanyId());
		}
		validationService.duplicateNameCheck(moduleValidation.getName(), module);
		validationService.isValidCondition(moduleValidation, module);
		validationService.isValidOperator(moduleValidation, module);
		validationService.isValidRelationshipValue(moduleValidation, module);

		moduleValidationRepository.saveModuleValidation(moduleValidation, moduleId,
				"modules_" + authManager.getUserDetails().getCompanyId());

		return moduleValidation;
	}

	@PutMapping("/modules/{module_id}/validations")
	public ModuleValidation putValidations(@RequestBody @Valid ModuleValidation moduleValidation,
			@PathVariable("module_id") String moduleId) {

		Module module = moduleService.validateAndGetModule(moduleId);

		if (!roleService.isSystemAdmin()) {
			throw new ForbiddenException("FORBIDDEN");
		}

		for (String roleId : moduleValidation.getRoles()) {
			roleService.isValidRoleId(roleId, "roles_" + authManager.getUserDetails().getCompanyId());
		}

		validationService.validateAndGetModuleValidation(moduleValidation.getValidationId(), module);

		// TODO: CHECK DUPLICATE

		validationService.duplicateNameCheck(moduleValidation.getName(), module);
		validationService.isValidCondition(moduleValidation, module);
		validationService.isValidOperator(moduleValidation, module);
		validationService.isValidRelationshipValue(moduleValidation, module);

		return moduleValidation;
	}

}
