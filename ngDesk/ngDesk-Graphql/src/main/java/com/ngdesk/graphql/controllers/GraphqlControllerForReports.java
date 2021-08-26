package com.ngdesk.graphql.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.exceptions.InternalErrorException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.commons.managers.SessionManager;
import com.ngdesk.graphql.company.dao.Company;
import com.ngdesk.graphql.services.GraphqlService;
import com.ngdesk.graphql.utilities.DataUtility;
import com.ngdesk.repositories.company.CompanyRepository;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLError;

@RestController
public class GraphqlControllerForReports {
	@Autowired
	GraphqlService graphQlService;

	@Autowired
	AuthManager authManager;

	@Autowired
	DataUtility dataUtility;

	@Autowired
	CompanyRepository companyRepository;

	@Autowired
	SessionManager sessionManager;

	@Autowired
	ControllerService controllerService;

	@PostMapping(value = "/reports/data")
	public ResponseEntity query(@RequestBody ReportDataInput reportDataInput) {

		String companyId = authManager.getUserDetails().getCompanyId();
		ExecutionInput executionInput = ExecutionInput.newExecutionInput().query(reportDataInput.getQuery())
				.context(new HashMap<String, Object>()).build();
		sessionManager.getSessionInfo().put("conditions", reportDataInput.getConditions());

		try {
			if (!graphQlService.graphqlSessions.containsKey(companyId)) {
				Optional<Company> optionalCompany = companyRepository.findById(companyId, "companies");
				if (optionalCompany.isPresent()) {
					Company company = optionalCompany.get();
					GraphQL graphQl = dataUtility.createGraphQlObject(company);
					graphQlService.graphqlSessions.put(company.getCompanyId(), graphQl);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();

		}
		ExecutionResult result = graphQlService.graphqlSessions.get(companyId).execute(executionInput);
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

	@PostMapping(value = "/reports/generate")
	public void reportGenerate(@RequestBody ReportInput reportInput) {

		String companyId = authManager.getUserDetails().getCompanyId();

		ExecutionInput executionInput = ExecutionInput.newExecutionInput().query(reportInput.getQuery())
				.context(new HashMap<String, Object>()).build();
		sessionManager.getSessionInfo().put("reports", reportInput);

		if (!graphQlService.graphqlSessions.containsKey(companyId)) {
			try {
				Optional<Company> optionalCompany = companyRepository.findById(companyId, "companies");
				if (optionalCompany.isPresent()) {
					Company company = optionalCompany.get();
					GraphQL graphQl = dataUtility.createGraphQlObject(company);
					graphQlService.graphqlSessions.put(company.getCompanyId(), graphQl);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			ExecutionResult result = graphQlService.graphqlSessions.get(companyId).execute(executionInput);

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
			ObjectMapper mapper = new ObjectMapper();

			Map<String, Object> resultDataMap = mapper.readValue(mapper.writeValueAsString(result.getData()),
					Map.class);

			controllerService.generateCsvForEntries(resultDataMap, reportInput.getFieldNames(),
					reportInput.getFileName(), reportInput.getEmailIds());

			if (resultDataMap == null) {
				throw new BadRequestException("INVALID_QUERY", null);
			}

		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}
}
