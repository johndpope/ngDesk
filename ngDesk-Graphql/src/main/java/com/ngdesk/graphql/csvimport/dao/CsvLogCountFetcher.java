package com.ngdesk.graphql.csvimport.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.csvimport.CsvImportRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class CsvLogCountFetcher implements DataFetcher<Integer>{

	@Autowired
	AuthManager authManager;

	@Autowired
	CsvImportRepository csvImportRepository;

	@Override
	public Integer get(DataFetchingEnvironment environment) throws Exception {
		String csvImportId = environment.getArgument("csvImportId");
		Optional<CsvImport> optionalCsvImport = csvImportRepository
				.findCsvImportById(authManager.getUserDetails().getCompanyId(), csvImportId, "csv_import");
		
		if (optionalCsvImport.isPresent()) {
			List<CsvImportLog> csvLogs = optionalCsvImport.get().getLogs();
			if(csvLogs == null || csvLogs.isEmpty()) {
				return 0;
			}
			return csvLogs.size();
		}
		return 0;
	}
}
