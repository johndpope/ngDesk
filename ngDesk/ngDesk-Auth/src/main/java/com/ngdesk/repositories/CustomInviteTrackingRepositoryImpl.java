package com.ngdesk.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import com.ngdesk.auth.forgot.password.InviteTracking;

@Repository
public class CustomInviteTrackingRepositoryImpl implements CustomInviteTrackingRepository {
	@Autowired
	MongoOperations mongoOperations;

	@Override
	public InviteTracking findInviteTrackingByUuidAndTempUuid(String collectionName, String userUuid, String tempUuid) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("USER_UUID").is(userUuid), Criteria.where("TEMP_UUID").is(tempUuid));
		Query query = new Query(criteria);
		return mongoOperations.findOne(query, InviteTracking.class, collectionName);
	}

	@Override
	public void removeInviteTrackingByUuid(String userUuid, String collectionName) {
		Query query = new Query();
		query.addCriteria(Criteria.where("USER_UUID").is(userUuid));
		mongoOperations.remove(query, collectionName);
	}

}
