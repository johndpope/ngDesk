package com.ngdesk.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import com.ngdesk.workflow.microsoft.teams.dao.MicrosoftTeams;

@Repository
public class CustomMicrosoftTeamsRepositoryImpl implements CustomMicrosoftTeamsRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<MicrosoftTeams> findMsTeamEntryByVariable(String variable, String value, String collectionName) {
		Assert.notNull(variable, "The given variable must not be null!");
		Assert.notNull(collectionName, "The given collectionName must not be null!");
		Query query = new Query(Criteria.where(variable).is(value));
		return Optional.ofNullable(mongoOperations.findOne(query, MicrosoftTeams.class, collectionName));
	}

	@Override
	public List<MicrosoftTeams> findMsTeamEntriesByVariable(String variable, String value, String collectionName) {
		Assert.notNull(variable, "The given variable must not be null!");
		Assert.notNull(collectionName, "The given collectionName must not be null!");
		Query query = new Query(Criteria.where(variable).is(value));
		return mongoOperations.find(query, MicrosoftTeams.class, collectionName);
	}
}
