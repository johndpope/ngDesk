package com.ngdesk.graphql.sam.file.rule.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.sam.file.rule.SamFileRuleRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class SamFileRuleCountFetcher implements DataFetcher<Integer> {

	@Autowired
	SamFileRuleRepository samFileRuleRepository;

	@Autowired
	AuthManager authManager;

	@Override
	public Integer get(DataFetchingEnvironment environment) {
		String companyId = authManager.getUserDetails().getCompanyId();
		return samFileRuleRepository.count(companyId, "sam_file_rules");
	}

}
