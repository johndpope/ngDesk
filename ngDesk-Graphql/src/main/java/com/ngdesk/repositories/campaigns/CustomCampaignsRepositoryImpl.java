package com.ngdesk.repositories.campaigns;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.ngdesk.graphql.campaigns.dao.Campaigns;

@Repository
public class CustomCampaignsRepositoryImpl implements CustomCampaignsRepository {

	@Autowired
	private MongoOperations mongoOperations;

	@Override
	public Optional<List<Campaigns>> findAllCampaignsLists(Pageable pageable, String collectionName) {
		Query query = new Query();
		query.with(pageable);
		return Optional.ofNullable(mongoOperations.find(query, Campaigns.class, collectionName));

	}

	@Override
	public Optional<Campaigns> findCampaignById(String id, String collectionName) {
		Query query = new Query(Criteria.where("_id").is(id));
		return Optional.ofNullable(mongoOperations.findOne(query, Campaigns.class, collectionName));

	}

	@Override
	public Integer findCampaignsCount(String collectionName) {

		return (int) mongoOperations.count(new Query(), collectionName);
	}

}
