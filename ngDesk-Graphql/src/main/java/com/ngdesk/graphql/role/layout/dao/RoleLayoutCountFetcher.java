package com.ngdesk.graphql.role.layout.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.role.layout.RoleLayoutRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class RoleLayoutCountFetcher implements DataFetcher<Integer> {

	@Autowired
	AuthManager authManager;

	@Autowired
	RoleLayoutRepository roleLayoutRepository;

	@Override
	public Integer get(DataFetchingEnvironment environment) throws Exception {
		String companyId = authManager.getUserDetails().getCompanyId();
		return roleLayoutRepository.roleLayoutsCount(companyId, "role_layouts");
	}

}
