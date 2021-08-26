package com.ngdesk.repositories;

import java.util.List;

import com.ngdesk.workflow.executor.dao.WorkflowInstance;

public interface CustomWorkflowInstanceRepository {
	
	public List<WorkflowInstance> getPausedWorkflows(String companyId, String moduleId, String dataId, String collectionName);
}
