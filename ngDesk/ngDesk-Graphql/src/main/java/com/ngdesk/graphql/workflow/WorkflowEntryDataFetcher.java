package com.ngdesk.graphql.workflow;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.commons.managers.SessionManager;
import com.ngdesk.repositories.workflow.WorkflowRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class WorkflowEntryDataFetcher implements DataFetcher<Workflow> {

	@Autowired
	AuthManager authManager;

	@Autowired
	WorkflowRepository workflowRepository;

	@Autowired
	SessionManager sessionManager;

	ObjectMapper mapper = new ObjectMapper();

	@Override
	public Workflow get(DataFetchingEnvironment environment) {

		try {
			String companyId = authManager.getUserDetails().getCompanyId();
			Map<String, Object> entry = mapper.readValue(mapper.writeValueAsString(environment.getSource()), Map.class);

			String entryId = null;
			if (entry == null) {
				entryId = environment.getArgument("id");
			} else {
				String fieldName = environment.getField().getName();
				entryId = entry.get(fieldName).toString();
			}
			Optional<Workflow> optionalWorkflow = workflowRepository.findByIdAndCompanyId(entryId, companyId,
					"module_workflows");
			if (optionalWorkflow.isPresent()) {
				sessionManager.getSessionInfo().put("workflowMap", optionalWorkflow.get());
				return optionalWorkflow.get();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

}
