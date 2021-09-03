package com.ngdesk.repositories;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CustomModuleEntryRepository {

	public Optional<List<Map<String, Object>>> findAllTeams(String collectionName);

}
