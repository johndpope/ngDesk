package com.ngdesk.graphql.form.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.graphql.role.dao.Role;
import com.ngdesk.repositories.form.FormRepository;
import com.ngdesk.repositories.role.RolesRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class FormDataFetcher implements DataFetcher<Form> {

	@Autowired
	AuthManager authManager;

	@Autowired
	FormRepository formRepository;
	
	@Autowired
	RolesRepository rolesRepository;

	@Override
	public Form get(DataFetchingEnvironment environment) throws Exception {

		String companyId = authManager.getUserDetails().getCompanyId();
		String moduleId = environment.getArgument("moduleId");
		String formId = environment.getArgument("formId");
		
		Optional<Role> optionalRole = rolesRepository.findById(authManager.getUserDetails().getRole(),
				"roles_" + companyId);
		if (optionalRole.isEmpty()) {
			throw new BadRequestException("INVALID_ROLE", null);
		}

		Role role = optionalRole.get();
		
		if (role.getName().equals("SystemAdmin")) {
			Optional<Form> optionalForm = formRepository.findFormById(formId, moduleId, companyId, "forms");
			if (optionalForm.isPresent()) {
				return optionalForm.get();
			}

		}else {
			List<String> teamIds = (List<String>) authManager.getUserDetails().getAttributes().get("TEAMS");
			Optional<Form> optionalForm = formRepository.findFormByIdAndTeams(formId, moduleId, companyId, "forms",teamIds);
			if (optionalForm.isPresent()) {
				return optionalForm.get();
			}
		}


		return null;
	}
}
