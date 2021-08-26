package com.ngdesk.graphql.form.dao;

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
import com.ngdesk.repositories.form.FormRepository;
import com.ngdesk.repositories.role.RolesRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class FormsDataFetcher implements DataFetcher<List<Form>> {

	@Autowired
	AuthManager authManager;

	@Autowired
	FormRepository formRepository;
	
	@Autowired
	RolesRepository rolesRepository;

	@Override
	public List<Form> get(DataFetchingEnvironment environment) throws Exception {

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
		
		Optional<Role> optionalRole = rolesRepository.findById(authManager.getUserDetails().getRole(),
				"roles_" + companyId);
		if (optionalRole.isEmpty()) {
			throw new BadRequestException("INVALID_ROLE", null);
		}

		Role role = optionalRole.get();
		
		if (role.getName().equals("SystemAdmin")) {
			return formRepository.findAllForms(companyId, moduleId, pageable, "forms");
		}else {
			List<String> teamIds = (List<String>) authManager.getUserDetails().getAttributes().get("TEAMS");
			return formRepository.findAllFormsWithTeams(companyId, moduleId, pageable, "forms", teamIds);
		}
	}
}
