package com.ngdesk.repositories.workflow;

import org.springframework.stereotype.Repository;

import com.ngdesk.graphql.workflow.WorkflowInstance;
import com.ngdesk.repositories.CustomNgdeskRepository;

@Repository
public interface WorkflowInstanceRepository
		extends CustomNgdeskRepository<WorkflowInstance, String>, CustomWorkflowInstanceRepository {

}
