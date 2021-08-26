package com.ngdesk.graphql.workflow;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.managers.SessionManager;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class NodeDataFetcher implements DataFetcher<Node> {

	@Autowired
	SessionManager sessionManager;

	ObjectMapper mapper = new ObjectMapper();

	@Override
	public Node get(DataFetchingEnvironment environment) throws Exception {

		Workflow workflow = (Workflow) sessionManager.getSessionInfo().get("workflowMap");
		Map<String, Object> source = mapper.readValue(mapper.writeValueAsString(environment.getSource()), Map.class);
		if (workflow != null && source != null) {
			String fieldName = environment.getField().getName();
			List<Stage> stages = workflow.getStages();
			Node nodeToReturn = null;
			for (Stage stage : stages) {
				List<Node> nodes = stage.getNodes();
				Optional<Node> optionalNodeToReturn = nodes.stream()
						.filter(node -> node.getNodeId().equals(source.get(fieldName).toString())).findFirst();
				if (optionalNodeToReturn.isPresent()) {
					nodeToReturn = optionalNodeToReturn.get();
					break;
				}
			}
			if (nodeToReturn != null) {
				return nodeToReturn;
			}

		}
		return null;

	}

}
