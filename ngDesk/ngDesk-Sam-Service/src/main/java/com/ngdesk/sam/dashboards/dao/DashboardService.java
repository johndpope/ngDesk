package com.ngdesk.sam.dashboards.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.commons.models.AdvancedPieChartWidget;
import com.ngdesk.commons.models.BarChartWidget;
import com.ngdesk.commons.models.Dashboard;
import com.ngdesk.commons.models.DashboardCondition;
import com.ngdesk.commons.models.PieChartWidget;
import com.ngdesk.commons.models.ScoreCardWidget;
import com.ngdesk.commons.models.Widget;
import com.ngdesk.repositories.DashboardRepository;
import com.ngdesk.repositories.ModuleRepository;
import com.ngdesk.repositories.RolesRepository;
import com.ngdesk.sam.modules.dao.Module;
import com.ngdesk.sam.modules.dao.ModuleField;
import com.ngdesk.sam.roles.dao.Role;

@Component
public class DashboardService {

	@Autowired
	ModuleRepository moduleRepository;

	@Autowired
	AuthManager authManager;

	@Autowired
	RolesRepository rolesRepository;

	@Autowired
	DashboardRepository dashboardRepository;

	public void validateRole(String role, String collectionName) {
		Optional<Role> optional = rolesRepository.findById(role, collectionName);
		if (optional.isEmpty()) {
			throw new BadRequestException("ROLE_ID_INVALID", null);
		}
	}

	public void duplicateDashboardCheck(String dashboardName) {
		Optional<Dashboard> optionalDuplicateDashboard = dashboardRepository.findDashboardByName(dashboardName,
				"dashboards");
		if (optionalDuplicateDashboard.isPresent()) {
			throw new BadRequestException("DASHBOARD_NAME_ALREADY_EXISTS", null);
		}
	}

	public void duplicateDashboardNameAndIdCheck(String dashboardName, String dashboardId) {
		Optional<Dashboard> optional = dashboardRepository.findOtherDashboardsWithDuplicateName(dashboardName,
				dashboardId, "dashboards");
		if (optional.isPresent()) {
			throw new BadRequestException("DASHBOARD_NAME_ALREADY_EXISTS", null);
		}
	}

	public Module validateModule(Widget widget) {
		Optional<List<Module>> optionalModules = moduleRepository
				.findAllModules("modules_" + authManager.getUserDetails().getCompanyId());
		Optional<Module> optionalModule = optionalModules.get().stream()
				.filter(module -> module.getModuleId().equals(widget.getModuleId())).findFirst();
		if (optionalModule.isEmpty()) {
			String[] vars = { widget.getType() };
			throw new BadRequestException("MODULE_ID_INVALID", vars);
		}
		validateCondition(widget, optionalModule.get());
		return optionalModule.get();
	}

	public void validate(List<Widget> widgets) {
		for (Widget widget : widgets) {
			if (widget.getType().equals("multi-score")) {
				List<ScoreCardWidget> scorecards = widget.getMultiScorecards();
				for (ScoreCardWidget scorecard : scorecards) {
					validateModule(scorecard);
					validateAggregateField(scorecard, validateModule(scorecard));
				}
			} else {
				validateModule(widget);
				if (widget.getType().equals("pie") || widget.getType().equals("bar-horizontal")
						|| widget.getType().equals("advanced-pie")) {
					validateAggregateField(widget, validateModule(widget));
					validateField(widget, validateModule(widget));
				}
			}
		}
	}

	public void validateCondition(Widget widget, Module module) {
		for (DashboardCondition condition : widget.getDashboardconditions()) {
			Optional<ModuleField> optional = module.getFields().stream()
					.filter(moduleField -> moduleField.getFieldId().equals(condition.getCondition())).findFirst();
			if (optional.isEmpty()) {
				String[] vars = { widget.getType() };
				throw new BadRequestException("DASHBOARD_CONDITION_INVALID", vars);
			}
			ModuleField conditionField = module.getFields().stream()
					.filter(field -> field.getFieldId().equals(condition.getCondition())).findFirst().get();
			String displayDatatype = conditionField.getDataType().getDisplay();
			if (displayDatatype.equalsIgnoreCase("Time Window")) {
				if (!condition.getOperator().equalsIgnoreCase("EQUALS_TO")) {
					throw new BadRequestException("DASHBOARD_CONDITION_OPERATOR", null);
				}
				String value = condition.getValue();
				String number = value.replaceAll("[^\\d]", "");

				if (!value.equalsIgnoreCase("days(current_date-" + number + ")")
						&& !value.equalsIgnoreCase("months(current_date-" + number + ")")) {
					throw new BadRequestException("DASHBOARD_CONDITION_VALUE", null);
				}
			}

		}
	}

	public void validateField(Widget widget, Module module) {
		if (widget.getType().equals("pie")) {
			PieChartWidget pieChartWidget = (PieChartWidget) widget;
			Optional<ModuleField> optional = module.getFields().stream()
					.filter(moduleField -> moduleField.getFieldId().equals(pieChartWidget.getField())).findFirst();
			if (optional.isEmpty()) {
				throw new BadRequestException("PIECHART_FIELD_INVALID", null);
			}
		} else if (widget.getType().equals("bar-horizontal")) {
			BarChartWidget barChartWidget = (BarChartWidget) widget;
			Optional<ModuleField> optional = module.getFields().stream()
					.filter(moduleField -> moduleField.getFieldId().equals(barChartWidget.getField())).findFirst();
			if (optional.isEmpty()) {
				throw new BadRequestException("BARCHART_FIELD_INVALID", null);
			}
		} else if (widget.getType().equals("advanced-pie")) {
			AdvancedPieChartWidget advancePieChart = (AdvancedPieChartWidget) widget;
			Optional<ModuleField> optional = module.getFields().stream()
					.filter(moduleField -> moduleField.getFieldId().equals(advancePieChart.getField())).findFirst();
			if (optional.isEmpty()) {
				throw new BadRequestException("ADVANCE_PIECHART_FIELD_INVALID", null);
			}
		}

	}

	public void validateAggregateField(Widget widget, Module module) {
		if (widget.getType().equals("pie")) {
			PieChartWidget pieChartWidget = (PieChartWidget) widget;

			if (!pieChartWidget.getAggregateType().equalsIgnoreCase("count")) {
				Optional<ModuleField> optional = module.getFields().stream()
						.filter(moduleField -> moduleField.getFieldId().equals(pieChartWidget.getAggregateField()))
						.findFirst();

				if (optional.isEmpty()) {
					String[] vars = { widget.getType() };
					throw new BadRequestException("AGGREGATE_FIELD_INVALID", vars);
				}

				if (!optional.get().getDataType().getBackend().equalsIgnoreCase("Integer")
						&& !optional.get().getDataType().getBackend().equalsIgnoreCase("Float")
						&& !optional.get().getDataType().getBackend().equalsIgnoreCase("Double")) {
					String[] vars = { widget.getType() };
					throw new BadRequestException("AGGREGATE_FIELD_DATATYPE", vars);

				}
			}

		} else if (widget.getType().equals("bar-horizontal")) {
			BarChartWidget barChartWidget = (BarChartWidget) widget;

			if (!barChartWidget.getAggregateType().equalsIgnoreCase("count")) {
				Optional<ModuleField> optional = module.getFields().stream()
						.filter(moduleField -> moduleField.getFieldId().equals(barChartWidget.getAggregateField()))
						.findFirst();
				if (optional.isEmpty()) {
					String[] vars = { widget.getType() };
					throw new BadRequestException("AGGREGATE_FIELD_INVALID", vars);
				}
				if (!optional.get().getDataType().getBackend().equalsIgnoreCase("Integer")
						&& !optional.get().getDataType().getBackend().equalsIgnoreCase("Float")
						&& !optional.get().getDataType().getBackend().equalsIgnoreCase("Double")) {
					String[] vars = { widget.getType() };
					throw new BadRequestException("AGGREGATE_FIELD_DATATYPE", vars);

				}
			}
		} else if (widget.getType().equals("score")) {
			ScoreCardWidget scoreCardWidget = (ScoreCardWidget) widget;

			if (!scoreCardWidget.getAggregateType().equalsIgnoreCase("count")) {
				Optional<ModuleField> optional = module.getFields().stream()
						.filter(moduleField -> moduleField.getFieldId().equals(scoreCardWidget.getAggregateField()))
						.findFirst();

				if (optional.isEmpty()) {
					String[] vars = { widget.getType() };
					throw new BadRequestException("AGGREGATE_FIELD_INVALID", vars);
				}

				if (!optional.get().getDataType().getBackend().equalsIgnoreCase("Integer")
						&& !optional.get().getDataType().getBackend().equalsIgnoreCase("Float")
						&& !optional.get().getDataType().getBackend().equalsIgnoreCase("Double")) {
					String[] vars = { widget.getType() };
					throw new BadRequestException("AGGREGATE_FIELD_DATATYPE", vars);

				}
			}
		} else {
			AdvancedPieChartWidget advancePieChart = (AdvancedPieChartWidget) widget;

			if (!advancePieChart.getAggregateType().equalsIgnoreCase("count")) {
				Optional<ModuleField> optional = module.getFields().stream()
						.filter(moduleField -> moduleField.getFieldId().equals(advancePieChart.getAggregateField()))
						.findFirst();
				if (optional.isEmpty()) {
					String[] vars = { widget.getType() };
					throw new BadRequestException("AGGREGATE_FIELD_INVALID", vars);
				}
				if (!optional.get().getDataType().getBackend().equalsIgnoreCase("Integer")
						&& !optional.get().getDataType().getBackend().equalsIgnoreCase("Float")
						&& !optional.get().getDataType().getBackend().equalsIgnoreCase("Double")) {
					String[] vars = { widget.getType() };
					throw new BadRequestException("AGGREGATE_FIELD_DATATYPE", vars);

				}
			}
		}
	}
}