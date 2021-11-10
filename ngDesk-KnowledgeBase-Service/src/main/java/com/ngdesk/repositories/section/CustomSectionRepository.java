package com.ngdesk.repositories.section;

import java.util.Map;
import java.util.Optional;

import com.ngdesk.knowledgebase.section.dao.Section;

public interface CustomSectionRepository {
	public Optional<Map<String, Object>> findByVisibleTo(String teamId, String collectionName);

	public int getCount(String collectionName);

	public Optional<Section> validateDuplicateSection(String name, String string);
}
