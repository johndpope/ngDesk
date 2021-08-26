package com.ngdesk.graphql.modules.data.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.graphql.CustomGraphqlException;
import com.ngdesk.graphql.modules.dao.Condition;
import com.ngdesk.graphql.modules.dao.Module;
import com.ngdesk.graphql.modules.dao.ModuleField;
import com.ngdesk.graphql.modules.dao.ModulesService;
import com.ngdesk.graphql.role.layout.dao.RoleService;
import com.ngdesk.repositories.modules.ModulesRepository;
import com.ngdesk.repositories.modules.data.ModuleEntryRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class OneToManyUnmappedCountDataFetcher implements DataFetcher<Integer> {

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
		String moduleId = environment.getArgument("moduleId");
		String fieldId = environment.getArgument("fieldId");
		String search = environment.getArgument("search");
		List<Module> modules = modulesRepository.findAllModules("modules_" + companyId);

		Optional<Module> optionalModule = modules.stream().filter(module -> module.getModuleId().equals(moduleId))
				.findFirst();
		if (optionalModule.isEmpty()) {
			throw new CustomGraphqlException(400, "INVALID_MODULE", null);
		}

		Module module = optionalModule.get();
		List<ModuleField> moduleFields = modulesService.getAllFields(module, modules);
		Optional<ModuleField> optionalField = module.getFields().stream()
				.filter(moduleField -> moduleField.getFieldId().equals(fieldId)).findFirst();
		if (optionalField.isEmpty()) {
			throw new CustomGraphqlException(400, "INVALID_FIELD", null);
		}
		ModuleField moduleField = optionalField.get();
		if (moduleField.getDataType().getDisplay().equalsIgnoreCase("Relationship")
				&& moduleField.getRelationshipType().equalsIgnoreCase("Many To One")) {
			if (search != null && !search.isBlank()) {
				String role = authManager.getUserDetails().getRole();
				Boolean isAdmin = roleService.isSystemAdmin(role);
				Set<String> teamIds = dataService.getAllTeamIds(isAdmin);
				List<String> entryIds = dataService.getIdsFromGlobalSearch(search, module, teamIds);
				if (entryIds == null) {
					entryIds = new ArrayList<String>();
				}
				return entryRepository.getCountForUnmappedEntriesSearch(entryIds,
						modulesService.getCollectionName(module.getName(), companyId), moduleField.getName());
			} else {
				List<Condition> conditions = new ArrayList<Condition>();
				Condition filter = new Condition();
				filter.setCondition(moduleField.getFieldId());
				filter.setOpearator("DOES_NOT_EXIST");
				filter.setRequirementType("All");
				return entryRepository.getOneToManyCountValue(modules, moduleFields, conditions, moduleField.getName(),
						"%OneToManyUnmapped%", modulesService.getCollectionName(module.getName(), companyId));
			}
		} else {
			throw new CustomGraphqlException(400, "INVALID_RELATED_FIELD", null);
		}
	}
}
