package com.ngdesk.repositories;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.ngdesk.workflow.notify.dao.UserToken;

public class CustomUserTokenRepositoryImpl implements CustomUserTokenRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public UserToken findByUserUuid(String Useruuid, String collectionName) {
		Criteria criteria = new Criteria();
		Query query = new Query(criteria.where("USER_UUID").is(Useruuid));
		return Optional.ofNullable(mongoOperations.findOne(query, UserToken.class, collectionName)).get();
	}

}
