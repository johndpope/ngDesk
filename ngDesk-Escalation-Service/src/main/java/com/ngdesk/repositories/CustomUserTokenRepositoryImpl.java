package com.ngdesk.repositories;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.ngdesk.escalation.notify.UserToken;

@Repository
public class CustomUserTokenRepositoryImpl implements CustomUserTokenRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<UserToken> getUserTokenByUserUUID(String userUuid, String companyId) {
		return Optional.ofNullable(mongoOperations.findOne(new Query(Criteria.where("USER_UUID").is(userUuid)),
				UserToken.class, "user_tokens_" + companyId));
	}

}
