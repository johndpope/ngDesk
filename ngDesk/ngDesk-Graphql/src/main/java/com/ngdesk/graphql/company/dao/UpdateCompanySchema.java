package com.ngdesk.graphql.company.dao;

import java.util.Optional;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.graphql.services.GraphqlService;
import com.ngdesk.graphql.utilities.DataUtility;
import com.ngdesk.repositories.company.CompanyRepository;

import graphql.GraphQL;

@Component
@RabbitListener(queues = "update-company-schema", concurrency = "5")
public class UpdateCompanySchema {
	
	@Autowired
	GraphqlService graphQlService;

	@Autowired
	CompanyRepository companyRepository;

	@Autowired
	DataUtility dataUtility;

	
	@RabbitHandler
	public void execute(String subdomain) {
		subdomain = subdomain.replaceAll("\"", "");
		Optional<Company> optionalCompany = companyRepository.findByCompanySubdomain(subdomain);
		if (optionalCompany.isPresent()) {
			try {
				Company company = optionalCompany.get();
				if (graphQlService.graphqlSessions.containsKey(company.getCompanyId())) {
					GraphQL graphQl = dataUtility.createGraphQlObject(company);
					graphQlService.graphqlSessions.remove(company.getCompanyId());
					graphQlService.graphqlSessions.put(company.getCompanyId(), graphQl);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
}
