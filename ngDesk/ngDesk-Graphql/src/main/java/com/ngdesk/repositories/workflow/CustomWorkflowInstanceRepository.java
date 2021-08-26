package com.ngdesk.repositories.workflow;

import java.util.List;
import java.util.Optional;

import com.ngdesk.graphql.workflow.WorkflowInstance;

public interface CustomWorkflowInstanceRepository {

	public WorkflowInstance getInExecutionInstance(String dataId, List<String> entryIds, String collectionName);

	public WorkflowInstance getCompletedInstance(String dataId, List<String> entryIds, String collectionName);

	public WorkflowInstance findByWorkflowIdAndDataId(String workflowId, String dataId, String collectionName);

	public WorkflowInstance findByWorkflowInstanceByModuleId(String moduleId, String dataId, String workflowId,
			String collectionName);

}
