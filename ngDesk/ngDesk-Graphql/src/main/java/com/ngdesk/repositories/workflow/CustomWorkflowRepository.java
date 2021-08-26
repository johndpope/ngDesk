package com.ngdesk.repositories.workflow;

import java.util.List;
import java.util.Optional;

import com.ngdesk.graphql.workflow.Workflow;

public interface CustomWorkflowRepository {

	public Optional<List<Workflow>> findWorkflowsToDisplay(String companyId, String moduleId, String collectionName);

}
