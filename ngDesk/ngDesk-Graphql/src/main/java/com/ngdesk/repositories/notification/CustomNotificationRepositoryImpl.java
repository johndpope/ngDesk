package com.ngdesk.repositories.notification;

import java.util.List;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.graphql.notification.dao.Notification;

@Repository
public class CustomNotificationRepositoryImpl implements CustomNotificationRepository {
	
	@Autowired
	AuthManager authManager;

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<Notification> findNotificationByCompanyIdAndDataId(String companyId, String recipientId,
			String notificationId, String collectionName) {

		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("companyId").is(companyId), Criteria.where("recipientId").is(recipientId),
				Criteria.where("_id").is(notificationId));
		Query query = new Query(criteria);
		return Optional.ofNullable(mongoOperations.findOne(query, Notification.class, collectionName));
	}

	@Override
	public Optional<List<Notification>> findAllNotificationsInCompany(Pageable pageable, String companyId,String userId,
			String collectionName) {
		userId=authManager.getUserDetails().getUserId();
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("companyId").is(companyId), Criteria.where("recipientId").is(userId));
		Query query = new Query(criteria);
		query.with(pageable);
		return Optional.ofNullable(mongoOperations.find(query, Notification.class, collectionName));

	}

	@Override
	public Optional<List<Notification>> findUnreadNotifications(Pageable pageable, String companyId, String recipientId,
			Boolean read, String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("companyId").is(companyId), Criteria.where("recipientId").is(recipientId),
				Criteria.where("read").is(read));
		Query query = new Query(criteria);
		query.with(pageable);
		return Optional.ofNullable(mongoOperations.find(query, Notification.class, collectionName));
	}

	@Override
	public int findUnreadNotificationsCount(String companyId, String recipientId, String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("companyId").is(companyId), Criteria.where("recipientId").is(recipientId),
				Criteria.where("read").is(false));
		Query query = new Query(criteria);
		return (int) mongoOperations.count(query, collectionName);
	}

}
