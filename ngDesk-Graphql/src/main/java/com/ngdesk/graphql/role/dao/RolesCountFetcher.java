package com.ngdesk.graphql.role.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.role.RolesRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class RolesCountFetcher implements DataFetcher<Integer> {
	
	@Autowired
	AuthManager authManager;

	@Autowired
	RolesRepository rolesRepository;

	@Override
	public Integer get(DataFetchingEnvironment environment) throws Exception {
		String companyId = authManager.getUserDetails().getCompanyId();
		

		return rolesRepository.count("roles_"+companyId);
	}

}
