package com.ngdesk.repositories;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import com.ngdesk.commons.managers.AuthManager;

@Repository
public class CustomModuleEntryRepositoryImpl implements CustomModuleEntryRepository {
	@Autowired
	AuthManager authManager;

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<Map<String, Object>> findEntryById(String entryId, String collectionName) {
		Assert.notNull(entryId, "entryid must not be null");
		Assert.notNull(collectionName, "collection name must not be null");
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("EFFETIVE_TO").is(null),
				Criteria.where("_id").is(entryId));
		Query query = new Query(criteria);

		return Optional.ofNullable(mongoOperations.findOne(query, Map.class, collectionName));
	}

	@Override
	public Optional<Map<String, Object>> findTeamById(String teamId) {
		String collectionName = "Teams_" + authManager.getUserDetails().getCompanyId();

		Criteria criteria = new Criteria();

		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("EFFECTIVE_TO").is(null),
				Criteria.where("_id").is(teamId));

		Query query = new Query(criteria);
		query.fields().include("NAME");

		return Optional.ofNullable(mongoOperations.findOne(query, Map.class, collectionName));

	}

}
