package com.ngdesk.graphql.campaigns.dao;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.graphql.role.layout.dao.RoleService;
import com.ngdesk.repositories.campaigns.CampaignsRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class CampaignDataFetcher implements DataFetcher<Campaigns> {

	@Autowired
	AuthManager authManager;

	@Autowired
	CampaignsRepository campaignsRepository;

	@Autowired
	RoleService roleService;

	@Override
	public Campaigns get(DataFetchingEnvironment environment) {

		String campaignsId = environment.getArgument("id");

		Optional<Campaigns> optionalCampaign = campaignsRepository.findCampaignById(campaignsId,
				"campaigns_" + authManager.getUserDetails().getCompanyId());

		if (optionalCampaign.isPresent() && roleService.isSystemAdmin(authManager.getUserDetails().getRole())) {

			return optionalCampaign.get();
		}
		return null;

	}

}
