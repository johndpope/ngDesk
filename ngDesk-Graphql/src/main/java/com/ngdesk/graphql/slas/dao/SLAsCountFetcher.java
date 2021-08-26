package com.ngdesk.graphql.slas.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.sla.SLARepository;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class SLAsCountFetcher implements DataFetcher<Integer> {

	@Autowired
	AuthManager authManager;

	@Autowired
	SLARepository slaRepository;

	@Override
	public Integer get(DataFetchingEnvironment environment) throws Exception {
		String companyId = authManager.getUserDetails().getCompanyId();
		String moduleId = environment.getArgument("moduleId");

		return slaRepository.count(companyId, moduleId, "slas");
	}

}