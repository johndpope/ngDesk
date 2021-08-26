package com.ngdesk.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ngdesk.workflow.dao.Workflow;

public interface CustomWorkflowRepository {

	public Optional<Workflow> findWorkflowByName(String name, String collection);

	public Optional<Workflow> findOtherWorkflowWithDuplicateName(String name, String moduleId, String workflowId,
			String companyId, String collectionName);

	public Optional<Workflow> findWorkFlowOrder(String workflowId, int order, String moduleId, String companyId,
			String collectionName);

	public Page<Workflow> findAllWorkflows(Pageable pageable, String moduleId, String companyId, String collectionName);

	public List<Workflow> findAllWorkflows(String moduleId, String companyId, String collectionName);

	public Optional<Workflow> findWorkflowById(String workflowId, String moduleId, String companyId,
			String collectionName);

	public void deleteWorkflow(String moduleId, String companyId, String workflowId, String collectionName);
}
