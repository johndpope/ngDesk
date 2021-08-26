package com.ngdesk.repositories;

import org.springframework.stereotype.Repository;

import com.ngdesk.workflow.executor.dao.WorkflowInstance;

@Repository
public interface WorkflowInstanceRepository
		extends CustomWorkflowInstanceRepository, CustomNgdeskRepository<WorkflowInstance, String> {

}
