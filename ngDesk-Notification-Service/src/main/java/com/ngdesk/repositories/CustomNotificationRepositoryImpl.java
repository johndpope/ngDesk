package com.ngdesk.repositories;

import java.util.Date;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import com.ngdesk.notifications.dao.Module;

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
	public Optional<Map<String, Object>> findByDataId(String dataId, String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("_id").is(dataId), Criteria.where("DELETED").is(false),
				Criteria.where("EFFECTIVE_TO").is(null));
		Query query = new Query(criteria);
		return Optional.ofNullable(mongoOperations.findOne(query, Map.class, collectionName));
	}

	@Override
	public void markAllNotificationsAsRead(String companyId, String userId, String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("companyId").is(companyId), Criteria.where("recipientId").is(userId));
		Query query = new Query(criteria);
		Update update = new Update();
		update.set("read", true);
		update.set("dateUpdated", new Date());
		mongoOperations.updateMulti(query, update, collectionName);
	}

	@Override
	public void markNotificationAsRead(String notificationId, String collectionName) {
		Query query = new Query(Criteria.where("_id").is(notificationId));
		Update update = new Update();
		update.set("read", true);
		update.set("dateUpdated", new Date());
		mongoOperations.updateFirst(query, update, collectionName);
	}

}
