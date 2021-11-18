package com.ngdesk.repositories.campaigns;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.ngdesk.graphql.campaigns.dao.Campaigns;

public interface CustomCampaignsRepository {
	public Optional<List<Campaigns>> findAllCampaignsLists(Pageable pageable, String collectionName);

	public Optional<Campaigns> findCampaignById(String id, String collectionName);

	public Integer findCampaignsCount(String collectionName);

}
