package com.ngdesk.workflow.executor.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.repositories.WorkflowRepository;
import com.ngdesk.workflow.dao.Workflow;

@Component
public class WorkflowExecutionService {

	@Autowired
	WorkflowRepository workflowRepository;

	String collectionName = "module_workflows";

	public List<Workflow> getWorkflowsForModule(String moduleId, String companyId) {
		return workflowRepository.findAllWorkflows(moduleId, companyId, collectionName);
	}

}
