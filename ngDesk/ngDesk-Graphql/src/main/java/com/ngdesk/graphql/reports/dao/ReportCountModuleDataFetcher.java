package com.ngdesk.graphql.reports.dao;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.commons.managers.SessionManager;
import com.ngdesk.graphql.modules.dao.Condition;
import com.ngdesk.graphql.modules.dao.Module;
import com.ngdesk.graphql.modules.dao.ModuleField;
import com.ngdesk.graphql.modules.dao.ModulesService;
import com.ngdesk.graphql.modules.data.dao.DataService;
import com.ngdesk.graphql.role.layout.dao.RoleLayoutCondition;
import com.ngdesk.graphql.role.layout.dao.RoleService;
import com.ngdesk.repositories.modules.ModulesRepository;
import com.ngdesk.repositories.modules.data.ModuleEntryRepository;
import com.ngdesk.repositories.reports.ReportRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class ReportCountModuleDataFetcher implements DataFetcher<Integer> {

	@Autowired
	SessionManager sessionManager;

	@Autowired
	AuthManager authManager;

	@Autowired
	ReportRepository reportRepository;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	ModulesService modulesService;

	@Autowired
	RoleService roleService;
	
	@Autowired
	DataService dataService;

	@Override
	public Integer get(DataFetchingEnvironment environment) throws Exception {

		String companyId = authManager.getUserDetails().getCompanyId();
		String moduleId = environment.getArgument("moduleId");
		String role = authManager.getUserDetails().getRole();
		
		Boolean isAdmin = false;

		if (roleService.isSystemAdmin(role)) {
			isAdmin = true;
		}

		Set<String> teamIds = dataService.getAllTeamIds(isAdmin);

		List<RoleLayoutCondition> filters = (List<RoleLayoutCondition>) sessionManager.getSessionInfo()
				.get("conditions");

		List<Module> modules = modulesRepository.findAllModules("modules_" + companyId);
		Optional<Module> optionalModule = modules.stream().filter(module -> module.getModuleId().equals(moduleId))
				.findAny();
		Module module = optionalModule.get();
		List<ModuleField> moduleFields = modulesService.getAllFields(module, modules);

		String collectionName = module.getName().replaceAll("\\s", "_") + "_" + companyId;

		List<Condition> conditions = roleService.convertCondition(filters);
		return moduleEntryRepository.getCountForLayouts(modules, moduleFields, conditions, collectionName, teamIds);
	}

}
