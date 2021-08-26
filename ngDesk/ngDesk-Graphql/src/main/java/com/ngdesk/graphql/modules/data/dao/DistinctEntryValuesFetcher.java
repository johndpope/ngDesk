package com.ngdesk.graphql.modules.data.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.graphql.modules.dao.Module;
import com.ngdesk.graphql.modules.dao.ModuleField;
import com.ngdesk.graphql.role.layout.dao.RoleService;
import com.ngdesk.repositories.modules.ModulesRepository;
import com.ngdesk.repositories.modules.data.ModuleEntryRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class DistinctEntryValuesFetcher implements DataFetcher<List<String>> {

	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	AuthManager authManager;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Autowired
	DataService dataService;
	
	@Autowired
	RoleService roleService;

	@Override
	public List<String> get(DataFetchingEnvironment environment) {

		String companyId = authManager.getUserDetails().getCompanyId();
		String moduleId = environment.getArgument("moduleId");
		String fieldId = environment.getArgument("fieldId");
		Integer page = environment.getArgument("pageNumber");
		Integer pageSize = environment.getArgument("pageSize");
		String sortBy = environment.getArgument("sortBy");
		String orderBy = environment.getArgument("orderBy");
		String search = environment.getArgument("search");
		String role = authManager.getUserDetails().getRole();

		Optional<Module> optionalModule = modulesRepository.findById(moduleId, "modules_" + companyId);
		if (optionalModule.isEmpty()) {

			return new ArrayList<String>();
		}

		Module module = optionalModule.get();

		String collectionName = module.getName().replaceAll("\\s", "_") + "_" + companyId;

		ModuleField existingField = module.getFields().stream().filter(field -> field.getFieldId().equals(fieldId))
				.findFirst().orElse(null);

		String fieldName = existingField.getName();

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

		if (search != null && !search.isBlank() && (search.length() > 2)) {
			search = fieldName + "=" + search;
			Boolean isAdmin = roleService.isSystemAdmin(role);
			Set<String> teamIds = dataService.getAllTeamIds(isAdmin);
			List<String> entryIds = dataService.getIdsFromGlobalSearch(search, module, teamIds);
			if (entryIds == null) {
				entryIds = new ArrayList<String>();
			}
			return moduleEntryRepository.findEntriesDistinctValuesWithSearch(entryIds, fieldName, pageable,
					collectionName);
		}

		return moduleEntryRepository.findEntriesDistinctValues(fieldName, pageable, collectionName);

	}
}
