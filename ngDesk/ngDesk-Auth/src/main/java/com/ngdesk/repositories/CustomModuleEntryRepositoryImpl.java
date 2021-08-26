package com.ngdesk.repositories;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

@Repository
public class CustomModuleEntryRepositoryImpl implements CustomModuleEntryRepository {

	@Autowired
	private MongoOperations mongoOperations;

	@Override
	public Optional<Map<String, Object>> findUserById(String userUuid, String collectionName) {
		Assert.notNull(userUuid, "The given userUUID must not be null!");
		Assert.notNull(collectionName, "The given collectionName must not be null!");
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("USER_UUID").is(userUuid),
				Criteria.where("EFFECTIVE_TO").is(null));
		Query query = new Query(criteria);
		query.fields().exclude("PASSWORD");
		query.fields().exclude("META_DATA");
		return Optional.ofNullable(mongoOperations.findOne(query, Map.class, collectionName));
	}

	@Override
	public Optional<List<Map>> findUsersByRoleId(String roleId, String collectionName) {

		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("ROLE").is(roleId),
				Criteria.where("EFFECTIVE_TO").is(null));
		Query query = new Query(criteria);
		return Optional.ofNullable(mongoOperations.find(query, Map.class, collectionName));
	}

	@Override
	public Optional<List<Map>> findAllByCollectionName(String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("EFFECTIVE_TO").is(null));
		Query query = new Query(criteria);
		return Optional.ofNullable(mongoOperations.find(query, Map.class, collectionName));
	}

	public Optional<Map<String, Object>> findUserByEmail(String emailAddress, String collectionName) {
		Criteria criteria = new Criteria();
		Query query = new Query(criteria.where("EMAIL_ADDRESS").is(emailAddress));
		return Optional.ofNullable(mongoOperations.findOne(query, Map.class, collectionName));
	}

	@Override
	public long getCountOfPayingUsers(String roleId, String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("EFFECTIVE_TO").is(null), Criteria.where("DELETED").is(false),
				Criteria.where("ROLE").ne(roleId));
		Query query = new Query(criteria);
		return mongoOperations.count(query, collectionName);
	}

	@Override
	public Map<String, Object> findUserByUuid(String collectionName, String userUuid) {
		Query query = new Query();
		query.addCriteria(Criteria.where("USER_UUID").is(userUuid));
		return mongoOperations.findOne(query, Map.class, collectionName);
	}

	@Override
	public Optional<Map<String, Object>> updateUserEntry(Map<String, Object> entry, String collectionName) {
		Query query = new Query();
		query.addCriteria(Criteria.where("USER_UUID").is(entry.get("USER_UUID").toString()));
		return Optional.ofNullable(mongoOperations.findAndReplace(query, entry, collectionName));
	}

}