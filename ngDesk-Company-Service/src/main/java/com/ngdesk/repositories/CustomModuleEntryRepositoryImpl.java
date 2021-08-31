package com.ngdesk.repositories;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;

public class CustomModuleEntryRepositoryImpl implements CustomModuleEntryRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<List<Map<String, Object>>> findAllTeams(String collectionName) {

		return Optional.ofNullable(
				mongoOperations.find(new Query(), (Class<Map<String, Object>>) (Class) Map.class, collectionName));
	}

}
