package com.ngdesk.module.layouts.mobile.dao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.validation.Valid;

import org.springdoc.core.converters.PageableAsQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.jaxb.SpringDataJaxb.PageDto;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
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
import com.ngdesk.repositories.EditMobileLayoutRepository;
import com.ngdesk.repositories.ModuleRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
public class EditMobileLayoutAPI {

	@Autowired
	EditMobileLayoutRepository editMobileLayoutRepository;

	@Autowired
	AuthManager authManager;

	@Autowired
	EditMobileLayoutService editMobileLayoutService;

	@Autowired
	ModuleRepository moduleRepository;

	@Autowired
	MongoOperations mongoOperations;

	@PostMapping("/{module_id}/edit_Mobile_Layout")
	public CreateEditMobileLayout postLayout(@Valid @RequestBody CreateEditMobileLayout createEditMobileLayout,
			@PathVariable("module_id") String moduleId) {
		editMobileLayoutService.isValidModuleId(moduleId, "modules_" + authManager.getUserDetails().getCompanyId());

		editMobileLayoutService.isValidRoleId(createEditMobileLayout.getRole(),
				"roles_" + authManager.getUserDetails().getCompanyId());
		editMobileLayoutService.checkForDuplicateRoleId(createEditMobileLayout.getRole(), moduleId,
				"modules_" + authManager.getUserDetails().getCompanyId(), createEditMobileLayout.getLayoutId());

		createEditMobileLayout.setLayoutId(UUID.randomUUID().toString());

		editMobileLayoutService.isValidFieldId(createEditMobileLayout.getFields(), moduleId,
				"modules_" + authManager.getUserDetails().getCompanyId());

		editMobileLayoutRepository.saveEditMobileLayout("modules_" + authManager.getUserDetails().getCompanyId(),
				createEditMobileLayout, moduleId, authManager.getUserDetails().getCompanyId());
		return createEditMobileLayout;
	}

	@GetMapping("/{module_id}/edit_mobile_layouts")
	@PageableAsQueryParam
	public Page<CreateEditMobileLayout> getAllEditMobileLayout(@PathVariable("module_id") String moduleId,
			@Parameter(description = "Pageable object to control pagination", required = true, hidden = true) Pageable pageable)
			throws JsonProcessingException {

		editMobileLayoutService.isValidModuleId(moduleId, "modules_" + authManager.getUserDetails().getCompanyId());
		return editMobileLayoutRepository.findAllEditMobileLayoutsWithPagination(pageable, moduleId,
				authManager.getUserDetails().getCompanyId());
	}

	@GetMapping("/{module_id}/edit_mobile_layout/{layout_id}")
	public CreateEditMobileLayout getOneEditMobileLayout(@Valid @PathVariable("module_id") String moduleid,

			@PathVariable("layout_id") String layoutid) {
		Optional<Module> optionalgetone = moduleRepository.findById(moduleid,
				"modules_" + authManager.getUserDetails().getCompanyId());
		List<CreateEditMobileLayout> createEditMobileLayouts = optionalgetone.get().getEditMobileLayout();
		for (int i = 0; i < createEditMobileLayouts.size(); i++) {
			if (createEditMobileLayouts.get(i).getLayoutId().equals(layoutid)) {
				return createEditMobileLayouts.get(i);
			}
		}
		throw new NotFoundException("DAO_NOT_FOUND", null);

	}

	@PutMapping("/{module_id}/update_edit_layout/{layout_id}")
	public CreateEditMobileLayout updateEditMobileLayout(@RequestBody CreateEditMobileLayout createEditMobileLayout,
			@PathVariable("module_id") String moduleId, @PathVariable("layout_id") String layoutId) {

		editMobileLayoutService.isValidModuleId(moduleId, "modules_" + authManager.getUserDetails().getCompanyId());

		editMobileLayoutService.isLayoutValid(moduleId, layoutId,
				"modules_" + authManager.getUserDetails().getCompanyId());

		editMobileLayoutService.isValidRoleId(createEditMobileLayout.getRole(),
				"roles_" + authManager.getUserDetails().getCompanyId());

		editMobileLayoutService.isValidFieldId(createEditMobileLayout.getFields(), moduleId,
				"modules_" + authManager.getUserDetails().getCompanyId());

		editMobileLayoutService.checkForDuplicateRoleId(createEditMobileLayout.getRole(), moduleId,
				"modules_" + authManager.getUserDetails().getCompanyId(), layoutId);

		editMobileLayoutRepository.removeEditMobileLayout(moduleId, layoutId,
				"modules_" + authManager.getUserDetails().getCompanyId());

		editMobileLayoutService.checkForDuplicateRoleId(createEditMobileLayout.getRole(), moduleId,
				"modules_" + authManager.getUserDetails().getCompanyId(), layoutId);

		editMobileLayoutRepository.saveEditMobileLayout("modules_" + authManager.getUserDetails().getCompanyId(),
				createEditMobileLayout, moduleId, authManager.getUserDetails().getCompanyId());
		return createEditMobileLayout;
	}

	@DeleteMapping("/{module_id}/edit_layout_toDelete/{layout_id}")
	public void deleteEditMobileLayout(@PathVariable("module_id") String moduleId,
			@PathVariable("layout_id") String layoutId) {

		editMobileLayoutService.isLayoutValid(moduleId, layoutId,
				"modules_" + authManager.getUserDetails().getCompanyId());
		editMobileLayoutRepository.removeEditMobileLayout(moduleId, layoutId,
				"modules_" + authManager.getUserDetails().getCompanyId());

	}

}
