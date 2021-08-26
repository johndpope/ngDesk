package com.ngdesk.graphql.sam.file.rule.dao;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.sam.file.rule.SamFileRuleRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class SamFileRuleDataFetcher implements DataFetcher<SamFileRule> {

	@Autowired
	SamFileRuleRepository samFileRuleRepository;

	@Autowired
	AuthManager authManager;

	@Override
	public SamFileRule get(DataFetchingEnvironment environment) {
		String companyId = authManager.getUserDetails().getCompanyId();
		String id = environment.getArgument("id");

		Optional<SamFileRule> optionalRule = samFileRuleRepository.findByCompanyIdAndId(companyId, id,
				"sam_file_rules");

		if (optionalRule.isPresent()) {
			return optionalRule.get();
		}

		return null;
	}

}
