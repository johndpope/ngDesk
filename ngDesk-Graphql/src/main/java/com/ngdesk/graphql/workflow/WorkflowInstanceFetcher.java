package com.ngdesk.graphql.workflow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.repositories.workflow.WorkflowInstanceRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class WorkflowInstanceFetcher implements DataFetcher<WorkflowInstance> {

	@Autowired
	WorkflowInstanceRepository workflowInstanceRepository;

	@Override
	public WorkflowInstance get(DataFetchingEnvironment environment) throws Exception {

		String workflowId = environment.getArgument("workflowId");
		String dataId = environment.getArgument("dataId");
		WorkflowInstance workflowInstance = workflowInstanceRepository.findByWorkflowIdAndDataId(workflowId, dataId,
				"workflows_in_execution");
		if (workflowInstance != null) {
			return workflowInstance;
		}

		return null;
	}

}
