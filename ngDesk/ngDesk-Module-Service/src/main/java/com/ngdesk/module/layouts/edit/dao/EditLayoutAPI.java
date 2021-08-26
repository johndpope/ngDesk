package com.ngdesk.module.layouts.edit.dao;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.springdoc.core.converters.PageableAsQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import com.ngdesk.repositories.EditLayoutRepository;
import com.ngdesk.repositories.ModuleRepository;

import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RefreshScope
public class EditLayoutAPI {

	@Autowired
	EditLayoutRepository editLayoutRepository;

	@Autowired
	AuthManager authManager;

	@Autowired
	ModuleRepository moduleRepository;

	@Autowired
	EditLayoutService editLayoutService;

	@Autowired
	ModuleService moduleService;

	@Autowired
	LayoutService layoutService;

	@Autowired
	RoleService roleService;

	@GetMapping("/modules/{module_id}/edit_layouts")
	@PageableAsQueryParam
	public Page<CreateEditLayout> getEditLayouts(@PathVariable("module_id") String moduleId,
			@Parameter(description = "Pageable object to control pagination", required = true, hidden = true) Pageable pageable) {

		moduleService.validateAndGetModule(moduleId);

		roleService.isAuthorized(authManager.getUserDetails().getRole(), "GET", moduleId);

		return editLayoutRepository.findAllEditLayoutsWithPagination(pageable, moduleId,
				authManager.getUserDetails().getCompanyId());

	}

	@GetMapping("/modules/{module_id}/edit_layout/{layout_id}")
	public CreateEditLayout getOneEditLayout(@PathVariable("module_id") String moduleId,
			@PathVariable("layout_id") String layoutId) {

		Module module = moduleService.validateAndGetModule(moduleId);

		roleService.isAuthorized(authManager.getUserDetails().getRole(), "GET", moduleId);

		return editLayoutService.validateAndGetExistingEditLayout(layoutId, module);

	}

	@PostMapping("/modules/{module_id}/edit_layout")
	public CreateEditLayout postEditLayout(@Valid @RequestBody CreateEditLayout editLayout,
			@PathVariable("module_id") String moduleId) {

		Module module = moduleService.validateAndGetModule(moduleId);

		roleService.isValidRoleId(editLayout.getRole(), "roles_" + authManager.getUserDetails().getCompanyId());

		roleService.isAuthorized(authManager.getUserDetails().getRole(), "POST", moduleId);

		editLayoutService.checkDuplicateLayoutForRole(module, editLayout.getRole());

		List<String> fieldIds = new ArrayList<String>();

		fieldIds = layoutService.getFieldIdsFromLayouts(editLayout, module);

		if (fieldIds.isEmpty()) {
			throw new BadRequestException("LAYOUT_FIELDS_EMPTY", null);
		}

		layoutService.isValidFieldId(fieldIds, module);
		layoutService.requiredFieldsValidation(fieldIds, module);

		layoutService.validateFieldPermission(editLayout.getRole(), moduleId, module);

		editLayout = layoutService.initializeDefaultsForPostCall(editLayout);

		editLayoutRepository.saveEditLayout("modules_" + authManager.getUserDetails().getCompanyId(), editLayout,
				moduleId);
		return editLayout;

	}

	@PutMapping("/modules/{module_id}/edit_layout")
	public CreateEditLayout putEditlayout(@Valid @RequestBody CreateEditLayout editLayout,
			@PathVariable("module_id") String moduleId) {

		Module module = moduleService.validateAndGetModule(moduleId);

		CreateEditLayout existingLayout = editLayoutService.validateAndGetExistingEditLayout(editLayout.getLayoutId(),
				module);

		roleService.isValidRoleId(editLayout.getRole(), "roles_" + authManager.getUserDetails().getCompanyId());

		roleService.isAuthorized(authManager.getUserDetails().getRole(), "PUT", moduleId);

		editLayoutService.duplicateCheckForPutCall(editLayout, module);

		List<String> fieldIds = new ArrayList<String>();

		fieldIds = layoutService.getFieldIdsFromLayouts(editLayout, module);

		layoutService.isValidFieldId(fieldIds, module);

		layoutService.validateFieldPermission(editLayout.getRole(), moduleId, module);

		editLayout = layoutService.initializeDefaultsForPutCall(editLayout, existingLayout);

		editLayoutRepository.updateEditLayout(editLayout, moduleId, editLayout.getLayoutId(),
				"modules_" + authManager.getUserDetails().getCompanyId());

		return editLayout;

	}

//	// NOT USED IN UI
//	@DeleteMapping("/modules/{module_id}/edit_layout/{layout_id}")
//	public void deleteEditLayout(@PathVariable("module_id") String moduleId,
//			@PathVariable("layout_id") String layoutId) {
//
//		Module module = moduleService.validateAndGetModule(moduleId,
//				"modules_" + authManager.getUserDetails().getCompanyId());
//
//		editLayoutService.validateAndGetExistingEditLayout(layoutId, module);
//
//		roleService.isAuthorized(authManager.getUserDetails().getRole(), "DELETE", moduleId);
//
//		editLayoutRepository.removeEditLayout(moduleId, layoutId,
//				"modules_" + authManager.getUserDetails().getCompanyId());
//
//	}

}
