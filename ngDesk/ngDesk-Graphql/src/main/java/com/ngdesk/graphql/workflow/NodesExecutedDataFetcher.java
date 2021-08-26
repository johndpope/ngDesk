package com.ngdesk.graphql.workflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class NodesExecutedDataFetcher implements DataFetcher<List<Map<String, Object>>> {

	ObjectMapper mapper = new ObjectMapper();

	@Override
	public List<Map<String, Object>> get(DataFetchingEnvironment environment) throws Exception {

		Map<String, Object> source = mapper.readValue(mapper.writeValueAsString(environment.getSource()), Map.class);
		if (source != null) {
			String fieldName = environment.getField().getName();
			Map<String, Object> nodesExecuted = (Map<String, Object>) source.get(fieldName);
			Set<String> nodeIds = nodesExecuted.keySet();
			List<Map<String, Object>> nodesExecutedList = new ArrayList<Map<String, Object>>();
			for (String nodeId : nodeIds) {
				Map<String, Object> nodesExecutedToReturn = new HashMap<String, Object>();
				nodesExecutedToReturn.put("nodeId", nodeId);
				nodesExecutedToReturn.put("nodeExecutionInfo", nodesExecuted.get(nodeId));
				nodesExecutedList.add(nodesExecutedToReturn);
			}
			return nodesExecutedList;

		}

		return null;
	}

}
