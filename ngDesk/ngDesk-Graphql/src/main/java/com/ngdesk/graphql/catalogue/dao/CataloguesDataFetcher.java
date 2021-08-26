package com.ngdesk.graphql.catalogue.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.graphql.role.dao.Role;
import com.ngdesk.repositories.catalogue.CatalogueRepository;
import com.ngdesk.repositories.role.RolesRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class CataloguesDataFetcher implements DataFetcher<List<Catalogue>> {

	@Autowired
	AuthManager authManager;

	@Autowired
	CatalogueRepository repository;
	
	@Autowired
	RolesRepository rolesRepository;

	@Override
	public List<Catalogue> get(DataFetchingEnvironment environment) throws Exception {
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
		
		Optional<Role> optionalRole = rolesRepository.findById(authManager.getUserDetails().getRole(),
				"roles_" + companyId);
		if (optionalRole.isEmpty()) {
			throw new BadRequestException("INVALID_ROLE", null);
		}

		Role role = optionalRole.get();
		if (role.getName().equals("SystemAdmin")) {
			return repository.findAllCatalogues(companyId, pageable, "catalogues");
		}else {
			List<String> teamIds = (List<String>) authManager.getUserDetails().getAttributes().get("TEAMS");
			return repository.findAllCataloguesWithTeams(companyId, pageable, "catalogues", teamIds);
		}
		

	}

}
