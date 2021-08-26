package com.ngdesk.graphql.workflow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.commons.managers.SessionManager;
import com.ngdesk.graphql.modules.dao.Module;
import com.ngdesk.repositories.modules.ModulesRepository;
import com.ngdesk.repositories.modules.data.ModuleEntryRepository;
import com.ngdesk.repositories.workflow.WorkflowInstanceRepository;
import com.ngdesk.repositories.workflow.WorkflowRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class WorkflowInstancesDataFetcher implements DataFetcher<WorkflowInstance> {

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	AuthManager authManager;

	@Autowired
	WorkflowInstanceRepository workflowInstanceRepository;

	@Autowired
	WorkflowRepository workflowrepository;

	@Autowired
	SessionManager sessionManager;

	@Override
	public WorkflowInstance get(DataFetchingEnvironment environment) throws Exception {

		Map<String, Object> source = environment.getSource();
		String dataId = source.get("_id").toString();

		Module currentModule = (Module) sessionManager.getSessionInfo().get("currentModule");

		Optional<List<Workflow>> optionalWorkflows = workflowrepository.findWorkflowsToDisplay(
				authManager.getUserDetails().getCompanyId(), currentModule.getModuleId(), "module_workflows");

		if (optionalWorkflows.isEmpty()) {
			return null;
		}

		List<Workflow> workflowsWithFlagTrue = optionalWorkflows.get();

		List<String> workflowIds = new ArrayList<String>();
		for (Workflow workflow : workflowsWithFlagTrue) {
			workflowIds.add(workflow.getId());
		}

		WorkflowInstance workflowInstanceInexecution = workflowInstanceRepository.getInExecutionInstance(dataId,
				workflowIds, "workflows_in_execution");
		WorkflowInstance workflowInstanceCompleted = workflowInstanceRepository.getCompletedInstance(dataId,
				workflowIds, "workflows_in_execution");

		if ((workflowInstanceInexecution != null && workflowInstanceCompleted != null)) {
			return workflowInstanceInexecution;
		} else if (workflowInstanceInexecution != null) {
			return workflowInstanceInexecution;
		} else if (workflowInstanceCompleted != null) {
			return workflowInstanceCompleted;
		}

		return null;
	}

}
