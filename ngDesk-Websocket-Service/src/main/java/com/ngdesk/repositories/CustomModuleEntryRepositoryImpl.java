package com.ngdesk.repositories;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import com.mongodb.client.model.Updates;
import com.ngdesk.data.dao.DiscussionMessage;
import com.ngdesk.websocket.companies.dao.Phone;

@Repository
public class CustomModuleEntryRepositoryImpl implements CustomModuleEntryRepository {

	@Autowired
	private MongoOperations mongoOperations;

	@Override
	public Optional<Map<String, Object>> findEntryById(String entryId, String collectionName) {
		Assert.notNull(entryId, "The given id must not be null!");
		Assert.notNull(collectionName, "The given collectionName must not be null!");
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("_id").is(entryId),
				Criteria.where("EFFECTIVE_TO").is(null));
		Query query = new Query(criteria);
		query.fields().exclude("PASSWORD");
		query.fields().exclude("META_DATA");

		return Optional.ofNullable(mongoOperations.findOne(query, Map.class, collectionName));
	}

	@Override
	public void addDiscussionToEntry(DiscussionMessage message, String discussionFieldName, String entryId,
			String collectionName, String variable, Object value) {
		Assert.notNull(message, "The given message must not be null!");
		Assert.notNull(collectionName, "The given collectionName must not be null!");
		Assert.notNull(entryId, "The given id must not be null!");
		Assert.notNull(discussionFieldName, "The given discussionFieldName must not be null!");

		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("_id").is(entryId),
				Criteria.where("EFFECTIVE_TO").is(null));
		Query query = new Query(criteria);

		Update update = new Update();
		update.addToSet(discussionFieldName, message);
		update.set(variable, value);
		mongoOperations.updateFirst(query, update, collectionName);

	}

	@Override
	public List<Map<String, Object>> findTeamsByIds(List<String> teamIds, String collectionName) {
		Assert.notNull(teamIds, "The given message must not be null!");
		Assert.notNull(collectionName, "The given collectionName must not be null!");

		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("_id").in(teamIds),
				Criteria.where("EFFECTIVE_TO").is(null));

		return mongoOperations.find(new Query(criteria), (Class<Map<String, Object>>) (Class) Map.class,
				collectionName);
	}

	@Override
	public List<Map<String, Object>> findAllEntriesExceptGivenId(String fieldName, int value, String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where(fieldName).is(value),
				Criteria.where("EFFECTIVE_TO").is(null));
		Query query = new Query(criteria);
		return mongoOperations.find(query, (Class<Map<String, Object>>) (Class) Map.class, collectionName);
	}

	@Override
	public Optional<Map<String, Object>> findBySessionUuid(String uuid, String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("SESSION_UUID").is(uuid),
				Criteria.where("EFFECTIVE_TO").is(null));
		return Optional.ofNullable(mongoOperations.findOne(new Query(criteria), Map.class, collectionName));

	}

	@Override
	public Optional<Map<String, Object>> findTeamByName(String teamName, String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("NAME").is(teamName),
				Criteria.where("EFFECTIVE_TO").is(null));
		return Optional.ofNullable(mongoOperations.findOne(new Query(criteria), Map.class, collectionName));
	}

	@Override
	public Optional<Map<String, Object>> findUserByEmailAddressIncludingDeleted(String emailAddress,
			String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("EMAIL_ADDRESS").is(emailAddress), Criteria.where("EFFECTIVE_TO").is(null));
		return Optional.ofNullable(mongoOperations.findOne(new Query(criteria), Map.class, collectionName));
	}

	@Override
	public Optional<Map<String, Object>> findAccountByName(String accountName, String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("ACCOUNT_NAME").is(accountName),
				Criteria.where("EFFECTIVE_TO").is(null));
		return Optional.ofNullable(mongoOperations.findOne(new Query(criteria), Map.class, collectionName));
	}

	@Override
	public void updateTeamUser(String userId, String teamName, String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("NAME").is(teamName),
				Criteria.where("EFFECTIVE_TO").is(null));
		Query query = new Query(criteria);
		Update update = new Update();
		update.addToSet("USERS", userId);
		mongoOperations.updateFirst(query, update, collectionName);
	}

	@Override
	public void removeTeamUser(String userId, String teamName, String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("NAME").is(teamName),
				Criteria.where("EFFECTIVE_TO").is(null));
		Query query = new Query(criteria);
		Update update = new Update();
		update.pull("USERS", userId);
		mongoOperations.updateFirst(query, update, collectionName);

	}

	@Override
	public Optional<Map<String, Object>> findAndReplace(String id, Map<String, Object> entry, String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("_id").is(id),
				Criteria.where("EFFECTIVE_TO").is(null));
		Query query = new Query(criteria);
		return Optional.ofNullable(mongoOperations.findAndReplace(query, entry, collectionName));

	}

	@Override
	public void setUserPhoneNumberAndDeletedToFalse(String emailAddress, Phone phone, String collectionName) {
		Criteria criteria = new Criteria();
		criteria.where("EMAIL_ADDRESS").is(emailAddress);
		Query query = new Query(criteria);
		Update update = new Update();
		update.set("PHONE_NUMBER", phone);
		update.set("DELETED", false);
		mongoOperations.updateFirst(query, update, collectionName);

	}

	@Override
	public void setUserDeletedToFalse(String emailAddress, String collectionName) {
		Criteria criteria = new Criteria();
		criteria.where("EMAIL_ADDRESS").is(emailAddress);
		Query query = new Query(criteria);
		Update update = new Update();
		update.set("DELETED", false);
		mongoOperations.updateFirst(query, update, collectionName);
	}

	@Override
	public Optional<Map<String, Object>> findUserByEmailAddress(String emailAddress, String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("EMAIL_ADDRESS").is(emailAddress),
				Criteria.where("EFFECTIVE_TO").is(null));
		return Optional.ofNullable(mongoOperations.findOne(new Query(criteria), Map.class, collectionName));
	}
}
