package com.ngdesk.repositories;

import java.util.Optional;

import org.apache.http.util.Asserts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.ngdesk.integration.microsoft.teams.dao.MicrosoftTeams;

public class CustomMicrosoftTeamsRepositoryImpl implements CustomMicrosoftTeamsRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<MicrosoftTeams> findByChannelId(String channelId, String collectionName) {
		Asserts.notNull(channelId, "The given channelId must not be null!");
		Asserts.notNull(collectionName, "The given collectionName must not be null!");
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("CHANNEL_ID").is(channelId));
		Query query = new Query(criteria);

		return Optional.ofNullable(mongoOperations.findOne(query, MicrosoftTeams.class, collectionName));
	}
}
