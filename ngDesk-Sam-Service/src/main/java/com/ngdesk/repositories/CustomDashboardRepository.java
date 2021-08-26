package com.ngdesk.repositories;

import java.util.Optional;

import com.ngdesk.commons.models.Dashboard;

public interface CustomDashboardRepository {
	public Optional<Dashboard> findDashboardByName(String name, String collectionName);

	public Optional<Dashboard> findOtherDashboardsWithDuplicateName(String name, String dashboardId,
			String collectionName);

}
