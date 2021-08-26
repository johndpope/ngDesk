package com.ngdesk.graphql.catalogue.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.graphql.form.dao.Form;
import com.ngdesk.graphql.role.dao.Role;
import com.ngdesk.repositories.form.FormRepository;
import com.ngdesk.repositories.role.RolesRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class CatalogueFormDataFetcher implements DataFetcher<Form> {

	@Autowired
	AuthManager authManager;

	@Autowired
	FormRepository formRepository;

	@Autowired
	RolesRepository rolesRepository;

	@Override

	public Form get(DataFetchingEnvironment environment) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> source = mapper.readValue(mapper.writeValueAsString(environment.getSource()), Map.class);
		String companyId = authManager.getUserDetails().getCompanyId();
		String moduleId = source.get("moduleId").toString();
		String formId = source.get("formId").toString();

		List<String> teamIds = new ArrayList<String>();
		Optional<Role> optionalRole = rolesRepository.findById(authManager.getUserDetails().getRole(),
				"roles_" + companyId);
		if (optionalRole.isEmpty()) {
			throw new BadRequestException("INVALID_ROLE", null);
		}

		Role role = optionalRole.get();
		if (!role.getName().equals("SystemAdmin")) {
			teamIds = (List<String>) authManager.getUserDetails().getAttributes().get("TEAMS");
		}
		Optional<Form> optionalForm = formRepository.findFormByIdAndTeams(formId, moduleId, companyId, "forms",
				teamIds);
		if (optionalForm.isPresent()) {
			return optionalForm.get();
		}

		return null;
	}
}