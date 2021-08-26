package com.ngdesk.repositories.section;

import java.util.Map;
import java.util.Optional;

public interface CustomSectionRepository {
	public Optional<Map<String, Object>> findByVisibleTo(String teamId, String collectionName);

}
