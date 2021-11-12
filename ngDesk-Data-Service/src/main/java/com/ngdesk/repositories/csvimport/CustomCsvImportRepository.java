package com.ngdesk.repositories.csvimport;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.ngdesk.data.csvimport.dao.CsvImport;

public interface CustomCsvImportRepository {

	public Optional<List<CsvImport>> findEntriesByVariable(String variable, String value, String collectionName);

	public void updateEntry(String dataId, String variable, Object value, String collectionName);

	public void updateEntry(String dataId, Map<String, Object> entry, String collectionName);

	public void addToEntrySet(String dataId, String variable, Object value, String collectionName);

}
