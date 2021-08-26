package com.ngdesk.graphql.dashboards.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.commons.models.Dashboard;
import com.ngdesk.commons.models.OrderBy;
import com.ngdesk.commons.models.ScoreCardWidget;
import com.ngdesk.commons.models.Widget;
import com.ngdesk.graphql.CustomGraphqlException;
import com.ngdesk.graphql.modules.dao.Condition;
import com.ngdesk.graphql.modules.dao.Module;
import com.ngdesk.graphql.modules.dao.ModuleField;
import com.ngdesk.graphql.modules.dao.ModulesService;
import com.ngdesk.graphql.role.layout.dao.RoleService;
import com.ngdesk.graphql.services.GraphqlService;
import com.ngdesk.repositories.dashboards.DashboardRepository;
import com.ngdesk.repositories.modules.ModulesRepository;
import com.ngdesk.repositories.modules.data.ModuleEntryRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class ScorecardValueFetcher implements DataFetcher<Double> {

	@Autowired
	AuthManager authManager;

	@Autowired
	DashboardRepository dashboardRepository;

	@Autowired
	ModulesService modulesService;

	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	ModuleEntryRepository entryRepository;

	@Autowired
	GraphqlService graphQlService;

	@Autowired
	RoleService roleService;

	@Override
	public Double get(DataFetchingEnvironment environment) throws Exception {

		// Get CompanyId, dashboardId, widgetId from the arguments passed
		String companyId = authManager.getUserDetails().getCompanyId();
		String dashboardId = environment.getArgument("dashboardId");
		String widgetId = environment.getArgument("widgetId");

		// Get Dashboard by companyId, dashboardId
		Optional<Dashboard> optionalDashboard = dashboardRepository.findByCompanyIdAndId(companyId, dashboardId,
				"dashboards");

		// Checks if dashboard isEmpty, If Dashboard isEmpty throw an exception
		if (optionalDashboard.isEmpty()) {
			throw new CustomGraphqlException(400, "INVALID_DASHBOARD", null);
		}

		// Get particular Widget by widgetId
		Dashboard dashboard = optionalDashboard.get();
		Optional<Widget> optionalWidget = dashboard.getWidgets().stream()
				.filter(widget -> widget.getWidgetId().equals(widgetId)).findAny();

		// Checks if Widget isEmpty, If Widget isEmpty throw an exception
		if (optionalWidget.isEmpty()) {
			throw new CustomGraphqlException(400, "INVALID_WIDGET", null);
		}
		// Get ModuleId from particular Widget
		Widget widget = optionalWidget.get();
		String moduleId = widget.getModuleId();

		// Checks if Module is present or not from Modules
		List<Module> modules = modulesRepository.findAllModules("modules_" + companyId);
		Optional<Module> optionalModule = modules.stream().filter(module -> module.getModuleId().equals(moduleId))
				.findAny();

		// Checks if Module isEmpty, If Module isEmpty throw an exception
		if (optionalModule.isEmpty()) {
			throw new CustomGraphqlException(400, "INVALID_MODULE", null);
		}

		Module module = optionalModule.get();
		String collectionName = module.getName().replaceAll("\\s", "_") + "_" + companyId;
		List<ModuleField> allFields = modulesService.getAllFields(module, modules);

		// Checks the WidgetType if widgetType is score
		switch (widget.getType()) {
		case "score":
			ScoreCardWidget scoreCardWidget = (ScoreCardWidget) widget;

			// converting the widgetConditions to Module Conditions
			List<Condition> condition = graphQlService.convertCondition(scoreCardWidget.getDashboardconditions());

			// Check the aggregation field with Type and Field
			if (scoreCardWidget.getAggregateField() != null) {
				Optional<ModuleField> aggregationField = module.getFields().stream()
						.filter(field -> field.getFieldId().equals(scoreCardWidget.getAggregateField())).findFirst();

				if (aggregationField.isEmpty()) {
					throw new CustomGraphqlException(400, "INVALID_AGGREGATION_FIELD", null);
				}
				Optional<ModuleField> orderByField = module.getFields().stream()
						.filter(field -> field.getFieldId().equals(scoreCardWidget.getOrderBy().getColumn()))
						.findFirst();
				OrderBy orderBy = scoreCardWidget.getOrderBy();
				if (orderByField.isPresent()) {
					orderBy.setColumn(orderByField.get().getName());
					scoreCardWidget.setOrderBy(orderBy);
				}

				List<Map<String, Object>> relatedEntries = entryRepository.findAllByScoreCardAggregationField(modules,
						allFields, aggregationField.get().getName(), scoreCardWidget.getLimit(), condition,
						scoreCardWidget.getLimitEntries(), scoreCardWidget.getAggregateType(),
						scoreCardWidget.getOrderBy(), modulesService.getCollectionName(module.getName(), companyId));

				List<Double> values = new ArrayList<Double>();

				Double count = 0.0;

				for (Map<String, Object> entry : relatedEntries) {

					if (entry.get(aggregationField.get().getName()) != null) {
						count = Double.valueOf((entry.get(aggregationField.get().getName())).toString());
					}
				}

				return count;
			}

			List<Condition> accountConditions = accessLevelControl(modules, allFields);
			if (accountConditions != null) {
				condition.addAll(accountConditions);
			}

			return (double) entryRepository.getCountForWidgets(modules, allFields, condition, collectionName,
					widget.getLimit(), widget.getLimitEntries());
		default:
			break;
		}

		return 0.0;
	}

	public List<Condition> accessLevelControl(List<Module> modules, List<ModuleField> moduleFields) {
		if (!roleService.isSystemAdmin(authManager.getUserDetails().getRole())) {
			Map<String, Object> company = entryRepository
					.findEntryById(authManager.getUserDetails().getCompanyId(), "companies").get();
			if (company.containsKey("ACCOUNT_LEVEL_ACCESS")) {
				if (company.get("ACCOUNT_LEVEL_ACCESS").equals(true)) {
					Map<String, Object> userAttributes = authManager.getUserDetails().getAttributes();
					if (userAttributes.containsKey("CONTACT") && userAttributes.get("CONTACT") != null) {
						String contactId = (String) userAttributes.get("CONTACT");
						Optional<Map<String, Object>> optionalContact = entryRepository.findEntryById(contactId,
								"Contacts_" + authManager.getUserDetails().getCompanyId());
						if (optionalContact.isPresent()) {
							List<Condition> conditions = new ArrayList<Condition>();
							String accountId = (String) optionalContact.get().get("ACCOUNT");
							if (accountId != null) {
								Module accountModule = modules.stream()
										.filter(module -> module.getName().equalsIgnoreCase("Accounts")).findFirst()
										.get();
								List<ModuleField> accountFieldsOfCurrentModule = new ArrayList<ModuleField>();
								for (ModuleField field : moduleFields) {
									if (field.getModule() != null
											&& field.getModule().equalsIgnoreCase(accountModule.getModuleId())) {
										accountFieldsOfCurrentModule.add(field);
									}

								}

								for (ModuleField accountFieldOfCurrentModule : accountFieldsOfCurrentModule) {
									Condition accountCondition = new Condition();
									accountCondition.setCondition(accountFieldOfCurrentModule.getFieldId());
									accountCondition.setConditionValue(accountId);
									accountCondition.setRequirementType("Any");
									accountCondition.setOpearator("EQUALS_TO");
									conditions.add(accountCondition);
								}
								if (conditions.size() > 0) {
									return conditions;
								} else {
									return null;
								}
							}
							return null;
						} else {
							return null;
						}
					} else {
						return null;
					}
				} else {
					return null;
				}
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
}
