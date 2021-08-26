package com.ngdesk.repositories;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.ngdesk.websocket.modules.dao.Module;

public class CustomNotificationRepositoryImpl implements CustomNotificationRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<Module> findByModuleId(String moduleId, String collectionName) {
		Criteria criteria = new Criteria();
		Query query = new Query(criteria.where("_id").is(moduleId));
		return Optional.ofNullable(mongoOperations.findOne(query, Module.class, collectionName));
	}

	@Override
	public Optional<Map<String, Object>> findByEntryId(String id, String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("_id").is(id), Criteria.where("DELETED").is(false),
				Criteria.where("EFFECTIVE_TO").is(null));
		Query query = new Query(criteria);
		return Optional.ofNullable(mongoOperations.findOne(query, Map.class, collectionName));
	}
}
