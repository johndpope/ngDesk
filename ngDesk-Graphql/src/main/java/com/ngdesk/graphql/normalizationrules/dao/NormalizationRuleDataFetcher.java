package com.ngdesk.graphql.normalizationrules.dao;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.normalizationrules.NormalizationRuleRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class NormalizationRuleDataFetcher implements DataFetcher<NormalizationRule> {
	@Autowired
	AuthManager authManager;

	@Autowired
	NormalizationRuleRepository normalizationRuleRepository;

		@Override
		public NormalizationRule get(DataFetchingEnvironment environment) {

			String companyId = authManager.getUserDetails().getCompanyId();
			String ruleId = environment.getArgument("ruleId");

			Optional<NormalizationRule> optionalNormalizationRule = normalizationRuleRepository
					.findByRuleIdAndCompanyId(ruleId, companyId, "normalization_rules");
			if (optionalNormalizationRule.isPresent()) {
				return optionalNormalizationRule.get();
			}

			return null;
		}

}
