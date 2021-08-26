package com.ngdesk.data.dao;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.data.form.dao.Form;
import com.ngdesk.data.modules.dao.Module;
import com.ngdesk.data.sam.dao.DataProxy;
import com.ngdesk.repositories.form.FormRepository;
import com.ngdesk.repositories.module.entry.ModulesRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
public class FormDataAPI {

	@Autowired
	FormRepository formRepository;

	@Autowired
	AuthManager authManager;

	@Autowired
	DataService dataService;

	@Autowired
	DataProxy dataProxy;

	@Autowired
	ModulesRepository modulesRepository;

	@PostMapping("/module/{moduleId}/form/{formId}")
	@Operation(summary = "Post Form Entry", description = "Post a Form entry for a module")
	public void postFormEntry(@RequestBody HashMap<String, Object> entry,
			@Parameter(description = "Form ID", required = true) @PathVariable("formId") String formId,
			@Parameter(description = "Module ID", required = true) @PathVariable("moduleId") String moduleId) {

		Optional<Module> optionalModule = modulesRepository.findById(moduleId,
				"modules_" + authManager.getUserDetails().getCompanyId());
		if (optionalModule.isEmpty()) {
			throw new BadRequestException("INVALID_MODULE", null);
		}
		Optional<Form> optionalForm = formRepository.findFormByFormId(formId, moduleId,
				authManager.getUserDetails().getCompanyId(), "forms");
		if (optionalForm.isEmpty()) {
			throw new BadRequestException("INVALID_FORM", null);
		}
		Map<String, Object> entryCreated = dataProxy.postModuleEntry(entry, moduleId, true,
				authManager.getUserDetails().getCompanyId(), authManager.getUserDetails().getUserUuid());
		Form form = optionalForm.get();
		if (form.getWorkflow() != null && !form.getWorkflow().isBlank()) {
			String workflowId = form.getWorkflow();
			String dataId = entryCreated.get("DATA_ID").toString();
			SingleWorkflowPayload singleWorkflowPayload = new SingleWorkflowPayload(
					authManager.getUserDetails().getCompanyId(), moduleId, dataId, workflowId,
					authManager.getUserDetails().getUserId(), new Date());
			dataService.addToSingleWorkflowQueue(singleWorkflowPayload);
		}

	}

}
