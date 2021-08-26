package com.ngdesk.workflow.dao;

import java.util.Date;
import java.util.Optional;

import javax.validation.Valid;

import org.springdoc.core.converters.PageableAsQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ngdesk.commons.exceptions.NotFoundException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.ModuleEntryRepository;
import com.ngdesk.repositories.ModulesRepository;
import com.ngdesk.repositories.WorkflowRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RefreshScope
public class WorkflowAPI {

	@Autowired
	private WorkflowRepository workflowRepository;

	@Autowired
	private AuthManager authManager;

	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	ModuleEntryRepository entryRepository;

	String collectionName = "module_workflows";

	@GetMapping("/modules/{module_id}/workflows")
	@Operation(summary = "Get all", description = "Gets all the workflows for the module with pagination")
	@PageableAsQueryParam
	public Page<Workflow> getWorkflows(
			@Parameter(description = "Pageable object to control pagination", required = true, hidden = true) Pageable pageable,
			@Parameter(description = "Id of the module of which workflows are to be retreived", required = true) @PathVariable("module_id") String moduleId) {

		String companyId = authManager.getUserDetails().getCompanyId();

		if (modulesRepository.findById(moduleId, "modules_" + companyId).isEmpty()) {
			String vars[] = { "MODULE" };
			throw new NotFoundException("DAO_NOT_FOUND", vars);
		}

		return workflowRepository.findAllWorkflows(pageable, moduleId, companyId, collectionName);
	}

	@GetMapping("/modules/{module_id}/workflows/{workflow_id}")
	@Operation(summary = "Get Workflow from module", description = "Get a specific workflow by Id")
	public Workflow getWorkflowById(
			@Parameter(description = "Id of the module to which the workflow belongs to", required = true) @PathVariable("module_id") String moduleId,
			@Parameter(description = "Id of the workflow to be retrieved", required = true) @PathVariable("workflow_id") String workflowId) {

		String companyId = authManager.getUserDetails().getCompanyId();

		if (modulesRepository.findById(moduleId, "modules_" + companyId).isEmpty()) {
			String vars[] = { "MODULE" };
			throw new NotFoundException("DAO_NOT_FOUND", vars);
		}

		Optional<Workflow> optional = workflowRepository.findWorkflowById(workflowId, moduleId, companyId,
				collectionName);

		if (optional.isEmpty()) {
			String vars[] = { "WORKFLOW" };
			throw new NotFoundException("DAO_NOT_FOUND", vars);
		}

		return optional.get();
	}

	@PostMapping("/modules/{module_id}/workflow")
	@Operation(summary = "Post Workflow to module", description = "post a new workflow to the module")
	public Workflow postWorkflow(
			@Parameter(description = "Id of the module to which the workflow belongs to", required = true) @PathVariable("module_id") String moduleId,
			@Valid @RequestBody Workflow workflow) throws JsonProcessingException {
		String companyId = authManager.getUserDetails().getCompanyId();
		if (modulesRepository.findById(moduleId, "modules_" + companyId).isEmpty()) {
			String vars[] = { "MODULE" };
			throw new NotFoundException("DAO_NOT_FOUND", vars);
		}
		workflow.setDateCreated(new Date());
		workflow.setDateUpdated(new Date());
		workflow.setCreatedBy(authManager.getUserDetails().getUserId());
		workflow.setLastUpdated(authManager.getUserDetails().getUserId());
		workflow.setModuleId(moduleId);
		workflow.setCompanyId(companyId);

		return workflowRepository.save(workflow, collectionName);
	}

	@PutMapping("/modules/{module_id}/workflow")
	@Operation(summary = "Update a workflow in the module", description = "Update an existing workflow in the module")
	public Workflow putWorkflow(
			@Parameter(description = "Id of the module to which the workflow belongs to", required = true) @PathVariable("module_id") String moduleId,
			@Valid @RequestBody Workflow workflow) {

		String companyId = authManager.getUserDetails().getCompanyId();
		if (modulesRepository.findById(moduleId, "modules_" + companyId).isEmpty()) {
			String vars[] = { "MODULE" };
			throw new NotFoundException("DAO_NOT_FOUND", vars);
		}

		Optional<Workflow> optional = workflowRepository.findWorkflowById(workflow.getId(), moduleId, companyId,
				collectionName);
		if (optional.isEmpty()) {
			String vars[] = { "WORKFLOW" };
			throw new NotFoundException("DAO_NOT_FOUND", vars);
		}
		Workflow existingWorkflow = optional.get();

		workflow.setCreatedBy(existingWorkflow.getCreatedBy());
		workflow.setDateCreated(existingWorkflow.getDateCreated());

		workflow.setDateUpdated(new Date());
		workflow.setLastUpdated(authManager.getUserDetails().getUserId());

		return workflowRepository.save(workflow, collectionName);
	}

	@DeleteMapping("/modules/{module_id}/workflow/{workflow_id}")
	@Operation(summary = "Deletes a workflow in the module", description = "Delete an existing workflow in the module")
	public void deleteWorkflow(
			@Parameter(description = "Id of the module to which the workflow belongs to", required = true) @PathVariable("module_id") String moduleId,
			@Parameter(description = "Id of the workflow to be retrieved", required = true) @PathVariable("workflow_id") String workflowId) {

		String companyId = authManager.getUserDetails().getCompanyId();

		if (modulesRepository.findById(moduleId, "modules_" + companyId).isEmpty()) {
			String vars[] = { "MODULE" };
			throw new NotFoundException("DAO_NOT_FOUND", vars);
		}

		Optional<Workflow> optional = workflowRepository.findWorkflowById(workflowId, moduleId, companyId,
				collectionName);

		if (optional.isEmpty()) {
			String vars[] = { "WORKFLOW" };
			throw new NotFoundException("DAO_NOT_FOUND", vars);
		}

		workflowRepository.deleteById(workflowId, collectionName);
	}
}
