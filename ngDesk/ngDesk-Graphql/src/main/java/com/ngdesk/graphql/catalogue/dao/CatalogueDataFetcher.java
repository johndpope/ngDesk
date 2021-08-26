package com.ngdesk.graphql.catalogue.dao;

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
public class CatalogueDataFetcher implements DataFetcher<Catalogue> {

	@Autowired
	AuthManager authManager;

	@Autowired
	CatalogueRepository repository;

	@Autowired
	RolesRepository rolesRepository;

	@Override
	public Catalogue get(DataFetchingEnvironment environment) throws Exception {
		String companyId = authManager.getUserDetails().getCompanyId();
		String catalogueId = environment.getArgument("catalogueId");

		Optional<Role> optionalRole = rolesRepository.findById(authManager.getUserDetails().getRole(),
				"roles_" + companyId);
		if (optionalRole.isEmpty()) {
			throw new BadRequestException("INVALID_ROLE", null);
		}

		Role role = optionalRole.get();
		if (role.getName().equals("SystemAdmin")) {
			Optional<Catalogue> optionalCatalogue = repository.findCatalogueById(companyId, catalogueId, "catalogues");
			if (optionalCatalogue.isPresent()) {
				return optionalCatalogue.get();
			}
		} else {

			List<String>  teamIds = (List<String> ) authManager.getUserDetails().getAttributes().get("TEAMS");
			
			
			Optional<Catalogue> optionalCatalogue = repository.findCatalogueByIdAndTeams(companyId, catalogueId, "catalogues",teamIds);
			if (optionalCatalogue.isPresent()) {
				return optionalCatalogue.get();
			}
		}

		return null;
	}

}
