package com.ngdesk.repositories.csvimport;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.ngdesk.graphql.csvimport.dao.CsvImport;

public interface CustomCsvImportRepository {

	public int findCsvImportsCount(String companyId, String collectionName);

	public Optional<CsvImport> findCsvImportById(String companyId, String csvImportId, String collectionName);

	public List<CsvImport> findAllCsvImports(Pageable pageable, String companyId, String collectionName);

}
