package com.ngdesk.module.layouts.mobile.dao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.validation.Valid;

import org.springdoc.core.converters.PageableAsQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
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
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.exceptions.NotFoundException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.module.dao.Module;
import com.ngdesk.module.mobile.layout.dao.CreateEditMobileLayout;
import com.ngdesk.repositories.CreateMobileLayoutRepository;
import com.ngdesk.repositories.ModuleRepository;

import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RefreshScope
public class CreateMobileLayoutAPI {

	@Autowired
	CreateMobileLayoutRepository createMobileLayoutRepository;

	@Autowired
	AuthManager authManager;

	@Autowired
	ModuleRepository moduleRepository;

	@Autowired
	CreateMobileLayoutService createMobileLayoutService;

	@Autowired
	MongoOperations mongoOperations;

	@PostMapping("/{module_id}/create_mobile_layouts")
	public CreateEditMobileLayout postMobileLayout(@Valid @RequestBody CreateEditMobileLayout createMobileLayouts,
			@PathVariable("module_id") String moduleId) {

		createMobileLayoutService.isValidModuleId(moduleId, "modules_" + authManager.getUserDetails().getCompanyId());

		createMobileLayoutService.isValidRoleId(createMobileLayouts.getRole(),
				"roles_" + authManager.getUserDetails().getCompanyId());

		createMobileLayoutService.checkForDuplicateRoleId(createMobileLayouts.getRole(), moduleId,
				"modules_" + authManager.getUserDetails().getCompanyId(), null);

		createMobileLayoutService.isValidFieldId(createMobileLayouts.getFields(), moduleId,
				"modules_" + authManager.getUserDetails().getCompanyId());

		createMobileLayouts.setLayoutId(UUID.randomUUID().toString());

		createMobileLayoutRepository.saveCreateMobileLayout("modules_" + authManager.getUserDetails().getCompanyId(),
				createMobileLayouts, moduleId, authManager.getUserDetails().getCompanyId());

		return createMobileLayouts;
	}

	@PutMapping("/{module_id}/create_mobile_layouts/{id}")
	public CreateEditMobileLayout putMobileLayout(@Valid @RequestBody CreateEditMobileLayout createMobileLayouts,
			@PathVariable("module_id") String moduleId, @PathVariable("id") String layoutId) {

		createMobileLayoutService.isValidLayoutId(layoutId, moduleId,
				"modules_" + authManager.getUserDetails().getCompanyId());

		createMobileLayoutService.isValidModuleId(moduleId, "modules_" + authManager.getUserDetails().getCompanyId());

		createMobileLayoutService.isValidRoleId(createMobileLayouts.getRole(),
				"roles_" + authManager.getUserDetails().getCompanyId());

		createMobileLayoutService.isValidFieldId(createMobileLayouts.getFields(), moduleId,
				"modules_" + authManager.getUserDetails().getCompanyId());

		createMobileLayoutService.checkForDuplicateRoleId(createMobileLayouts.getRole(), moduleId,
				"modules_" + authManager.getUserDetails().getCompanyId(), layoutId);

		createMobileLayoutRepository.removeCreateMobileLayout(layoutId,
				"modules_" + authManager.getUserDetails().getCompanyId(), moduleId);

		createMobileLayouts.setLayoutId(layoutId);

		createMobileLayoutRepository.saveCreateMobileLayout("modules_" + authManager.getUserDetails().getCompanyId(),
				createMobileLayouts, moduleId, authManager.getUserDetails().getCompanyId());

		return createMobileLayouts;
	}

	@GetMapping("/{module_id}/create_mobile_layouts/{id}")
	public CreateEditMobileLayout getOneMobileLayout(@PathVariable("module_id") String moduleId,
			@PathVariable("id") String layoutId) {

		createMobileLayoutService.isValidLayoutId(layoutId, moduleId,
				"modules_" + authManager.getUserDetails().getCompanyId());

		Optional<Module> optionalModule = moduleRepository.findById(moduleId,
				"modules_" + authManager.getUserDetails().getCompanyId());

		if (optionalModule.isEmpty()) {
			throw new NotFoundException("DAO_NOT_FOUND", null);
		}

		List<CreateEditMobileLayout> createMobileLayout = optionalModule.get().getCreateMobileLayout();

		for (int i = 0; i < createMobileLayout.size(); i++) {
			if ((createMobileLayout.get(i).getLayoutId()).equals(layoutId)) {
				return createMobileLayout.get(i);
			}
		}
		throw new BadRequestException("DAO_NOT_FOUND", null);
	}

	@GetMapping("/{module_id}/create_mobile_layouts")
	@PageableAsQueryParam
	public Page<CreateEditMobileLayout> getAllMobileLayout(@PathVariable("module_id") String moduleId,
			@Parameter(description = "Pageable object to control pagination", required = true, hidden = true) Pageable pageable)
			throws JsonProcessingException {

		return createMobileLayoutRepository.findAllCreateMobileLayoutWithPagination(pageable, moduleId,
				authManager.getUserDetails().getCompanyId());
	}

	@DeleteMapping("/{module_id}/create_mobile_layouts/{id}")
	public void deleteMobileLayout(@PathVariable("module_id") String moduleId, @PathVariable("id") String layoutId) {

		createMobileLayoutService.isValidLayoutId(layoutId, moduleId,
				"modules_" + authManager.getUserDetails().getCompanyId());

		createMobileLayoutRepository.removeCreateMobileLayout(layoutId,
				"modules_" + authManager.getUserDetails().getCompanyId(), moduleId);

	}

}
