package com.ngdesk.graphql.role.dao;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.role.RolesRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class RoleDataFetcher implements DataFetcher<Role> {

	@Autowired
	AuthManager authManager;

	@Autowired
	RolesRepository rolesRepository;

	@Override
	public Role get(DataFetchingEnvironment environment) throws Exception {

		ObjectMapper mapper = new ObjectMapper();

		String companyId = authManager.getUserDetails().getCompanyId();

		// TODO: HANDLE CASE MISMATCH
		if (environment.getSource() != null) {
			try {
				Map<String, Object> source = mapper.readValue(mapper.writeValueAsString(environment.getSource()),
						Map.class);
				String fieldName = environment.getField().getName();
				String roleId = source.get(fieldName).toString();
				Optional<Role> optionalRole = rolesRepository.findById(roleId, "roles_" + companyId);
				if (optionalRole.isPresent()) {
					return optionalRole.get();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

}
