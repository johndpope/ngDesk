package com.ngdesk.repositories;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.ngdesk.sam.controllers.user.dao.User;

public class CustomUserRepositoryImpl implements CustomUserRepository {

	@Autowired
	private MongoOperations mongoOperations;

	@Override
	public Optional<User> findByUserEmail(String email, String collection) {
		return Optional.ofNullable(
				mongoOperations.findOne(new Query(Criteria.where("EMAIL_ADDRESS").is(email)), User.class, collection));
	}

}
