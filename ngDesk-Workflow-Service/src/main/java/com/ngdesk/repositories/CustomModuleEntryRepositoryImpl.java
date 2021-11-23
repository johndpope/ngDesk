package com.ngdesk.repositories;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.http.util.Asserts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import com.ngdesk.data.dao.DiscussionMessage;

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
		return Optional.ofNullable(mongoOperations.findOne(query, Map.class, collectionName));
	}

	@Override
	public List<Map<String, Object>> findEntriesByVariable(String variable, String value, String collectionName) {
		Assert.notNull(variable, "The given variable must not be null!");
		Assert.notNull(collectionName, "The given collectionName must not be null!");
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where(variable).is(value));
		Query query = new Query(criteria);

		return mongoOperations.find(query, (Class<Map<String, Object>>) (Class) Map.class, collectionName);

	}

	@Override
	public Map<String, Object> findUserIdByUuid(String variable, String value, String collectionName) {
		Assert.notNull(variable, "The given variable must not be null!");
		Assert.notNull(collectionName, "The given collectionName must not be null!");
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where(variable).is(value),
				Criteria.where("EFFECTIVE_TO").is(null));
		Query query = new Query(criteria);

		return mongoOperations.findOne(query, Map.class, collectionName);

	}

	@Override
	public List<Map<String, Object>> findEntriesByTeamIds(List<String> teamIds, String collectionName) {
		for (String id : teamIds) {
			Assert.notNull(id, "The given id must not be null!");
		}
		Assert.notNull(collectionName, "The given collectionName must not be null!");

		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("TEAMS").in(teamIds));
		Query query = new Query(criteria);
		return mongoOperations.find(query, (Class<Map<String, Object>>) (Class) Map.class, collectionName);
	}

	@Override
	public Optional<Map<String, Object>> findEntryByVariable(String variable, String value, String collectionName) {
		Assert.notNull(variable, "The given variable must not be null!");
		Assert.notNull(collectionName, "The given collectionName must not be null!");
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where(variable).is(value));
		Query query = new Query(criteria);

		return Optional.ofNullable(
				mongoOperations.findOne(query, (Class<Map<String, Object>>) (Class) Map.class, collectionName));
	}

	@Override
	public List<Map<String, Object>> findContactsByUserIds(List<String> entryIds, String collectionName) {
		Assert.notNull(entryIds, "The entry ids must not be null!");
		Assert.notNull(collectionName, "The given collectionName must not be null!");
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("USER").in(entryIds));
		Query query = new Query(criteria);
		return mongoOperations.find(query, (Class<Map<String, Object>>) (Class) Map.class, collectionName);
	}

	@Override
	public List<Map<String, Object>> findEntriesByIds(List<String> entryIds, String collectionName) {
		Assert.notNull(entryIds, "The entry ids must not be null!");
		Assert.notNull(collectionName, "The given collectionName must not be null!");
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("_id").in(entryIds));
		Query query = new Query(criteria);
		return mongoOperations.find(query, (Class<Map<String, Object>>) (Class) Map.class, collectionName);
	}

	@Override
	public void updateEntry(String dataId, Map<String, Object> metaData, String collectionName) {
		Update update = new Update();
		update.set("META_DATA", metaData);
		Criteria criteria = new Criteria();
		criteria.where("_id").is(dataId);
		mongoOperations.updateFirst(new Query(Criteria.where("_id").is(dataId)), update, collectionName);
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
	public Optional<Map<String, Object>> findChatBysessionUUID(String sessionUUID, String collectionName) {
		Criteria criteria = new Criteria();
		criteria.where("SESSION_UUID").is(sessionUUID);
		return Optional.ofNullable(mongoOperations.findOne(new Query(criteria),
				(Class<Map<String, Object>>) (Class) Map.class, collectionName));
	}

	@Override
	public List<Map<String, Object>> findEntriesByCollectionName(String collectionName) {
		Assert.notNull(collectionName, "The given collectionName must not be null!");
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("EFFECTIVE_TO").is(null));
		Query query = new Query(criteria);
		return mongoOperations.find(query, (Class<Map<String, Object>>) (Class) Map.class, collectionName);
	}

	@Override
	public Optional<Map<String, Object>> findEntriesBySessionUuid(String sessionUuid, String collectionName) {
		Assert.notNull(collectionName, "The given collectionName must not be null!");
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("EFFECTIVE_TO").is(null),
				Criteria.where("SESSION_UUID").is(sessionUuid));
		Query query = new Query(criteria);
		return Optional.ofNullable(
				mongoOperations.findOne(query, (Class<Map<String, Object>>) (Class) Map.class, collectionName));

	}

	@Override
	public List<String> findDistinctEntries(String fieldName, String collectionName) {
		Asserts.notNull(collectionName, "collectionName must not be null");
		Asserts.notNull(fieldName, "fieldName must not be null");
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("EFFECTIVE_TO").is(null));
		Query query = new Query(criteria);
		if (collectionName.contains("Users_")) {
			query.fields().exclude("PASSWORD");
		}
		query.fields().exclude("META_DATA");

		return mongoOperations.findDistinct(query, fieldName, collectionName, String.class);
	}

	@Override
	public Optional<Map<String, Object>> findAggregationFieldValue(String fieldName, String value,
			String aggregationField, String aggregationType, Criteria criterias, String collectionName) {

		Aggregation aggregation = null;
		if (aggregationType.equals("sum")) {
			aggregation = newAggregation(match(criterias), group().sum(aggregationField).as(aggregationField));

		} else if (aggregationType.equals("min")) {
			aggregation = newAggregation(match(criterias), group().min(aggregationField).as(aggregationField));

		} else if (aggregationType.equals("max")) {
			aggregation = newAggregation(match(criterias), group().max(aggregationField).as(aggregationField));

		}
		AggregationResults<Map<String, Object>> groupResults = mongoOperations.aggregate(aggregation, collectionName,
				(Class<Map<String, Object>>) (Class) Map.class);

		return Optional.ofNullable(groupResults.getUniqueMappedResult());
	}

}