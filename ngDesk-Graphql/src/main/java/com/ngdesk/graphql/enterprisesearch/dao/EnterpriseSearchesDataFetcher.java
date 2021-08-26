package com.ngdesk.graphql.enterprisesearch.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.enterprisesearch.EnterpriseSearchRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class EnterpriseSearchesDataFetcher implements DataFetcher<List<EnterpriseSearch>> {
	@Autowired
	AuthManager authManager;

	@Autowired
	EnterpriseSearchRepository enterpriseSearchRepository;

	@Override
	public List<EnterpriseSearch> get(DataFetchingEnvironment environment) {

		String companyId = authManager.getUserDetails().getCompanyId();
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

		Optional<List<EnterpriseSearch>> optional = enterpriseSearchRepository
				.findAllEnterpriseSearchIdByCompanyId(pageable, companyId, "Enterprise_Search");
		if (!optional.isEmpty()) {
			return optional.get();
		}
		return null;
	}
}
