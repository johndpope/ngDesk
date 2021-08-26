package com.ngdesk.repositories;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.ngdesk.escalation.dao.Escalation;

@Repository
public class CustomEscalationRepositoryImpl implements CustomEscalationRepository {

	@Autowired
	private MongoOperations mongoOperations;

	@Override
	public Optional<Escalation> findEscalationByName(String name, String collectionName) {
		return Optional.ofNullable(mongoOperations.findOne(new Query(Criteria.where("NAME").is(name)), Escalation.class, collectionName));
	}

	@Override
	public Optional<Escalation> findOtherEscalationsWithDuplicateName(String name, String escalationId, String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("NAME").is(name), Criteria.where("_id").ne(escalationId));
		return Optional.ofNullable(mongoOperations.findOne(new Query(criteria), Escalation.class, collectionName));
	}

}