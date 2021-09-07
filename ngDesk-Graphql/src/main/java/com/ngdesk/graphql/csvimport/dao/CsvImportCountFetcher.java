package com.ngdesk.graphql.csvimport.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.csvimport.CsvImportRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class CsvImportCountFetcher implements DataFetcher<Integer>{

	@Autowired
	AuthManager authManager;

	@Autowired
	CsvImportRepository csvImportRepository; 

	@Override
	public Integer get(DataFetchingEnvironment environment) throws Exception {
		String companyId = authManager.getUserDetails().getCompanyId();
		return csvImportRepository.findCsvImportsCount(companyId, "csv_import");
	}
}
