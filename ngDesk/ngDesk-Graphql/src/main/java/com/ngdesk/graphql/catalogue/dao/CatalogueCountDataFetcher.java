package com.ngdesk.graphql.catalogue.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.graphql.role.dao.Role;
import com.ngdesk.repositories.catalogue.CatalogueRepository;
import com.ngdesk.repositories.role.RolesRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class CatalogueCountDataFetcher implements DataFetcher<Long> {

	@Autowired
	CatalogueRepository repository;

	@Autowired
	AuthManager authManager;
	
	@Autowired
	RolesRepository rolesRepository;

	@Override
	public Long get(DataFetchingEnvironment environment) throws Exception {
		String companyId = authManager.getUserDetails().getCompanyId();
		
		Optional<Role> optionalRole = rolesRepository.findById(authManager.getUserDetails().getRole(),
				"roles_" + companyId);
		if (optionalRole.isEmpty()) {
			throw new BadRequestException("INVALID_ROLE", null);
		}

		Role role = optionalRole.get();
		
		List<String> teamIds = new ArrayList<String>();
		
		if (!role.getName().equals("SystemAdmin")) {
			teamIds = (List<String>) authManager.getUserDetails().getAttributes().get("TEAMS");
			System.out.println(teamIds);
		}
		Optional<Long> optionalCatalogueCount = repository.catalogueCountWithTeams(companyId,teamIds, "catalogues");
		return optionalCatalogueCount.get();
		
	}

}
