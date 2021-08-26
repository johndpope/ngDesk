package com.ngdesk.repositories;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.util.Assert;

public class CustomModuleEntryRepositoryImpl implements CustomModuleEntryRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<Map<String, Object>> findEntryByVariable(String fieldName, String value, String collectionName) {

		Query query = new Query();
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where(fieldName).is(value), Criteria.where("EFFECTIVE_TO").is(null),
				Criteria.where("DELETED").is(false));
		query.addCriteria(criteria);

		return Optional.ofNullable(mongoOperations.findOne(query, Map.class, collectionName));
	}

	@Override
	public Optional<Map<String, Object>> findOneModule(String name, String collectionName) {

		Assert.notNull(name, "The given  name must not be null!");
		Query query = new Query(Criteria.where("NAME").is(name));
		return Optional.ofNullable(mongoOperations.findOne(query, Map.class, collectionName));

	}

	@Override
	public void updateEntry(Map<String, Object> entry, String variable, String collectionName) {
		Query query = new Query();
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("_id").is(entry.get("_id").toString()),
				Criteria.where("EFFECTIVE_TO").is(null), Criteria.where("DELETED").is(false));
		query.addCriteria(criteria);

		mongoOperations.updateFirst(query, new Update().update(variable, entry.get(variable)), collectionName);
	}

}
