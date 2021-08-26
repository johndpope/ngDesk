package com.ngdesk.repositories;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.util.Assert;

import com.ngdesk.module.slas.dao.DiscussionMessage;

public class CustomModuleEntryRepositoryImpl implements CustomModuleEntryRepository {

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
	public Optional<Map<String, Object>> findEntryByName(String fieldName, String fieldValue, String collectionName) {

		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("EFFETIVE_TO").is(null),
				Criteria.where(fieldName).is(fieldValue));

		Query query = new Query(criteria);

		return Optional.ofNullable(mongoOperations.findOne(query, Map.class, collectionName));
	}

	@Override
	public Optional<List<Map>> findAllEntriesByCollectionName(String collectionName) {
		Criteria criteria = new Criteria();
		Query query = new Query(criteria).with(Sort.by(Sort.Direction.ASC, "DATE_CREATED"));
		return Optional.ofNullable(mongoOperations.find(query, Map.class, collectionName));
	}

	@Override
	public Optional<Map<String, Object>> updateEntry(Map<String, Object> entry, String collectionName) {
		Assert.notNull(entry, "Entry must not be null");
		Assert.notNull(entry.get("_id"), "Data ID must not be null");
		Query query = new Query();
		query.addCriteria(Criteria.where("_id").is(entry.get("_id").toString()));

		return Optional.ofNullable(mongoOperations.findAndReplace(query, entry, collectionName));
	}

	@Override
	public Optional<Map<String, Object>> findTeamByName(String teamName, String collectionName) {
		Query query = new Query(Criteria.where("NAME").is(teamName));
		return Optional.ofNullable(mongoOperations.findOne(query, Map.class, collectionName));
	}

	@Override
	public void updateMetadataEvents(String dataId, DiscussionMessage eventsDiscussionMessage, String collectionName) {

		Update update = new Update();
		update.addToSet("META_DATA.EVENTS", eventsDiscussionMessage);
		Criteria criteria = new Criteria();
		criteria.where("_id").is(dataId);
		mongoOperations.updateFirst(new Query(Criteria.where("_id").is(dataId)), update, collectionName);

	}

	@Override
	public void addDiscussionMessage(String entryId, String fieldName, DiscussionMessage discussionMessage,
			String collectionName) {
		Update update = new Update();
		update.addToSet(fieldName, discussionMessage);
		Criteria criteria = new Criteria();
		criteria.where("_id").is(entryId);
		mongoOperations.updateFirst(new Query(Criteria.where("_id").is(entryId)), update, collectionName);

	}

	@Override
	public void findEntryAndUpdateUnset(String collectionName, String dataId, String fieldName) {
		Query query = new Query();
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("_id").is(dataId), Criteria.where("DELETED").is(false));
		query.addCriteria(criteria);
		mongoOperations.updateFirst(query, new Update().unset(fieldName), collectionName);

	}

	@Override
	public void findEntryAndUpdate(String collectionName, String dataId, String fieldName, Date currentTimestamp) {
		Query query = new Query();
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("_id").is(dataId), Criteria.where("DELETED").is(false));
		query.addCriteria(criteria);
		mongoOperations.updateFirst(query, new Update().set(fieldName, currentTimestamp), collectionName);

	}

}
