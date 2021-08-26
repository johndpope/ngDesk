package com.ngdesk.graphql.modules.data.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.graphql.modules.dao.Condition;
import com.ngdesk.graphql.modules.dao.ListLayout;
import com.ngdesk.graphql.modules.dao.ListMobileLayout;
import com.ngdesk.graphql.modules.dao.Module;
import com.ngdesk.graphql.modules.dao.ModuleField;
import com.ngdesk.graphql.modules.dao.ModulesService;
import com.ngdesk.graphql.role.layout.dao.RoleService;
import com.ngdesk.repositories.modules.ModulesRepository;
import com.ngdesk.repositories.modules.data.ModuleEntryRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class CountDataFetcher implements DataFetcher<Integer> {

	@Autowired
	ModuleEntryRepository entryRepository;

	@Autowired
	AuthManager authManager;

	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	ModulesService modulesService;

	@Autowired
	DataService dataService;

	@Autowired
	RoleService roleService;

	@Override
	public Integer get(DataFetchingEnvironment environment) {

		String companyId = authManager.getUserDetails().getCompanyId();
		String role = authManager.getUserDetails().getRole();
		String moduleId = environment.getArgument("moduleId");
		String layoutId = environment.getArgument("layoutId");
		String search = environment.getArgument("search");
		Boolean includeConditions = environment.getArgument("includeConditions");

		Optional<Module> optionalModule = modulesRepository.findById(moduleId, "modules_" + companyId);
		if (optionalModule.isEmpty()) {
			// TODO: HANDLE CUSTOM ERROR
			return 0;
		}
		Module module = optionalModule.get();
		String collectionName = module.getName().replaceAll("\\s", "_") + "_" + companyId;
		
		Boolean isAdmin = false;

		if (roleService.isSystemAdmin(role)) {
			isAdmin = true;
		}

		Set<String> teamIds = dataService.getAllTeamIds(isAdmin);

		if ((search != null && !search.isBlank()) || layoutId == null || layoutId.isBlank()) {

			if (search != null && !search.isBlank()) {
				List<String> entryIds = dataService.getIdsFromGlobalSearch(search, module, teamIds);
				if (entryIds == null) {
					entryIds = new ArrayList<String>();
				}
				if (layoutId != null && !layoutId.isBlank()) {
					List<Module> modules = modulesRepository.findAllModules("modules_" + companyId);
					List<ModuleField> moduleFields = modulesService.getAllFields(module, modules);
					List<ListMobileLayout> mobileListLayouts = module.getListMobileLayouts();
					List<ListLayout> webListLayouts = module.getListLayouts();

					Optional<ListLayout> optionalWebListLayout = webListLayouts.stream()
							.filter(layout -> layout.getLayoutId().equals(layoutId) && layout.getRole().equals(role))
							.findFirst();

					if (optionalWebListLayout.isEmpty()) {

						Optional<ListMobileLayout> optionalMobileListLayout = mobileListLayouts.stream().filter(
								layout -> layout.getLayoutId().equals(layoutId) && layout.getRole().equals(role))
								.findFirst();
						if (optionalMobileListLayout.isEmpty()) {
							return 0;
						}

						ListMobileLayout layout = optionalMobileListLayout.get();
						if (includeConditions) {
							return entryRepository.getCountForSearchIncludingConditions(entryIds,
									layout.getConditions(), modules, moduleFields, collectionName);
						}
						return entryRepository.getCountForSearch(entryIds, collectionName);

					}
					ListLayout layout = optionalWebListLayout.get();
					if (includeConditions) {
						return entryRepository.getCountForSearchIncludingConditions(entryIds, layout.getConditions(),
								modules, moduleFields, collectionName);
					}
					return entryRepository.getCountForSearch(entryIds, collectionName);

				} else {
					return entryRepository.getCountForSearch(entryIds, collectionName);
				}
			}

			return entryRepository.getCount(collectionName);
		}
		List<Module> modules = modulesRepository.findAllModules("modules_" + companyId);
		List<ModuleField> moduleFields = modulesService.getAllFields(module, modules);
		List<ListMobileLayout> mobileListLayouts = module.getListMobileLayouts();
		List<ListLayout> webListLayouts = module.getListLayouts();

		Optional<ListLayout> optionalWebListLayout = webListLayouts.stream()
				.filter(layout -> layout.getLayoutId().equals(layoutId) && layout.getRole().equals(role)).findFirst();

		List<Condition> accountConditions = accessLevelControl(modules, moduleFields);
		if (optionalWebListLayout.isEmpty()) {

			Optional<ListMobileLayout> optionalMobileListLayout = mobileListLayouts.stream()
					.filter(layout -> layout.getLayoutId().equals(layoutId) && layout.getRole().equals(role))
					.findFirst();
			if (optionalMobileListLayout.isEmpty()) {
				return 0;
			}

			ListMobileLayout layout = optionalMobileListLayout.get();
			List<Condition> conditions = layout.getConditions();
			if (accountConditions != null) {
				conditions.addAll(accountConditions);
			}
			return entryRepository.getCountForLayouts(modules, moduleFields, layout.getConditions(), collectionName, teamIds);
		}
		ListLayout layout = optionalWebListLayout.get();
		List<Condition> conditions = layout.getConditions();
		if (accountConditions != null) {
			conditions.addAll(accountConditions);
		}
		return entryRepository.getCountForLayouts(modules, moduleFields, layout.getConditions(), collectionName, teamIds);
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
