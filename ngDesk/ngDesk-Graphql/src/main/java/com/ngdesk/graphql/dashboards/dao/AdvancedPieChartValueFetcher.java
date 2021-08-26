package com.ngdesk.graphql.dashboards.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.commons.models.AdvancedPieChartWidget;
import com.ngdesk.commons.models.Dashboard;
import com.ngdesk.commons.models.OrderBy;
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
public class AdvancedPieChartValueFetcher implements DataFetcher<List<AdvancePieChart>> {

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
	public List<AdvancePieChart> get(DataFetchingEnvironment environment) throws Exception {

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

		// Get Module from particular Widget
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

		// Checks the WidgetType if widgetType is advanced-pie
		switch (widget.getType()) {
		case "advanced-pie":
			AdvancedPieChartWidget pieChartWidget = (AdvancedPieChartWidget) widget;
			ModuleField existingField = module.getFields().stream()
					.filter(field -> field.getFieldId().equals(pieChartWidget.getField())).findFirst().orElse(null);

			String fieldName = existingField.getName();
			List<Object> allUniqueValues = entryRepository.findDistinctEntriyValues(fieldName, collectionName);

			Map<String, String> loopUpMap = new HashMap<String, String>();

			// checks the relationship field
			if (existingField.getDataType().getDisplay().equalsIgnoreCase("Relationship")) {
				if (existingField.getRelationshipType().equalsIgnoreCase("Many to One")) {

					Optional<Module> relatedModule = modules.stream()
							.filter(relModule -> relModule.getModuleId().equals(existingField.getModule())).findAny();
					if (relatedModule.isEmpty()) {
						throw new CustomGraphqlException(400, "INVALID_RELATED_MODULE", null);
					}
					Module relationshipModule = relatedModule.get();
					String primaryDisplayField1 = existingField.getPrimaryDisplayField();

					Optional<ModuleField> optionalRelationship = relationshipModule.getFields().stream()
							.filter(relatedModuleField -> relatedModuleField.getFieldId().equals(primaryDisplayField1))
							.findFirst();

					if (optionalRelationship.isEmpty()) {
						throw new CustomGraphqlException(400, "INVALID_PRIMARY_FIELD", null);
					}

					List<Map<String, Object>> relatedEntries = entryRepository.findAllByDashboardRelationship(
							optionalRelationship.get().getName(),
							modulesService.getCollectionName(relatedModule.get().getName(), companyId));

					relatedEntries.forEach(relatedEntry -> {
						loopUpMap.put(relatedEntry.get("_id").toString(),
								relatedEntry.get(optionalRelationship.get().getName()).toString());
					});
				}
			}

			// converting the widgetConditions to Module Conditions
			List<Condition> conditions = graphQlService.convertCondition(pieChartWidget.getDashboardconditions());
			List<AdvancePieChart> advancePiChart = new ArrayList<AdvancePieChart>();
			Double count = 0.0;
			long totalCount = 0;
			float percentage = 0;
			// Check the aggregation field with Type and Field

			if (pieChartWidget.getAggregateField() != null) {
				Optional<ModuleField> aggregationField = module.getFields().stream()
						.filter(field -> field.getFieldId().equals(pieChartWidget.getAggregateField())).findFirst();

				if (aggregationField.isEmpty()) {
					throw new CustomGraphqlException(400, "INVALID_AGGREGATION_FIELD", null);
				}
				Optional<ModuleField> orderByField = module.getFields().stream()
						.filter(field -> field.getFieldId().equals(pieChartWidget.getOrderBy().getColumn()))
						.findFirst();
				OrderBy orderBy = pieChartWidget.getOrderBy();
				if (orderByField.isPresent()) {
					orderBy.setColumn(orderByField.get().getName());
					pieChartWidget.setOrderBy(orderBy);
				}
				List<Map<String, Object>> relatedEntries = entryRepository.findAllByAggregationField(modules, allFields,
						aggregationField.get().getName(), pieChartWidget.getLimit(), conditions,
						pieChartWidget.getLimitEntries(), allUniqueValues, fieldName, pieChartWidget.getAggregateType(),
						pieChartWidget.getOrderBy(), modulesService.getCollectionName(module.getName(), companyId));

				for (Map<String, Object> entry : relatedEntries) {

					if (entry.get(aggregationField.get().getName().toString()) == null) {

						entry.put(aggregationField.get().getName(), 0.0);
					}

					count = Double.parseDouble(entry.get(aggregationField.get().getName()).toString());
					totalCount = (long) (totalCount + count);
				}
				for (Map<String, Object> entry : relatedEntries) {

					count = Double.parseDouble(entry.get(aggregationField.get().getName()).toString());

					if (!existingField.getDataType().getDisplay().equalsIgnoreCase("Relationship")) {

						percentage = (float) (count * 100) / totalCount;

						Double convertedPercentage = (double) (Math.round(percentage * 100.0) / 100.0);

						advancePiChart
								.add(new AdvancePieChart(entry.get("_id"), convertedPercentage, count, totalCount));
					} else {

						percentage = (float) (count * 100) / totalCount;

						Double convertedPercentage = (double) (Math.round(percentage * 100.0) / 100.0);

						advancePiChart.add(new AdvancePieChart(loopUpMap.get(entry.get("_id")), convertedPercentage,
								count, totalCount));
					}
				}

			}

			// fetch by unique value
			List<Condition> accountConditions = accessLevelControl(modules, allFields);
			if (accountConditions != null) {
				conditions.addAll(accountConditions);
			}

			List<Map<String, Object>> entries = entryRepository.getAllEntriesField(modules, allFields, fieldName,
					allUniqueValues, conditions, collectionName, pieChartWidget.getLimitEntries(),
					pieChartWidget.getLimit());

			Map<Object, Double> map = new HashMap<Object, Double>();
			String fieldValue = null;

			for (Map<String, Object> entry : entries) {
				fieldValue = entry.get(fieldName).toString();
				if (map.containsKey(fieldValue)) {
					count = map.get(fieldValue);
					map.put(fieldValue, count + 1);
				} else {
					map.put(fieldValue, 1.0);
				}
			}
			totalCount = entryRepository.getCount(collectionName);

			for (Object key : map.keySet()) {
				if (loopUpMap.get(key) != null) {
					if (pieChartWidget.getAggregateField() == null) {
						percentage = (float) (map.get(key) * 100) / totalCount;
						Double convertedPercentage = (double) (Math.round(percentage * 100.0) / 100.0);
						advancePiChart.add(
								new AdvancePieChart(loopUpMap.get(key), convertedPercentage, map.get(key), totalCount));
					}
				}

				if (!existingField.getDataType().getDisplay().equalsIgnoreCase("Relationship")) {
					if (pieChartWidget.getAggregateField() == null) {
						percentage = (float) (map.get(key) * 100) / totalCount;

						Double convertedPercentage = (double) (Math.round(percentage * 100.0) / 100.0);

						advancePiChart.add(new AdvancePieChart(key, convertedPercentage, map.get(key), totalCount));
					}
				}
			}

			return advancePiChart;
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
