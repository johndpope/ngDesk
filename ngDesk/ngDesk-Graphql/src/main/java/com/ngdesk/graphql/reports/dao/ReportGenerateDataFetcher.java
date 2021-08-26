package com.ngdesk.graphql.reports.dao;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.commons.managers.SessionManager;
import com.ngdesk.graphql.CustomGraphqlException;
import com.ngdesk.graphql.controllers.ReportInput;
import com.ngdesk.graphql.modules.dao.Condition;
import com.ngdesk.graphql.modules.dao.Module;
import com.ngdesk.graphql.modules.dao.ModuleField;
import com.ngdesk.graphql.modules.dao.ModulesService;
import com.ngdesk.graphql.modules.data.dao.DataService;
import com.ngdesk.graphql.role.layout.dao.RoleLayoutCondition;
import com.ngdesk.graphql.role.layout.dao.RoleService;
import com.ngdesk.repositories.modules.ModulesRepository;
import com.ngdesk.repositories.modules.data.ModuleEntryRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class ReportGenerateDataFetcher implements DataFetcher<List<Map<String, Object>>> {
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
	SessionManager sessionManager;

	ObjectMapper mapper = new ObjectMapper();

	@Override
	public List<Map<String, Object>> get(DataFetchingEnvironment environment) throws Exception {

		String companyId = authManager.getUserDetails().getCompanyId();
		String role = authManager.getUserDetails().getRole();
		String moduleId = environment.getArgument("moduleId");

		ReportInput reportInput = (ReportInput) sessionManager.getSessionInfo().get("reports");
		List<RoleLayoutCondition> filters = reportInput.getConditions();

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
		List<ModuleField> moduleFields = modulesService.getAllFields(module, modules);
		sessionManager.getSessionInfo().put("modules", modules);
		sessionManager.getSessionInfo().put("currentModule", module);

		String collectionName = module.getName().replaceAll("\\s", "_") + "_" + companyId;

		List<Condition> conditions = roleService.convertCondition(filters);

		return moduleEntryRepository.findEntriesForLayout(modules, moduleFields, conditions, null, collectionName,
				teamIds, module);

	}
}