package com.ngdesk.graphql.dashboards.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.commons.managers.SessionManager;
import com.ngdesk.commons.models.Dashboard;
import com.ngdesk.graphql.modules.dao.Module;
import com.ngdesk.repositories.dashboards.DashboardRepository;
import com.ngdesk.repositories.modules.ModulesRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class DashboardDataFetcher implements DataFetcher<Dashboard> {

	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	AuthManager authManager;

	@Autowired
	SessionManager sessionManager;

	@Autowired
	DashboardRepository dashboardRepository;

	@Override
	public Dashboard get(DataFetchingEnvironment environment) {

		String companyId = authManager.getUserDetails().getCompanyId();
		String dashboardId = environment.getArgument("dashboardId");
		Optional<Dashboard> optionalDashboard = dashboardRepository.findByCompanyIdAndId(companyId, dashboardId,
				"dashboards");
		List<Module> modules = modulesRepository.findAllModules("modules_" + companyId);
		sessionManager.getSessionInfo().put("modulesMap", modules);
		if (optionalDashboard.isPresent()) {
			return optionalDashboard.get();
		}

		return null;
	}

}
