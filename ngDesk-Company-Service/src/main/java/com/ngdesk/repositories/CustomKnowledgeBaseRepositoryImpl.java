package com.ngdesk.repositories;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;

public class CustomKnowledgeBaseRepositoryImpl implements CustomKnowledgeBaseRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public void saveAll(List<Map<String, Object>> payload, String collectionName) {
		mongoOperations.insert(payload, collectionName);

	}

}
