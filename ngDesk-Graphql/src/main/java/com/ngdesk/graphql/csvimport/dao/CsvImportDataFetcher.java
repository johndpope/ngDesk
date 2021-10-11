package com.ngdesk.graphql.csvimport.dao;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.graphql.role.layout.dao.RoleService;
import com.ngdesk.repositories.csvimport.CsvImportRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class CsvImportDataFetcher implements DataFetcher<CsvImport> {

	@Autowired
	AuthManager authManager;

	@Autowired
	CsvImportRepository csvImportRepository;

	@Autowired
	RoleService roleService;

	@Override
	public CsvImport get(DataFetchingEnvironment environment) throws Exception {

		String csvImportId = environment.getArgument("csvImportId");

		Optional<CsvImport> optionalCsvImport = csvImportRepository
				.findCsvImportById(authManager.getUserDetails().getCompanyId(), csvImportId, "csv_import");
		if (optionalCsvImport.isPresent() && roleService.isSystemAdmin(authManager.getUserDetails().getRole())) {
			return optionalCsvImport.get();
		}
		return null;
	}

}
