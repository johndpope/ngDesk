package com.ngdesk.graphql.modules.data.dao;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.commons.managers.SessionManager;
import com.ngdesk.graphql.CustomGraphqlException;
import com.ngdesk.graphql.modules.dao.Condition;
import com.ngdesk.graphql.modules.dao.Module;
import com.ngdesk.graphql.modules.dao.ModuleField;
import com.ngdesk.graphql.modules.dao.ModulesService;
import com.ngdesk.graphql.role.layout.dao.RoleLayoutCondition;
import com.ngdesk.graphql.role.layout.dao.RoleService;
import com.ngdesk.repositories.modules.ModulesRepository;
import com.ngdesk.repositories.modules.data.ModuleEntryRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class AllEntriesByConditionFetcher implements DataFetcher<List<Map<String, Object>>> {

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

	@Autowired
	SessionManager sessionManager;

	@Override
	public List<Map<String, Object>> get(DataFetchingEnvironment environment) throws Exception {

		String moduleId = environment.getArgument("moduleId");

		Integer page = environment.getArgument("pageNumber");
		Integer pageSize = environment.getArgument("pageSize");
		String sortBy = environment.getArgument("sortBy");
		String orderBy = environment.getArgument("orderBy");

		Optional<Module> optionalModule = modulesRepository.findById(moduleId,
				"modules_" + authManager.getUserDetails().getCompanyId());
		if (optionalModule.isEmpty()) {
			throw new CustomGraphqlException(400, "INVALID_MODULE", null);
		}

		Module module = optionalModule.get();
		sessionManager.getSessionInfo().put("currentModule", module);

		if (!roleService.isAuthorizedForRecord(authManager.getUserDetails().getRole(), "GET", module.getModuleId())) {
			throw new CustomGraphqlException(400, "FORBIDDEN", null);
		}

		Boolean isAdmin = false;

		if (roleService.isSystemAdmin(authManager.getUserDetails().getRole())) {
			isAdmin = true;
		}

		Set<String> teamIds = dataService.getAllTeamIds(isAdmin);

		List<Module> modules = modulesRepository
				.findAllModules("modules_" + authManager.getUserDetails().getCompanyId());
		String collectionName = module.getName().replaceAll("\\s", "_") + "_"
				+ authManager.getUserDetails().getCompanyId();

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

		List<RoleLayoutCondition> conditionsList = (List<RoleLayoutCondition>) sessionManager.getSessionInfo()
				.get("conditions");

		List<Condition> conditions = roleService.convertCondition(conditionsList);

		List<ModuleField> moduleFields = modulesService.getAllFields(module, modules);

		Pageable pageable = PageRequest.of(page, pageSize, sort);

		Optional<List<Map<String, Object>>> optionalEntriesList = entryRepository.findEntriesWithConditions(conditions,
				pageable, collectionName, modules, moduleFields, teamIds);
		if (optionalEntriesList.isPresent() && isAdmin) {
			return optionalEntriesList.get();
		}

		return null;

	}

}
