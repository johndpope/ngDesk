package com.ngdesk.graphql.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.exceptions.InternalErrorException;
import com.ngdesk.graphql.services.GraphqlService;
import com.ngdesk.graphql.utilities.DataUtilityForNoAuthCalls;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLError;

@RestController
public class GraphqlControllerForNoAuthCalls {

	@Autowired
	GraphqlService graphQlService;

	@Autowired
	DataUtilityForNoAuthCalls dataUtilityForNoAuthCalls;

	@PostMapping(value = "/noauth/query")
	public ResponseEntity query(@RequestBody String plugins) {

		ExecutionInput executionInput = ExecutionInput.newExecutionInput().query(plugins)
				.context(new HashMap<String, Object>()).build();

		try {
			GraphQL graphQl = dataUtilityForNoAuthCalls.createGraphQlObject();
			graphQlService.graphqlSessions.put("queries", graphQl);

		} catch (Exception e) {
			e.printStackTrace();
		}

		ExecutionResult result = graphQlService.graphqlSessions.get("queries").execute(executionInput);
		List<GraphQLError> errors = result.getErrors();

		if (errors.size() > 0) {
			GraphQLError error = errors.get(0);
			Map<String, Object> errorMap = error.getExtensions();
			if (errorMap != null && errorMap.containsKey("errorCode") && errorMap.containsKey("errorMessage")
					&& errorMap.containsKey("vars")) {

				Integer errorCode = (Integer) errorMap.get("errorCode");
				String errorMessage = (String) errorMap.get("errorMessage");
				String[] vars = (String[]) errorMap.get("vars");

				if (errorCode == 400) {
					throw new BadRequestException(errorMessage, vars);
				} else {
					throw new InternalErrorException("INTERNAL_ERROR");
				}
			}
		}
		return ResponseEntity.ok(result.getData());
	}

}
