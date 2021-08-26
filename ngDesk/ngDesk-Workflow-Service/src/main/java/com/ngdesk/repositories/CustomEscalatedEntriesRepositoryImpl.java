package com.ngdesk.repositories;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.ngdesk.workflow.escalation.dao.EscalatedEntries;

@Repository
public class CustomEscalatedEntriesRepositoryImpl implements CustomEscalatedEntriesRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<EscalatedEntries> findEscalatedEntries(String entryId, String escalationId, String collectionName) {

		Criteria critera = new Criteria();
		critera.andOperator(Criteria.where("ESCALATION_ID").is(escalationId), Criteria.where("ENTRY_ID").is(entryId));

		Query query = new Query(critera);
		return Optional.ofNullable(mongoOperations.findOne(query, EscalatedEntries.class, collectionName));
	}

	@Override
	public Optional<EscalatedEntries> deleteEscalatedEntries(String entryId,
			String collectionName) {
		Criteria critera = new Criteria();
		critera.andOperator(Criteria.where("ENTRY_ID").is(entryId));

		Query query = new Query(critera);
		return Optional.ofNullable(mongoOperations.findAndRemove(query, EscalatedEntries.class, collectionName ));
	}

}
