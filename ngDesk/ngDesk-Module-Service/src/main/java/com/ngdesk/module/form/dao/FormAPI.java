package com.ngdesk.module.form.dao;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.exceptions.ForbiddenException;
import com.ngdesk.commons.exceptions.NotFoundException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.module.role.dao.RoleService;
import com.ngdesk.module.task.dao.Task;
import com.ngdesk.repositories.FormRepository;
import com.ngdesk.repositories.RoleRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
public class FormAPI {

	@Autowired
	AuthManager authManager;

	@Autowired
	FormRepository formRepository;

	@Autowired
	RoleRepository rolesRepository;

	@Autowired
	RoleService roleService;

	@PostMapping("/modules/{moduleId}/form")
	@Operation(summary = "Post Form", description = "Post a form")
	public Form postForm(@Valid @RequestBody Form form,
			@Parameter(description = "Module ID", required = true) @PathVariable("moduleId") String moduleId) {
		if (!roleService.isSystemAdmin()) {
			throw new ForbiddenException("FORBIDDEN");
		}
		form.setCompanyId(authManager.getUserDetails().getCompanyId());
		form.setModuleId(moduleId);
		form.setDateCreated(new Date());
		form.setDateUpdated(new Date());
		form.setLastUpdatedBy(authManager.getUserDetails().getUserId());
		form.setCreatedBy(authManager.getUserDetails().getUserId());
		formRepository.save(form, "forms");
		return form;

	}

	@PutMapping("/modules/{moduleId}/form")
	@Operation(summary = "Put Form", description = "Update a Form")
	public Form putForm(@Valid @RequestBody Form form,
			@Parameter(description = "Module ID", required = true) @PathVariable String moduleId) {
		Optional<Form> optionalForm = formRepository.findFormById(form.getFormId(),
				authManager.getUserDetails().getCompanyId(), moduleId, "forms");
		if (!roleService.isSystemAdmin()) {
			throw new ForbiddenException("FORBIDDEN");
		}
		if (optionalForm.isEmpty()) {
			throw new BadRequestException("FORM_DOES_NOT_EXIST", null);
		}

		Optional<Form> existingForm = formRepository.findFormById(form.getFormId(),
				authManager.getUserDetails().getCompanyId(), moduleId, "forms");
		form.setCompanyId(authManager.getUserDetails().getCompanyId());
		form.setModuleId(existingForm.get().getModuleId());
		form.setDateCreated(existingForm.get().getDateCreated());
		form.setCreatedBy(existingForm.get().getCreatedBy());
		form.setDateUpdated(new Date());
		form.setLastUpdatedBy(authManager.getUserDetails().getUserId());
		return formRepository.save(form, "forms");
	}

	@DeleteMapping("/module/{moduleId}/form/{formId}")
	@Operation(summary = "Delete Form", description = "Delete a Form by ID")
	public void deleteForm(@Parameter(description = "Form ID", required = true) @PathVariable String formId,
			@Parameter(description = "mdule Id", required = true) @PathVariable String moduleId) {
		if (!roleService.isSystemAdmin()) {
			throw new ForbiddenException("FORBIDDEN");
		}
		String companyId = authManager.getUserDetails().getCompanyId();

		Optional<Form> optionalForm = formRepository.findFormById(formId, companyId, moduleId, "forms");
		System.out.println("form Id " + optionalForm.get().getFormId());
		if (optionalForm.isEmpty()) {
			throw new NotFoundException("FORM_NOT_FOUND", null);
		}
		formRepository.removeFormById(formId, companyId, moduleId, "forms");
	}

}
