package com.ngdesk.graphql.slas.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;

import com.ngdesk.repositories.sla.SLARepository;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class SLAsDataFetcher implements DataFetcher<List<SLA>> {

	@Autowired
	AuthManager authManager;

	@Autowired
	SLARepository slaRepository;

	@Override
	public List<SLA> get(DataFetchingEnvironment environment) throws Exception {

		String companyId = authManager.getUserDetails().getCompanyId();
		String moduleId = environment.getArgument("moduleId");
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
			sort = Sort.by("dateCreated");
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
		
		if(moduleId != null) {
			return slaRepository.findAllSlas(companyId, moduleId, pageable, "slas");
		}
		return null;
	}

}