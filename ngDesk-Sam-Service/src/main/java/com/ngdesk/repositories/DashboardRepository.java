package com.ngdesk.repositories;

import org.springframework.stereotype.Repository;

import com.ngdesk.commons.models.Dashboard;

@Repository
public interface DashboardRepository extends CustomDashboardRepository, CustomNgdeskRepository<Dashboard, String> {

}
