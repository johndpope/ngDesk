package com.ngdesk.graphql.sam.file.rule.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.sam.file.rule.SamFileRuleRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class AllSamFileRulesDataFetcher implements DataFetcher<List<SamFileRule>> {

	@Autowired
	SamFileRuleRepository samFileRuleRepository;

	@Autowired
	AuthManager authManager;

	@Override
	public List<SamFileRule> get(DataFetchingEnvironment environment) {
		String companyId = authManager.getUserDetails().getCompanyId();
		Integer pageNumber = environment.getArgument("pageNumber");
		Integer pageSize = environment.getArgument("pageSize");
		String sortBy = environment.getArgument("sortBy");
		String orderBy = environment.getArgument("orderBy");
		if (pageNumber == null || pageNumber < 0) {
			pageNumber = 0;
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
		Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
		List<SamFileRule> rules = samFileRuleRepository.findAllRulesWithPagination(companyId, pageable,
				"sam_file_rules");
		return rules;

	}

}
