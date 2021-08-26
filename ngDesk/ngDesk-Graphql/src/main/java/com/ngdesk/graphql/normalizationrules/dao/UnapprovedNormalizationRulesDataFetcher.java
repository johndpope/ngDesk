package com.ngdesk.graphql.normalizationrules.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.normalizationrules.NormalizationRuleRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class UnapprovedNormalizationRulesDataFetcher implements DataFetcher<List<NormalizationRule>> {
	@Autowired
	AuthManager authManager;

	@Autowired
	NormalizationRuleRepository normalizationRuleRepository;

	@Override
	public List<NormalizationRule> get(DataFetchingEnvironment environment) {

		Integer page = environment.getArgument("pageNumber");
		Integer pageSize = environment.getArgument("pageSize");
		String sortBy = environment.getArgument("sortBy");
		String orderBy = environment.getArgument("orderBy");

		if (page == null || page < 0) {
			page = 0;
		}

		if (pageSize == null || pageSize < 0) {
			pageSize = 20;
		}
		Sort sort = null;
		if (sortBy == null) {
			sort = Sort.by("DATE_CREATED");
		} else {
			sort = Sort.by(sortBy);
		}
		if (orderBy == null) {
			sort = sort.descending();
		} else {
			if (orderBy.equalsIgnoreCase("asc")) {
				sort = sort.ascending();
			} else {
				sort = sort.descending();
			}
		}
		Pageable pageable = PageRequest.of(page, pageSize, sort);

		Optional<List<NormalizationRule>> optionalNormalizationRules = normalizationRuleRepository
				.findAllUnapprovedNormaliztionRules(pageable, "normalization_rules");
		if (optionalNormalizationRules.isPresent()) {
			return optionalNormalizationRules.get();
		}
		return null;
	}

}
