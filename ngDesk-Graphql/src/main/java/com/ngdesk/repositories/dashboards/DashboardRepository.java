package com.ngdesk.repositories.dashboards;

import org.springframework.stereotype.Repository;

import com.ngdesk.commons.models.Dashboard;
import com.ngdesk.repositories.CustomNgdeskRepository;

@Repository
public interface DashboardRepository extends CustomDashboardRepository, CustomNgdeskRepository<Dashboard, String> {

}
