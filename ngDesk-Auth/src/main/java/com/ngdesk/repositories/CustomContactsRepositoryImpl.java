package com.ngdesk.repositories;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

public class CustomContactsRepositoryImpl implements CustomContactsRepository{

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<Map<String, Object>> findContactsByContactId(String contactId, String collectionName) {
		Criteria criteria = new Criteria();
		Query query = new Query(Criteria.where("_id").is(contactId));
		return Optional.ofNullable(mongoOperations.findOne(query, Map.class, collectionName));
	}
}
