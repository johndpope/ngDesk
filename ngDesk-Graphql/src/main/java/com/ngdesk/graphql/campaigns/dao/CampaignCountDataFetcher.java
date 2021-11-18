package com.ngdesk.graphql.campaigns.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.campaigns.CampaignsRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class CampaignCountDataFetcher implements DataFetcher<Integer> {

	@Autowired
	AuthManager authManager;

	@Autowired
	CampaignsRepository campaignsRepository;

	@Override
	public Integer get(DataFetchingEnvironment environment) throws Exception {
		return campaignsRepository.findCampaignsCount("campaigns_" + authManager.getUserDetails().getCompanyId());

	}

}