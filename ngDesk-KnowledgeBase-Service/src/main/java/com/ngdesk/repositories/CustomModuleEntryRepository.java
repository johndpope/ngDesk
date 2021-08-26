package com.ngdesk.repositories;

import java.util.Map;
import java.util.Optional;

public interface CustomModuleEntryRepository {

	public Optional<Map<String, Object>> findEntryById(String entryId, String collectionName);

	public Optional<Map<String, Object>> findTeamById(String teamId);

}
