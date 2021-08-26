package com.ngdesk.graphql.modules.data.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.commons.managers.SessionManager;
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
public class OneToManyUnmappedDataFetcher implements DataFetcher<List<Map<String, Object>>> {
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
		String companyId = authManager.getUserDetails().getCompanyId();
		String moduleId = environment.getArgument("moduleId");
		String fieldId = environment.getArgument("fieldId");
		Integer page = environment.getArgument("pageNumber");
		Integer pageSize = environment.getArgument("pageSize");
		String sortBy = environment.getArgument("sortBy");
		String search = environment.getArgument("search");
		String orderBy = environment.getArgument("orderBy");
		List<Module> modules = modulesRepository.findAllModules("modules_" + companyId);

		Optional<Module> optionalModule = modules.stream().filter(module -> module.getModuleId().equals(moduleId))
				.findFirst();
		if (optionalModule.isEmpty()) {
			throw new CustomGraphqlException(400, "INVALID_MODULE", null);
		}

		Module module = optionalModule.get();
		List<ModuleField> moduleFields = modulesService.getAllFields(module, modules);
		sessionManager.getSessionInfo().put("currentModule", module);
		Optional<ModuleField> optionalField = module.getFields().stream()
				.filter(moduleField -> moduleField.getFieldId().equals(fieldId)).findFirst();
		if (optionalField.isEmpty()) {
			throw new CustomGraphqlException(400, "INVALID_FIELD", null);
		}
		ModuleField moduleField = optionalField.get();

		if (moduleField.getDataType().getDisplay().equalsIgnoreCase("Relationship")
				&& moduleField.getRelationshipType().equalsIgnoreCase("Many To One")) {

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
			if (search != null && !search.isBlank()) {
				String role = authManager.getUserDetails().getRole();
				Boolean isAdmin = false;

				if (roleService.isSystemAdmin(role)) {
					isAdmin = true;
				}
				Set<String> teamIds = dataService.getAllTeamIds(isAdmin);
				List<String> entryIds = dataService.getIdsFromGlobalSearch(search, module, teamIds);
				if (entryIds == null) {
					entryIds = new ArrayList<String>();
				}
				return entryRepository.findUnmappedEntriesWithSearch(entryIds, pageable,
						modulesService.getCollectionName(module.getName(), companyId), moduleField.getName());
			} else {
				List<Condition> conditions = new ArrayList<Condition>();
				Condition filter = new Condition();
				filter.setCondition(moduleField.getFieldId());
				filter.setOpearator("DOES_NOT_EXIST");
				filter.setRequirementType("All");
				return entryRepository.findEntriesByVariable(modules, moduleFields, conditions, pageable,
						moduleField.getName(), "%OneToManyUnmapped%",
						modulesService.getCollectionName(module.getName(), companyId));
			}
		} else {
			throw new CustomGraphqlException(400, "INVALID_RELATED_FIELD", null);
		}
	}

}
