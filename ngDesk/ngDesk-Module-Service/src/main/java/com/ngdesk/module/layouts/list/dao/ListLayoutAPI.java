package com.ngdesk.module.layouts.list.dao;

import java.util.UUID;

import javax.validation.Valid;

import org.springdoc.core.converters.PageableAsQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.module.dao.Module;
import com.ngdesk.module.dao.ModuleService;
import com.ngdesk.module.layout.dao.LayoutService;
import com.ngdesk.module.layout.dao.ListLayout;
import com.ngdesk.module.role.dao.RoleService;
import com.ngdesk.repositories.ListLayoutRepository;
import com.ngdesk.repositories.ModuleRepository;

import io.swagger.v3.oas.annotations.Parameter;

@RestController
public class ListLayoutAPI {
	@Autowired
	ListLayoutRepository listLayoutRepository;

	@Autowired
	ModuleRepository moduleRepository;

	@Autowired
	ListLayoutService listLayoutService;

	@Autowired
	MongoOperations mongoOperations;

	@Autowired
	AuthManager authManager;

	@Autowired
	ModuleService moduleService;

	@Autowired
	RoleService roleService;

	@Autowired
	LayoutService layoutService;

	@PostMapping("/modules/{module_id}/list_layout")
	public ListLayout postListLayout(@Valid @RequestBody ListLayout listLayout,
			@PathVariable("module_id") String moduleId) {

		Module module = moduleService.validateAndGetModule(moduleId);
		listLayout.setLayoutId(UUID.randomUUID().toString());

		roleService.isValidRoleId(listLayout.getRole(), "roles_" + authManager.getUserDetails().getCompanyId());

		// TODO: VALIDATE CONDITIONS
		listLayoutService.isValidOrderBy(listLayout);
		layoutService.isValidFieldId(listLayout.getColumnShow().getFields(), module);

		listLayoutService.isValidOrderBy(listLayout);
		layoutService.isValidFieldId(listLayout.getColumnShow().getFields(), module);

		listLayoutService.isValidConditions(listLayout, module);

		listLayoutService.isValidListLayoutFields(listLayout, module);
		listLayoutService.isDefault(listLayout, moduleId, module);

		listLayoutService.isRoleNameExists(listLayout.getName(), listLayout.getRole(), moduleId,
				"modules_" + authManager.getUserDetails().getCompanyId());
		listLayoutService.isDefault(listLayout, moduleId, module);

		listLayoutRepository.saveListLayout("modules_" + authManager.getUserDetails().getCompanyId(), listLayout,
				moduleId, authManager.getUserDetails().getCompanyId());

		return listLayout;
	}

	@PutMapping("/modules/{module_id}/list_layout")
	public ListLayout putListLayout(@RequestBody ListLayout listLayout, @PathVariable("module_id") String moduleId) {

		Module module = moduleService.validateAndGetModule(moduleId);

		ListLayout existingListLayout = listLayoutService.validateAndGetListLayout(module, listLayout.getLayoutId());

		roleService.isValidRoleId(listLayout.getRole(), "roles_" + authManager.getUserDetails().getCompanyId());

		// TODO: VALIDATE CONDITIONS
		layoutService.isValidFieldId(listLayout.getColumnShow().getFields(), module);
		listLayoutService.isValidOrderBy(listLayout);
		layoutService.isValidFieldId(listLayout.getColumnShow().getFields(), module);

		listLayoutService.isValidConditions(listLayout, module);

		listLayoutService.isValidListLayoutFields(listLayout, module);
		listLayoutService.isDefault(listLayout, moduleId, module);

		listLayoutRepository.removeListLayout(moduleId, listLayout.getLayoutId(),
				"modules_" + authManager.getUserDetails().getCompanyId());

		listLayoutRepository.saveListLayout("modules_" + authManager.getUserDetails().getCompanyId(), listLayout,
				moduleId, authManager.getUserDetails().getCompanyId());

		return listLayout;
	}

	@GetMapping("/modules/{module_id}/list_layout/{layout_id}")
	public ListLayout getListLayout(@PathVariable("module_id") String moduleId,
			@PathVariable("layout_id") String layoutId) {

		Module module = moduleService.validateAndGetModule(moduleId);

		return listLayoutService.validateAndGetListLayout(module, layoutId);
	}

	@GetMapping("/modules/{module_id}/list_layouts")
	@PageableAsQueryParam
	public Page<ListLayout> getAllListLayout(@PathVariable("module_id") String moduleId,
			@Parameter(description = "Pageable object to control pagination", required = true, hidden = true) Pageable pageable)
			throws JsonProcessingException {

		return listLayoutRepository.findAllListLayoutsWithPagination(pageable, moduleId,
				authManager.getUserDetails().getCompanyId());
	}

	@DeleteMapping("/modules/{module_id}/list_layout/{layout_id}")
	public void deleteListLayout(@PathVariable("module_id") String moduleId,
			@PathVariable("layout_id") String layoutId) {

		Module module = moduleService.validateAndGetModule(moduleId);

		listLayoutService.validateAndGetListLayout(module, layoutId);

		listLayoutRepository.removeListLayout(moduleId, layoutId,
				"modules_" + authManager.getUserDetails().getCompanyId());
	}

}