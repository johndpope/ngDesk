package com.ngdesk.graphql.modules.data.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.exceptions.ForbiddenException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.commons.managers.SessionManager;
import com.ngdesk.graphql.CustomGraphqlException;
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
public class AllEntriesFetcher implements DataFetcher<List<Map<String, Object>>> {

	@Autowired
	ModuleEntryRepository entryRepository;

	@Autowired
	AuthManager authManager;

	ObjectMapper mapper = new ObjectMapper();

	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	ModulesService modulesService;

	@Autowired
	DataService dataService;

	@Autowired
	RoleService roleService;

	@Autowired
	SessionManager sessionManager;

	@Override
	public List<Map<String, Object>> get(DataFetchingEnvironment environment) {
		// When is customer should always be true
		String companyId = authManager.getUserDetails().getCompanyId();
		String role = authManager.getUserDetails().getRole();

		String moduleId = environment.getArgument("moduleId");
		Integer page = environment.getArgument("pageNumber");
		Integer pageSize = environment.getArgument("pageSize");
		String layoutId = environment.getArgument("layoutId");
		String sortBy = environment.getArgument("sortBy");
		String orderBy = environment.getArgument("orderBy");
		String search = environment.getArgument("search");
		Boolean includeConditions = environment.getArgument("includeConditions");

		Optional<Module> optionalModule = modulesRepository.findById(moduleId, "modules_" + companyId);
		if (optionalModule.isEmpty()) {
			throw new CustomGraphqlException(400, "INVALID_MODULE", null);
		}
		Module module = optionalModule.get();
		sessionManager.getSessionInfo().put("currentModule", module);

		if (!roleService.isAuthorizedForRecord(authManager.getUserDetails().getRole(), "GET", module.getModuleId())) {
			throw new CustomGraphqlException(400, "FORBIDDEN", null);
		}

		List<Module> modules = modulesRepository.findAllModules("modules_" + companyId);
		String collectionName = module.getName().replaceAll("\\s", "_") + "_" + companyId;

		Boolean isAdmin = false;

		if (roleService.isSystemAdmin(role)) {
			isAdmin = true;
		}

		if (roleService.isCustomer(role)) {
			includeConditions = true;
		}

		Set<String> teamIds = dataService.getAllTeamIds(isAdmin);

		if ((search != null && !search.isBlank()) || layoutId == null || layoutId.isBlank()) {
			Sort sort = null;
			if (sortBy == null) {
				sort = Sort.by("DATE_CREATED");
			} else {
				sort = Sort.by(sortBy);
			}

			if (orderBy == null) {
				sort = sort.descending();
			} else {
				if (orderBy.equalsIgnoreCase("asc")) {
					sort = sort.ascending();
				} else {
					sort = sort.descending();
				}
			}

			Pageable pageable = PageRequest.of(page, pageSize, sort);
			if (search != null && !search.isBlank()) {
				List<String> entryIds = dataService.getIdsFromGlobalSearch(search, module, teamIds);

				if (entryIds == null) {
					entryIds = new ArrayList<String>();
				}

				if (layoutId != null && !layoutId.isBlank()) {
					List<ModuleField> moduleFields = modulesService.getAllFields(module, modules);

					List<ListLayout> webListLayouts = module.getListLayouts();
					List<ListMobileLayout> mobileListLayouts = module.getListMobileLayouts();

					Optional<ListLayout> optionalWebListLayout = webListLayouts.stream()
							.filter(layout -> layout.getRole().equals(role) && layout.getLayoutId().equals(layoutId))
							.findFirst();
					if (optionalWebListLayout.isEmpty()) {

						Optional<ListMobileLayout> optionalMobileListLayout = mobileListLayouts.stream().filter(
								layout -> layout.getRole().equals(role) && layout.getLayoutId().equals(layoutId))
								.findFirst();
						if (optionalMobileListLayout.isEmpty()) {
							return new ArrayList<Map<String, Object>>();
						}

						ListMobileLayout layout = optionalMobileListLayout.get();
						if (includeConditions) {
							return entryRepository.findEntriesWithSearchIncludingConditions(entryIds,
									layout.getConditions(), modules, moduleFields, pageable, collectionName);
						}
						return entryRepository.findEntriesWithSearch(entryIds, pageable, collectionName);

					}
					ListLayout layout = optionalWebListLayout.get();

					if (includeConditions) {
						return entryRepository.findEntriesWithSearchIncludingConditions(entryIds,
								layout.getConditions(), modules, moduleFields, pageable, collectionName);
					}
					return entryRepository.findEntriesWithSearch(entryIds, pageable, collectionName);

				} else {
					return entryRepository.findEntriesWithSearch(entryIds, pageable, collectionName);
				}

			}
			return entryRepository.findEntries(pageable, teamIds, collectionName);
		} else {
			List<ModuleField> moduleFields = modulesService.getAllFields(module, modules);

			List<ListLayout> webListLayouts = module.getListLayouts();
			List<ListMobileLayout> mobileListLayouts = module.getListMobileLayouts();

			Optional<ListLayout> optionalWebListLayout = webListLayouts.stream()
					.filter(layout -> layout.getRole().equals(role) && layout.getLayoutId().equals(layoutId))
					.findFirst();

			if (optionalWebListLayout.isEmpty()) {

				Optional<ListMobileLayout> optionalMobileListLayout = mobileListLayouts.stream()
						.filter(layout -> layout.getRole().equals(role) && layout.getLayoutId().equals(layoutId))
						.findFirst();
				if (optionalMobileListLayout.isEmpty()) {
					return new ArrayList<Map<String, Object>>();
				}

				ListMobileLayout layout = optionalMobileListLayout.get();
				Sort sort = null;
				if (sortBy == null || sortBy.isBlank()) {
					ModuleField sortByField = moduleFields.stream()
							.filter(field -> field.getFieldId().equals(layout.getOrderBy().getColumn())).findFirst()
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
					if (layout.getOrderBy().getOrder().equals("Desc")) {
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
				}
				Pageable pageable = PageRequest.of(page, pageSize, sort);

				List<Condition> accountConditions = accessLevelControl(modules, moduleFields);
				List<Condition> conditions = layout.getConditions();
				if (accountConditions != null) {
					conditions.addAll(accountConditions);
				}

				return entryRepository.findEntriesForLayout(modules, moduleFields, conditions, pageable, collectionName,
						teamIds, module);

			}
			ListLayout layout = optionalWebListLayout.get();
			Sort sort = null;
			if (sortBy == null || sortBy.isBlank()) {
				ModuleField sortByField = moduleFields.stream()
						.filter(field -> field.getFieldId().equals(layout.getOrderBy().getColumn())).findFirst()
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
				if (layout.getOrderBy().getOrder().equals("Desc")) {
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
			}
			Pageable pageable = PageRequest.of(page, pageSize, sort);
			List<Condition> accountConditions = accessLevelControl(modules, moduleFields);
			List<Condition> conditions = layout.getConditions();
			if (accountConditions != null) {
				conditions.addAll(accountConditions);
			}
			return entryRepository.findEntriesForLayout(modules, moduleFields, conditions, pageable, collectionName,
					teamIds, module);
		}

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
