package com.ngdesk.repositories.section;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class CustomSectionRepositoryImpl implements CustomSectionRepository {

	@Autowired
	private MongoOperations mongoOperations;

	@Override
	public Optional<Map<String, Object>> findByVisibleTo(String teamId, String collectionName) {
		Query query = new Query(Criteria.where("_id").is(teamId));

		return Optional.ofNullable(mongoOperations.findOne(query, Map.class, collectionName));
	}

}
