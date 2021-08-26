package com.ngdesk.repositories.escalation;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.ngdesk.graphql.escalation.dao.Escalation;

@Repository
public class CustomEscalationRepositoryImpl implements CustomEscalationRepository {
	@Autowired
	private MongoOperations mongoOperations;

	@Override
	public List<Escalation> findAllEscalations(Pageable pageable, String collectionName) {
		Query query = new Query();
		query.with(pageable);
		List<Escalation> e = mongoOperations.find(query, Escalation.class, collectionName);
		return mongoOperations.find(query, Escalation.class, collectionName);

	}

	@Override
	public Integer getEscalationCount(String collectionName) {
		// TODO Auto-generated method stub
		return (int) mongoOperations.count(new Query(), Escalation.class, collectionName);
	}

}
