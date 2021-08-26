package com.ngdesk.graphql.role.layout.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.commons.managers.SessionManager;
import com.ngdesk.graphql.modules.dao.Condition;
import com.ngdesk.graphql.modules.dao.Module;
import com.ngdesk.graphql.modules.dao.ModuleField;
import com.ngdesk.graphql.modules.dao.ModulesService;
import com.ngdesk.graphql.modules.data.dao.DataService;
import com.ngdesk.repositories.modules.ModulesRepository;
import com.ngdesk.repositories.modules.data.ModuleEntryRepository;
import com.ngdesk.repositories.role.layout.RoleLayoutRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class RoleLayoutValuesDataFetcher implements DataFetcher<List<Map<String, Object>>> {
	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	AuthManager authManager;

	@Autowired
	SessionManager sessionManager;

	@Autowired
	RoleLayoutRepository roleLayoutRepository;

	@Autowired
	ModuleEntryRepository entryRepository;

	@Autowired
	ModulesService modulesService;

	@Autowired
	RoleService roleService;

	@Autowired
	DataService dataService;

	@Override
	public List<Map<String, Object>> get(DataFetchingEnvironment environment) {

		String companyId = authManager.getUserDetails().getCompanyId();
		String layoutId = environment.getArgument("layoutId");
		String tabId = environment.getArgument("tabId");
		Integer page = environment.getArgument("pageNumber");
		Integer pageSize = environment.getArgument("pageSize");
		String sortBy = environment.getArgument("sortBy");
		String orderBy = environment.getArgument("orderBy");

		String role = authManager.getUserDetails().getRole();

		Optional<RoleLayout> optionalRoleLayout = roleLayoutRepository.findByCompanyIdAndId(companyId, layoutId,
				"role_layouts");

		if (optionalRoleLayout.isPresent()) {
			List<Tab> tabs = optionalRoleLayout.get().getTabs();
			Tab tab = tabs.stream().filter(t -> t.getTabId().equals(tabId)).findFirst().get();

			String moduleId = tab.getModule();
			Optional<Module> optionalModule = modulesRepository.findById(moduleId, "modules_" + companyId);

			if (optionalModule.isPresent()) {
				List<Module> modules = modulesRepository.findAllModules("modules_" + companyId);
				Module module = optionalModule.get();
				String collectionName = module.getName().replaceAll("\\s", "_") + "_" + companyId;
				List<ModuleField> allFields = modulesService.getAllFields(module, modules);

				Sort sort = null;
				if (sortBy == null || sortBy.isBlank()) {
					ModuleField sortByField = allFields.stream()
							.filter(field -> field.getFieldId().equals(tab.getOrderBy().getColumn())).findFirst()
							.orElse(null);
					if (sortByField == null) {
						sort = Sort.by("DATE_CREATED");
					} else {
						sort = Sort.by(sortByField.getName());
					}
				} else {
					sort = Sort.by(sortBy);
				}
				if (orderBy == null || orderBy.isBlank()) {
					if (tab.getOrderBy().getOrder().equals("Desc")) {
						sort = sort.descending();
					} else {
						sort = sort.ascending();
					}
				} else {
					if (orderBy.equalsIgnoreCase("asc")) {
						sort = sort.ascending();
					} else {
						sort = sort.descending();
					}
					Pageable pageable = PageRequest.of(page, pageSize, sort);
					sessionManager.getSessionInfo().put("modulesMap", modules);
					List<Condition> conditions = roleService.convertCondition(tab.getConditions());

					List<Condition> accountConditions = accessLevelControl(modules, allFields);
					if (accountConditions != null) {
						conditions.addAll(accountConditions);
					}

					return entryRepository.findEntriesForLayout(modules, allFields, conditions, pageable,
							collectionName, dataService.getAllTeamIds(roleService.isSystemAdmin(role)), module);
				}

			}
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
