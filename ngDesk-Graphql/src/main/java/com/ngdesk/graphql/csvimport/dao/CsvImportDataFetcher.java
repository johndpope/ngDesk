package com.ngdesk.graphql.csvimport.dao;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.graphql.role.dao.Role;
import com.ngdesk.repositories.csvimport.CsvImportRepository;
import com.ngdesk.repositories.role.RolesRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class CsvImportDataFetcher implements DataFetcher<CsvImport> {

	@Autowired
	AuthManager authManager;

	@Autowired
	CsvImportRepository csvImportRepository;

	@Autowired
	RolesRepository rolesRepository;

	@Override
	public CsvImport get(DataFetchingEnvironment environment) throws Exception {

		String companyId = authManager.getUserDetails().getCompanyId();
		String csvImportId = environment.getArgument("csvImportId");
		String roleId = authManager.getUserDetails().getRole();
		Optional<Role> optionalRole = rolesRepository.findById(roleId, "roles_" + companyId);
		Role role = optionalRole.get();

		Optional<CsvImport> optionalCsvImport = csvImportRepository.findCsvImportById(companyId, csvImportId,
				"csv_import");
		if (optionalCsvImport.isPresent()) {
			if (role != null && role.getName().equals("SystemAdmin")) {
				return optionalCsvImport.get();
			}
		}
		return null;
	}

}
