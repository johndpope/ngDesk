package com.ngdesk.graphql.workflow;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.commons.models.Widget;
import com.ngdesk.graphql.modules.dao.ModuleField;
import com.ngdesk.repositories.workflow.WorkflowInstanceRepository;
import com.ngdesk.repositories.workflow.WorkflowRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class WorkflowInstanceDataFetcher implements DataFetcher<List<WorkflowInstance>> {

	@Autowired
	WorkflowInstanceRepository workflowInstanceRepository;

	@Autowired
	WorkflowRepository workflowRepository;

	@Autowired
	AuthManager authManager;

	@Override
	public List<WorkflowInstance> get(DataFetchingEnvironment environment) throws Exception {

		String moduleId = environment.getArgument("moduleId");
		String dataId = environment.getArgument("dataId");
		String companyId = authManager.getUserDetails().getCompanyId();

		Optional<List<Workflow>> optionalWorkflows = workflowRepository.findWorkflowsToDisplay(companyId, moduleId,
				"module_workflows");

		if (optionalWorkflows.isEmpty()) {
			return null;
		}

		List<Workflow> workflows = optionalWorkflows.get();

		List<WorkflowInstance> allWorkflowInstances = new ArrayList<WorkflowInstance>();
		for (Workflow workflow : workflows) {
			String workflowId = workflow.getId();
			WorkflowInstance optionalWorkflowInstances = workflowInstanceRepository
					.findByWorkflowInstanceByModuleId(moduleId, dataId, workflowId, "workflows_in_execution");
			if (optionalWorkflowInstances != null) {
				allWorkflowInstances.add(optionalWorkflowInstances);
			}
		}

		return allWorkflowInstances;
	}

}
