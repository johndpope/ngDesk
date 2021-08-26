package com.ngdesk.repositories;

import java.util.Map;
import java.util.Optional;

public interface CustomModuleEntryRepository {

	public Optional<Map<String, Object>> findEntryByVariable(String fieldName, String value, String collectionName);

	public Optional<Map<String, Object>> findOneModule(String name, String collectionName);

	public void updateEntry(Map<String, Object> entry, String variable, String collectionName);

}
