package com.ngdesk.graphql.modules.data.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
public class RelationshipOneToManyDataFetcher implements DataFetcher<List<Map<String, Object>>> {
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
		String role = authManager.getUserDetails().getRole();

		Integer page = environment.getArgument("pageNumber");
		Integer pageSize = environment.getArgument("pageSize");
		String sortBy = environment.getArgument("sortBy");
		String orderBy = environment.getArgument("orderBy");
		String moduleId = environment.getArgument("moduleId");
		String fieldName = environment.getField().getName();
		Map<String, Object> source = environment.getSource();

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

		Optional<Module> optionalModule = modules.stream()
				.filter(module -> module.getModuleId().equalsIgnoreCase(moduleId)).findAny();

		if (optionalModule.isEmpty()) {
			throw new CustomGraphqlException(400, "INVALID_MODULE", null);
		}

		Module module = optionalModule.get();

		ModuleField currentField = module.getFields().stream().filter(field -> field.getName().equals(fieldName))
				.findFirst().orElse(null);

		if (currentField == null) {
			String[] vars = { fieldName };
			throw new CustomGraphqlException(400, "INVALID_FIELD", vars);
		}
		Optional<Module> optionalRelatedModule = modules.stream()
				.filter(relatedModule -> relatedModule.getModuleId().equalsIgnoreCase(currentField.getModule()))
				.findAny();
		if (optionalRelatedModule.isEmpty()) {
			String[] vars = { fieldName };
			throw new CustomGraphqlException(400, "INVALID_MODULE", vars);
		}
		ModuleField relatedField = optionalRelatedModule.get().getFields().stream()
				.filter(field -> field.getFieldId().equals(currentField.getRelationshipField())).findFirst()
				.orElse(null);

		if (relatedField == null) {
			String[] vars = { fieldName };
			throw new CustomGraphqlException(400, "INVALID_FIELD", vars);
		}

		String collectionName = optionalRelatedModule.get().getName().replaceAll("\\s", "_") + "_" + companyId;
		List<Condition> conditions = new ArrayList<Condition>();
		Condition condition = new Condition();
		List<ModuleField> moduleFields = modulesService.getAllFields(optionalRelatedModule.get(), modules);

		condition.setCondition(relatedField.getFieldId());
		condition.setConditionValue(source.get("_id").toString());
		condition.setOpearator("EQUALS_TO");
		condition.setRequirementType("All");
		conditions.add(condition);
		List<Map<String, Object>> entries = entryRepository.findEntriesForLayout(modules, moduleFields, conditions,
				pageable, collectionName, teamIds, module);
		return entries;
	}
}
