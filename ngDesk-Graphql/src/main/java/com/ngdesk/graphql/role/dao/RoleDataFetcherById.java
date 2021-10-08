package com.ngdesk.graphql.role.dao;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.role.RolesRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class RoleDataFetcherById implements DataFetcher<Role> {

	@Autowired
	AuthManager authManager;

	@Autowired
	RolesRepository rolesRepository;

	@Override
	public Role get(DataFetchingEnvironment environment) throws Exception {
		String companyId = authManager.getUserDetails().getCompanyId();
		String roleId = environment.getArgument("roleId");
		Optional<Role> optionalRole = rolesRepository.findById(roleId, "roles_"+companyId);
		if (optionalRole.isPresent()) {
			return optionalRole.get();
		}
		return null;
	}

}
