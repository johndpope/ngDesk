package com.ngdesk.graphql.enterprisesearch.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.enterprisesearch.EnterpriseSearchRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class EnterpriseSearchCountFetcher implements DataFetcher<Integer> {
	@Autowired
	AuthManager authManager;

	@Autowired
	EnterpriseSearchRepository enterpriseSearchRepository;

	@Override
	public Integer get(DataFetchingEnvironment environment) {
		String companyId = authManager.getUserDetails().getCompanyId();
		return enterpriseSearchRepository.enterpriseSearchCount(companyId, "Enterprise_Search");
	}
}
