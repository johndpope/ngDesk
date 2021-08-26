package com.ngdesk.graphql.dashboards.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.dashboards.DashboardRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class DashboardCountFetcher implements DataFetcher<Integer> {

	@Autowired
	AuthManager authManager;

	@Autowired
	DashboardRepository dashboardRepository;

	@Override
	public Integer get(DataFetchingEnvironment environment) {
		String companyId = authManager.getUserDetails().getCompanyId();
		return dashboardRepository.dashboardCount(companyId, "dashboards");
	}

}
