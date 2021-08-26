package com.ngdesk.graphql.workflow;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.managers.SessionManager;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class StageDataFetcher implements DataFetcher<Stage> {
	@Autowired
	SessionManager sessionManager;

	ObjectMapper mapper = new ObjectMapper();

	@Override
	public Stage get(DataFetchingEnvironment environment) throws Exception {

		Workflow workflow = (Workflow) sessionManager.getSessionInfo().get("workflowMap");
		Map<String, Object> source = mapper.readValue(mapper.writeValueAsString(environment.getSource()), Map.class);
		if (workflow != null && source != null) {
			String fieldName = environment.getField().getName();
			List<Stage> stages = workflow.getStages();
			Stage stageToReturn = stages.stream()
					.filter(stage -> stage.getId().equals(source.get(fieldName).toString())).findFirst().get();
			if (stageToReturn != null) {
				return stageToReturn;
			}

		}

		return null;
	}

}
