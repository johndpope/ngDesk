package com.ngdesk.graphql.enterprisesearch.dao;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.enterprisesearch.EnterpriseSearchRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class EnterpriseSearchDataFetcher implements DataFetcher<EnterpriseSearch> {
	@Autowired
	AuthManager authManager;

	@Autowired
	EnterpriseSearchRepository enterpriseSearchRepository;

	@Override
	public EnterpriseSearch get(DataFetchingEnvironment environment) {

		String companyId = authManager.getUserDetails().getCompanyId();
		String enterpriseSearchId = environment.getArgument("enterpriseSearchId");

		Optional<EnterpriseSearch> optional = enterpriseSearchRepository.findByCompanyIdAndId(companyId,
				enterpriseSearchId, "Enterprise_Search");
		if (optional.isPresent()) {
			return optional.get();
		}
		return null;
	}
}
