package com.ngdesk.graphql.workflow;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.commons.managers.SessionManager;
import com.ngdesk.repositories.workflow.WorkflowRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class WorkflowEntriesDataFetcher implements DataFetcher<List<Workflow>> {

	@Autowired
	AuthManager authManager;

	@Autowired
	WorkflowRepository workflowRepository;

	@Autowired
	SessionManager sessionManager;

	ObjectMapper mapper = new ObjectMapper();

	@Override
	public List<Workflow> get(DataFetchingEnvironment environment) {

		try {
			String companyId = authManager.getUserDetails().getCompanyId();
			String moduleId = environment.getArgument("moduleId");

			Optional<List<Workflow>> optionalWorkflows = workflowRepository.findAllWorkflowsInCompany(companyId,
					moduleId, "module_workflows");

			if (optionalWorkflows.isPresent()) {
				return optionalWorkflows.get();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
