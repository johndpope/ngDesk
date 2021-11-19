package com.ngdesk.repositories;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;;

public class CustomModuleEntryRepositoryImpl implements CustomModuleEntryRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<List<Map<String, Object>>> findAllTeams(String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("EFFECTIVE_TO").is(null));
		return Optional.ofNullable(mongoOperations.find(new Query(criteria),
				(Class<Map<String, Object>>) (Class) Map.class, collectionName));
	}

	public Optional<List<Map<String, Object>>> getAllEntries(String collectionName) {
		Query query = new Query();
		return Optional.ofNullable(
				mongoOperations.find(new Query(), (Class<Map<String, Object>>) (Class) Map.class, collectionName));
	}
}
