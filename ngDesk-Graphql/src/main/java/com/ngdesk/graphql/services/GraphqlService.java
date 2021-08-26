package com.ngdesk.graphql.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.models.DashboardCondition;
import com.ngdesk.graphql.modules.dao.Condition;
import com.ngdesk.graphql.utilities.DataUtility;
import com.ngdesk.repositories.company.CompanyRepository;

import graphql.GraphQL;

@Component
public class GraphqlService {

	public Map<String, GraphQL> graphqlSessions;

	@Autowired
	GraphqlService graphQlService;

	@Autowired
	CompanyRepository companyRepository;

	@Autowired
	DataUtility dataUtility;

	@PostConstruct
	public void init() {
		graphQlService.graphqlSessions = new HashMap<String, GraphQL>();
//		List<Company> companies = companyRepository.findAll(new Query(), "companies");
//		try {
//			int count = 0;
//			for (Company company : companies) {
//				GraphQL graphQl = dataUtility.createGraphQlObject(company);
//				graphQlService.graphqlSessions.put(company.getCompanyId(), graphQl);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

	}

	public List<Condition> convertCondition(List<DashboardCondition> dashboardConditions) {

		List<Condition> conditions = new ArrayList<Condition>();
		dashboardConditions.forEach(dashboardCondition -> {
			Condition condition = new Condition(dashboardCondition.getRequirementType(),
					dashboardCondition.getOperator(), dashboardCondition.getCondition(), dashboardCondition.getValue());
			conditions.add(condition);
		});

		return conditions;

	}

}
