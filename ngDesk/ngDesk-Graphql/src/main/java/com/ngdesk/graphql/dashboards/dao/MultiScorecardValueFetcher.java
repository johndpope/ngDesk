package com.ngdesk.graphql.dashboards.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.commons.models.Dashboard;
import com.ngdesk.commons.models.MultiScoreCardWidget;
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
public class MultiScorecardValueFetcher implements DataFetcher<List<FieldValueCount>> {

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
	public List<FieldValueCount> get(DataFetchingEnvironment environment) throws Exception {

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

		Widget widget = optionalWidget.get();

		// Checks the WidgetType if widgetType is multi-score
		switch (widget.getType()) {
		case "multi-score":

			List<FieldValueCount> fieldValueCount = new ArrayList<FieldValueCount>();
			MultiScoreCardWidget multiScoreCardWidget = (MultiScoreCardWidget) widget;
			List<ScoreCardWidget> scorecards = multiScoreCardWidget.getMultiScorecards();

			for (ScoreCardWidget scorecard : scorecards) {
				Object name = scorecard.getWidgetId();

				// converting the widgetConditions to Module Conditions
				List<Condition> condition = graphQlService.convertCondition(scorecard.getDashboardconditions());

				// Get Module from particular Widget
				String moduleId = scorecard.getModuleId();
				List<Module> modules = modulesRepository.findAllModules("modules_" + companyId);

				// Checks if Module is present or not from Modules
				Optional<Module> optionalModule = modules.stream()
						.filter(module -> module.getModuleId().equals(moduleId)).findAny();

				// Checks if Module isEmpty, If Module isEmpty throw an exception
				if (optionalModule.isEmpty()) {
					throw new CustomGraphqlException(400, "INVALID_MODULE", null);
				}

				Module module = optionalModule.get();
				String collectionName = module.getName().replaceAll("\\s", "_") + "_" + companyId;
				List<ModuleField> allFields = modulesService.getAllFields(module, modules);

				List<Condition> accountConditions = accessLevelControl(modules, allFields);
				if (accountConditions != null) {
					condition.addAll(accountConditions);
				}

				Double count = (double) entryRepository.getCountForWidgets(modules, allFields, condition,
						collectionName, scorecard.getLimit(), scorecard.getLimitEntries());
				fieldValueCount.add(new FieldValueCount(name, count, "no_id"));

				// Check the aggregation field with Type and Field
				if (scorecard.getAggregateField() != null) {
					Optional<ModuleField> aggregationField = module.getFields().stream()
							.filter(field -> field.getFieldId().equals(scorecard.getAggregateField())).findFirst();

					if (aggregationField.isEmpty()) {
						throw new CustomGraphqlException(400, "INVALID_AGGREGATION_FIELD", null);
					}
					Optional<ModuleField> orderByField = module.getFields().stream()
							.filter(field -> field.getFieldId().equals(scorecard.getOrderBy().getColumn())).findFirst();
					OrderBy orderBy = scorecard.getOrderBy();
					if (orderByField.isPresent()) {
						orderBy.setColumn(orderByField.get().getName());
						scorecard.setOrderBy(orderBy);
					}
					List<Map<String, Object>> relatedEntries = entryRepository.findAllByScoreCardAggregationField(
							modules, allFields, aggregationField.get().getName(), scorecard.getLimit(), condition,
							scorecard.getLimitEntries(), scorecard.getAggregateType(), scorecard.getOrderBy(),
							modulesService.getCollectionName(module.getName(), companyId));

					Double count2 = 0.0;

					for (Map<String, Object> entry : relatedEntries) {

						if (entry.get(aggregationField.get().getName()) != null) {
							count2 = Double.valueOf((entry.get(aggregationField.get().getName())).toString());

						}
					}

					fieldValueCount.add(new FieldValueCount(name, count2, "no_id"));
				}

			}

			return fieldValueCount;
		default:
			break;
		}

		return null;
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