package com.ngdesk.graphql.reports.dao;

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
import com.ngdesk.graphql.modules.data.dao.DataService;
import com.ngdesk.graphql.role.layout.dao.RoleLayoutCondition;
import com.ngdesk.graphql.role.layout.dao.RoleService;
import com.ngdesk.repositories.modules.ModulesRepository;
import com.ngdesk.repositories.modules.data.ModuleEntryRepository;
import com.ngdesk.repositories.role.RolesRepository;

import graphql.language.Field;
import graphql.language.Selection;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class ReportModuleDataFetcher implements DataFetcher<List<Map<String, Object>>> {

	@Autowired
	AuthManager authManager;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	ModulesService modulesService;

	@Autowired
	private DataService dataService;

	@Autowired
	RoleService roleService;

	@Autowired
	RolesRepository rolesRepository;

	@Autowired
	SessionManager sessionManager;

	@Override
	public List<Map<String, Object>> get(DataFetchingEnvironment environment) throws Exception {

		String companyId = authManager.getUserDetails().getCompanyId();
		String role = authManager.getUserDetails().getRole();
		Integer page = environment.getArgument("pageNumber");
		Integer pageSize = environment.getArgument("pageSize");
		String sortBy = environment.getArgument("sortBy");
		String orderBy = environment.getArgument("orderBy");
		String moduleId = environment.getArgument("moduleId");

		List<RoleLayoutCondition> filters = (List<RoleLayoutCondition>) sessionManager.getSessionInfo()
				.get("conditions");

		if (page == null || page < 0) {
			page = 0;
		}

		if (pageSize == null || pageSize < 0) {
			pageSize = 20;
		}
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
		Boolean isAdmin = false;

		if (roleService.isSystemAdmin(role)) {
			isAdmin = true;
		}

		Set<String> teamIds = dataService.getAllTeamIds(isAdmin);

		List<Module> modules = modulesRepository.findAllModules("modules_" + companyId);
		Optional<Module> optionalModule = modules.stream().filter(module -> module.getModuleId().equals(moduleId))
				.findAny();

		if (optionalModule.isEmpty()) {
			throw new CustomGraphqlException(400, "INVALID_MODULE", null);
		}

		Module module = optionalModule.get();
		sessionManager.getSessionInfo().put("currentModule", module);

		List<ModuleField> moduleFields = modulesService.getAllFields(module, modules);

		String collectionName = module.getName().replaceAll("\\s", "_") + "_" + companyId;

		List<Condition> conditions = roleService.convertCondition(filters);

		return moduleEntryRepository.findEntriesForLayout(modules, moduleFields, conditions, pageable, collectionName,
				teamIds, module);

	}
}
