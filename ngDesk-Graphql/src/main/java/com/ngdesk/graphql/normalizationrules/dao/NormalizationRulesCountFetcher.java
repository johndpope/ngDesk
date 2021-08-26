package com.ngdesk.graphql.normalizationrules.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.normalizationrules.NormalizationRuleRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class NormalizationRulesCountFetcher implements DataFetcher<Integer>{
	@Autowired
	AuthManager authManager;

	@Autowired
	NormalizationRuleRepository normalizationRuleRepository;

	@Override
	public Integer get(DataFetchingEnvironment environment) {
		String companyId=authManager.getUserDetails().getCompanyId();
		return normalizationRuleRepository.normalizationRulesCount(companyId,
				"normalization_rules");

	}


}
