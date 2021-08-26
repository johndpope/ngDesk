package com.ngdesk.repositories;

import java.util.List;
import java.util.Optional;

import com.ngdesk.workflow.dao.Workflow;

public interface CustomWorkflowRepository {

	public Optional<List<Workflow>> getAllWorkflowTemplates(String moduleId, String collectionName);

	public Optional<Workflow> getWorkflowByModule(String workflowId, String moduleId, String companyId,
			String collectionName);

	public Optional<List<Workflow>> findAllWithModuleIdAndCompanyId(String moduleId, String companyId,
			String collectionName);

	public Optional<List<Workflow>> findAllWithModuleIdsAndCompanyId(List<String> moduleIds, String companyId,
			String collectionName);

}
