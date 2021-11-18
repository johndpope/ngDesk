package com.ngdesk.repositories.campaigns;

import org.springframework.stereotype.Repository;

import com.ngdesk.graphql.campaigns.dao.Campaigns;
import com.ngdesk.repositories.CustomNgdeskRepository;

@Repository
public interface CampaignsRepository extends CustomCampaignsRepository, CustomNgdeskRepository<Campaigns, String> {

}