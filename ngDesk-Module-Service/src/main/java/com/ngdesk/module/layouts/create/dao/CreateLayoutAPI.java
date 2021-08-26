package com.ngdesk.module.layouts.create.dao;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.springdoc.core.converters.PageableAsQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.module.dao.Module;
import com.ngdesk.module.dao.ModuleService;
import com.ngdesk.module.layout.dao.CreateEditLayout;
import com.ngdesk.module.layout.dao.LayoutService;
import com.ngdesk.module.role.dao.RoleService;
import com.ngdesk.repositories.CreatelayoutRepository;
import com.ngdesk.repositories.ModuleRepository;

import io.swagger.v3.oas.annotations.Parameter;

@RestController
public class CreateLayoutAPI {
	@Autowired
	CreatelayoutRepository createlayoutRepository;

	@Autowired
	AuthManager authManager;

	@Autowired
	ModuleRepository moduleRepository;

	@Autowired
	CreateLayoutService createlayoutService;

	@Autowired
	ModuleService moduleService;

	@Autowired
	RoleService roleService;

	@Autowired
	LayoutService layoutService;

	@Autowired
	MongoOperations mongoOperations;

	@PostMapping("/modules/{module_id}/create_layout")
	public CreateEditLayout postCreateLayout(@Valid @RequestBody CreateEditLayout createLayout,
			@PathVariable("module_id") String moduleId) {
		Module module = moduleService.validateAndGetModule(moduleId);

		roleService.isValidRoleId(createLayout.getRole(), "roles_" + authManager.getUserDetails().getCompanyId());

		roleService.isAuthorized(authManager.getUserDetails().getRole(), "POST", moduleId);

		createlayoutService.checkDuplicateLayoutForRole(module, createLayout.getRole());

		List<String> fieldIds = new ArrayList<String>();

		fieldIds = layoutService.getFieldIdsFromLayouts(createLayout, module);

		if (fieldIds.isEmpty()) {
			throw new BadRequestException("LAYOUT_FIELDS_EMPTY", null);
		}

		layoutService.isValidFieldId(fieldIds, module);
		layoutService.requiredFieldsValidation(fieldIds, module);

		// VALIDATION FOR FIELD PERMISSION FOR SELECTED FIELDS
		layoutService.validateFieldPermission(createLayout.getRole(), moduleId, module);

		createLayout = layoutService.initializeDefaultsForPostCall(createLayout);

		createlayoutRepository.saveCreateLayout("modules_" + authManager.getUserDetails().getCompanyId(), createLayout,
				moduleId);

		return createLayout;
	}

	@PutMapping("/modules/{module_id}/create_layout")
	public CreateEditLayout putCreateLayout(@RequestBody CreateEditLayout createLayout,
			@PathVariable("module_id") String moduleId) {
		Module module = moduleService.validateAndGetModule(moduleId);

		CreateEditLayout existingCreateLayout = createlayoutService
				.validateAndGetExistingCreateLayout(createLayout.getLayoutId(), module);

		roleService.isValidRoleId(createLayout.getRole(), "roles_" + authManager.getUserDetails().getCompanyId());

		roleService.isAuthorized(authManager.getUserDetails().getRole(), "PUT", moduleId);

		createlayoutService.duplicateCheckForPutCall(createLayout, module);

		List<String> fieldIds = new ArrayList<String>();

		fieldIds = layoutService.getFieldIdsFromLayouts(createLayout, module);

		layoutService.isValidFieldId(fieldIds, module);

		createLayout = layoutService.initializeDefaultsForPutCall(createLayout, existingCreateLayout);

		layoutService.validateFieldPermission(createLayout.getRole(), moduleId, module);

		createlayoutRepository.updateCreateLayout(createLayout, moduleId, createLayout.getLayoutId(),
				"modules_" + authManager.getUserDetails().getCompanyId());

		return createLayout;
	}

	@GetMapping("/modules/{module_id}/create_layouts")
	@PageableAsQueryParam
	public Page<CreateEditLayout> getAllCreateLayouts(@PathVariable("module_id") String moduleId,
			@Parameter(description = "Pageable object to control pagination", required = true, hidden = true) Pageable pageable) {

		moduleService.validateAndGetModule(moduleId);
		roleService.isAuthorized(authManager.getUserDetails().getRole(), "GET", moduleId);

		return createlayoutRepository.findAllCreateLayoutWithPagination(pageable, moduleId,
				authManager.getUserDetails().getCompanyId());
	}

	@GetMapping("/modules/{module_id}/create_layout/{layout_id}")
	public CreateEditLayout getOneCreateLayout(@PathVariable("module_id") String moduleId,
			@PathVariable("layout_id") String layoutId) {

		Module module = moduleService.validateAndGetModule(moduleId);

		roleService.isAuthorized(authManager.getUserDetails().getRole(), "GET", moduleId);

		return createlayoutService.validateAndGetExistingCreateLayout(layoutId, module);
	}

//	@DeleteMapping("modules/{module_id}/create_layout")
//	public void deleteCreateLayout(@PathVariable("module_id") String moduleId,
//
//			@RequestParam("layout_id") String layoutId, String collectionName) {
//
//		Module module = moduleService.validateAndGetModule(moduleId,
//				"modules_" + authManager.getUserDetails().getCompanyId());
//
//		createlayoutService.validateAndGetExistingCreateLayout(layoutId, module);
//
//		roleService.isAuthorized(authManager.getUserDetails().getRole(), "DELETE", moduleId);
//
//		createlayoutRepository.removeCreateLayout(moduleId, layoutId,
//				"modules_" + authManager.getUserDetails().getCompanyId());
//	}

}