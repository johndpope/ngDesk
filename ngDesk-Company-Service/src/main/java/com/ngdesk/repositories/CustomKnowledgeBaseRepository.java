package com.ngdesk.repositories;

import java.util.List;
import java.util.Map;

public interface CustomKnowledgeBaseRepository {

	public void saveAll(List<Map<String, Object>> payload, String collectionName);
}
