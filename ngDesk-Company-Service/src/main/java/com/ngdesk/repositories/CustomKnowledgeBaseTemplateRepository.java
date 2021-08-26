package com.ngdesk.repositories;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CustomKnowledgeBaseTemplateRepository {

	public Optional<List<Map<String, Object>>> findAllTemplates(String collectionName);
}
