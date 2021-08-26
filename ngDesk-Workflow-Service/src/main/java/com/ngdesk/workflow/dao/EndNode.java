package com.ngdesk.workflow.dao;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.data.dao.WorkflowExecutionInstance;
import com.ngdesk.repositories.WorkflowInstanceRepository;
import com.ngdesk.workflow.executor.dao.WorkflowInstance;

@Component
public class EndNode extends Node {

	@Autowired
	WorkflowInstanceRepository workflowInstanceRepository;

	@Override
	public void execute(WorkflowExecutionInstance instance) {
		updateWorkflowInstance(instance);
	}

	@Override
	public void executeNextNode(WorkflowExecutionInstance instance) {

	}

	@Override
	public boolean validateNodeOnSave(Optional<?> fields) {
		return false;
	}

	@Override
	public void updateWorkflowInstance(WorkflowExecutionInstance instance) {
		Optional<WorkflowInstance> optionalWorkflowInstance = workflowInstanceRepository
				.findById(instance.getWorkflowInstanceId(), "workflows_in_execution");
		WorkflowInstance workflowInstance = optionalWorkflowInstance.get();
		workflowInstance.setStatus("COMPLETED");
		workflowInstanceRepository.save(workflowInstance, "workflows_in_execution");
	}
}
