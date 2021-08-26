package com.ngdesk.sam.dashboards.dao;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ngdesk.commons.exceptions.ForbiddenException;
import com.ngdesk.commons.exceptions.NotFoundException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.commons.models.Dashboard;
import com.ngdesk.commons.models.MultiScoreCardWidget;
import com.ngdesk.commons.models.Widget;
import com.ngdesk.repositories.DashboardRepository;
import com.ngdesk.repositories.RolesRepository;

@RestController
public class DashboardAPI {

	@Autowired
	DashboardRepository dashboardRepository;

	@Autowired
	private AuthManager authManager;

	@Autowired
	private DashboardService dashboardService;

	@Autowired
	private RolesRepository rolesRepository;

	@PostMapping("/dashboard")
	public Dashboard postDashboard(@Valid @RequestBody Dashboard dashboard) {

		String systemAdminId = rolesRepository
				.findRoleName("SystemAdmin", "roles_" + authManager.getUserDetails().getCompanyId()).get().getId();
		String userId = authManager.getUserDetails().getRole();
		if (!systemAdminId.equals(userId)) {
			throw new ForbiddenException("FORBIDDEN");
		}
		dashboardService.duplicateDashboardCheck(dashboard.getName());
		dashboardService.validateRole(dashboard.getRole(), "roles_" + authManager.getUserDetails().getCompanyId());
		List<Widget> widgets = dashboard.getWidgets();
		for (Widget widget : widgets) {
			if (widget.getType().equals("multi-score")) {
				MultiScoreCardWidget scoreCardWidgets = (MultiScoreCardWidget) widget;
				for (Widget widgetScorecards : scoreCardWidgets.getMultiScorecards()) {
					widgetScorecards.setWidgetId(UUID.randomUUID().toString());
				}
			}
			widget.setWidgetId(UUID.randomUUID().toString());
		}
		dashboardService.validate(widgets);
		dashboard.setCompanyId(authManager.getUserDetails().getCompanyId());
		dashboard.setDateCreated(new Date());
		dashboard.setDateUpdated(new Date());
		dashboard.setLastUpdatedBy(authManager.getUserDetails().getUserId());
		dashboard.setCreatedBy(authManager.getUserDetails().getUserId());
		dashboard = dashboardRepository.save(dashboard, "dashboards");
		return dashboard;
	}

	@PutMapping("/dashboard")
	public Dashboard putDashboard(@Valid @RequestBody Dashboard dashboard) {

		String systemAdminId = rolesRepository
				.findRoleName("SystemAdmin", "roles_" + authManager.getUserDetails().getCompanyId()).get().getId();
		String userId = authManager.getUserDetails().getRole();
		if (!systemAdminId.equals(userId)) {
			throw new ForbiddenException("FORBIDDEN");
		}
		Optional<Dashboard> optional = dashboardRepository.findById(dashboard.getDashboardId(), "dashboards");
		if (optional.isEmpty()) {
			throw new NotFoundException("DASHBOARD_NOT_FOUND", null);
		}
		Dashboard existingDashboard = optional.get();
		dashboardService.duplicateDashboardNameAndIdCheck(dashboard.getName(), dashboard.getDashboardId());
		dashboardService.validateRole(dashboard.getRole(), "roles_" + authManager.getUserDetails().getCompanyId());
		List<Widget> widgets = dashboard.getWidgets();
		dashboardService.validate(widgets);
		dashboard.setDateCreated(existingDashboard.getDateCreated());
		dashboard.setCreatedBy(existingDashboard.getCreatedBy());
		dashboard.setCompanyId(existingDashboard.getCompanyId());
		dashboard.setDateUpdated(new Date());
		dashboard.setLastUpdatedBy(authManager.getUserDetails().getUserId());
		dashboard = dashboardRepository.save(dashboard, "dashboards");
		return dashboard;

	}

	@DeleteMapping("/dashboard")
	public void deleteDashboard(@RequestParam("dashboard_id") String dashboardId, Dashboard dashboard) {
		String systemAdminId = rolesRepository
				.findRoleName("SystemAdmin", "roles_" + authManager.getUserDetails().getCompanyId()).get().getId();
		String userId = authManager.getUserDetails().getRole();
		if (!systemAdminId.equals(userId)) {
			throw new ForbiddenException("FORBIDDEN");
		}
		Optional<Dashboard> optional = dashboardRepository.findById(dashboardId, "dashboards");
		if (optional.isEmpty()) {
			throw new NotFoundException("DASHBOARD_NOT_FOUND", null);
		}
		dashboardRepository.deleteById(dashboardId, "dashboards");
	}

}
