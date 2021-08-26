package com.ngdesk.graphql.dashboards.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.commons.managers.SessionManager;
import com.ngdesk.commons.models.AdvancedPieChartWidget;
import com.ngdesk.commons.models.BarChartWidget;
import com.ngdesk.commons.models.Dashboard;
import com.ngdesk.commons.models.DashboardCondition;
import com.ngdesk.commons.models.PieChartWidget;
import com.ngdesk.commons.models.Widget;
import com.ngdesk.graphql.CustomGraphqlException;
import com.ngdesk.graphql.modules.dao.Condition;
import com.ngdesk.graphql.modules.dao.Module;
import com.ngdesk.graphql.modules.dao.ModuleField;
import com.ngdesk.graphql.modules.dao.ModulesService;
import com.ngdesk.graphql.modules.data.dao.DataService;
import com.ngdesk.graphql.role.layout.dao.RoleService;
import com.ngdesk.graphql.services.GraphqlService;
import com.ngdesk.repositories.dashboards.DashboardRepository;
import com.ngdesk.repositories.modules.ModulesRepository;
import com.ngdesk.repositories.modules.data.ModuleEntryRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class WidgetEntriesCountFetcher implements DataFetcher<Integer> {
	@Autowired
	AuthManager authManager;

	@Autowired
	DashboardRepository dashboardRepository;

	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	ModulesService modulesService;

	@Autowired
	SessionManager sessionManager;

	@Autowired
	GraphqlService graphQlService;

	@Autowired
	ModuleEntryRepository entryRepository;

	@Autowired
	RoleService roleService;
	
	@Autowired
	DataService dataService;

	@Override
	public Integer get(DataFetchingEnvironment environment) throws Exception {
		try {
			String companyId = authManager.getUserDetails().getCompanyId();
			String role = authManager.getUserDetails().getRole();
			String dashboardId = environment.getArgument("dasboardId");
			String widgetId = environment.getArgument("widgetId");
			String value = environment.getArgument("value");
			
			Boolean isAdmin = false;

			if (roleService.isSystemAdmin(role)) {
				isAdmin = true;
			}

			Set<String> teamIds = dataService.getAllTeamIds(isAdmin);

			Optional<Dashboard> optionalDashboard = dashboardRepository.findByCompanyIdAndId(companyId, dashboardId,
					"dashboards");
			if (optionalDashboard.isPresent()) {
				Dashboard dashboard = optionalDashboard.get();
				Widget requiredWidget = dashboard.getWidgets().stream()
						.filter(widget -> widget.getWidgetId().equals(widgetId)).findFirst().orElse(null);
				if (requiredWidget != null) {
					String moduleId = requiredWidget.getModuleId();
					List<Module> modules = modulesRepository.findAllModules("modules_" + companyId);
					Module requiredModule = modules.stream().filter(module -> module.getModuleId().equals(moduleId))
							.findFirst().orElse(null);
					String collectionName = requiredModule.getName().replaceAll("\\s", "_") + "_" + companyId;
					List<ModuleField> allFields = modulesService.getAllFields(requiredModule, modules);

					if (requiredModule != null) {
						if (requiredWidget.getType().equals("score")) {
							sessionManager.getSessionInfo().put("modulesMap", modules);
							List<Condition> conditions = graphQlService
									.convertCondition(requiredWidget.getDashboardconditions());

							List<Condition> accountConditions = accessLevelControl(modules, allFields);
							if (accountConditions != null) {
								conditions.addAll(accountConditions);
							}

							return entryRepository.getCountForLayouts(modules, allFields, conditions, collectionName, teamIds);

						} else if (requiredWidget.getType().equals("bar-horizontal")) {
							BarChartWidget barchartWidget = (BarChartWidget) requiredWidget;

							sessionManager.getSessionInfo().put("modulesMap", modules);
							if (value != null) {
								DashboardCondition condition = new DashboardCondition();
								condition.setCondition(barchartWidget.getField());
								condition.setValue(value);
								condition.setOperator("EQUALS_TO");
								condition.setRequirementType("All");
								barchartWidget.getDashboardconditions().add(condition);
							}
							List<Condition> conditions = graphQlService
									.convertCondition(barchartWidget.getDashboardconditions());

							List<Condition> accountConditions = accessLevelControl(modules, allFields);
							if (accountConditions != null) {
								conditions.addAll(accountConditions);
							}

							return entryRepository.getCountForLayouts(modules, allFields, conditions, collectionName, teamIds);

						} else if (requiredWidget.getType().equals("pie")) {
							PieChartWidget piechartWidget = (PieChartWidget) requiredWidget;

							sessionManager.getSessionInfo().put("modulesMap", modules);
							if (value != null) {
								DashboardCondition condition = new DashboardCondition();
								condition.setCondition(piechartWidget.getField());
								condition.setValue(value);
								condition.setOperator("EQUALS_TO");
								condition.setRequirementType("All");
								piechartWidget.getDashboardconditions().add(condition);
							}
							List<Condition> conditions = graphQlService
									.convertCondition(piechartWidget.getDashboardconditions());

							List<Condition> accountConditions = accessLevelControl(modules, allFields);
							if (accountConditions != null) {
								conditions.addAll(accountConditions);
							}

							return entryRepository.getCountForLayouts(modules, allFields, conditions, collectionName, teamIds);

						} else if (requiredWidget.getType().equals("advanced-pie")) {
							AdvancedPieChartWidget advancedPiechartWidget = (AdvancedPieChartWidget) requiredWidget;

							sessionManager.getSessionInfo().put("modulesMap", modules);

							if (value != null) {
								DashboardCondition condition = new DashboardCondition();
								condition.setCondition(advancedPiechartWidget.getField());
								condition.setValue(value);
								condition.setOperator("EQUALS_TO");
								condition.setRequirementType("All");
								advancedPiechartWidget.getDashboardconditions().add(condition);
							}
							List<Condition> conditions = graphQlService
									.convertCondition(advancedPiechartWidget.getDashboardconditions());

							List<Condition> accountConditions = accessLevelControl(modules, allFields);
							if (accountConditions != null) {
								conditions.addAll(accountConditions);
							}

							return entryRepository.getCountForLayouts(modules, allFields, conditions, collectionName, teamIds);

						}

					}
					throw new CustomGraphqlException(400, "INVALID_MODULE", null);
				}

				throw new CustomGraphqlException(400, "INVALID_WIDGET", null);
			}

			throw new CustomGraphqlException(400, "INVALID_DASHBOARD", null);

		} catch (Exception e) {
			e.printStackTrace();
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
