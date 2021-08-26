package com.ngdesk.repositories.dashboards;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.ngdesk.commons.models.Dashboard;

public interface CustomDashboardRepository {

	public Optional<Dashboard> findByCompanyIdAndId(String companyId, String id, String collectionName);

	public Optional<List<Dashboard>> findAllDashboardsInCompany(Pageable pageable, String companyId,
			String collectionName);

	public int dashboardCount(String companyId, String collectionName);

}
