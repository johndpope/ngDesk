package com.ngdesk.graphql.categories.dao;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.categories.CategoryRepository;
import com.ngdesk.repositories.modules.data.ModuleEntryRepository;
import com.ngdesk.repositories.role.RolesRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class CategoriesDataFetcher implements DataFetcher<List<Category>> {

	@Autowired
	AuthManager authManager;

	@Autowired
	CategoryRepository categoryRepository;

	@Autowired
	RolesRepository rolesRepository;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Override
	public List<Category> get(DataFetchingEnvironment environment) {

		String companyId = authManager.getUserDetails().getCompanyId();
		Integer page = environment.getArgument("pageNumber");
		Integer pageSize = environment.getArgument("pageSize");
		String sortBy = environment.getArgument("sortBy");
		String orderBy = environment.getArgument("orderBy");

		if (page == null || page < 0) {
			page = 0;
		}

		if (pageSize == null || pageSize < 0) {
			pageSize = 20;
		}
		Sort sort = null;
		if (sortBy == null) {
			sort = Sort.by("dateCreated");
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

		String systemAdminId = rolesRepository
				.findRoleName("SystemAdmin", "roles_" + authManager.getUserDetails().getCompanyId()).get().getRoleId();
		String roleId = authManager.getUserDetails().getRole();
		String userId = authManager.getUserDetails().getUserId();

		Optional<Map<String, Object>> user = moduleEntryRepository.findById(userId, "Users_" + companyId);
		List<String> currentUserteamIds = (List<String>) user.get().get("TEAMS");

		if (systemAdminId.equals(roleId)) {
			Optional<List<Category>> optionalCategories = categoryRepository.findAllCategories(pageable,
					"categories_" + companyId);
			if (optionalCategories.isPresent()) {
				return optionalCategories.get();
			}
		} else {
			Optional<List<Category>> optionalCategories = categoryRepository
					.findCategoriesByVisibleTo(currentUserteamIds, pageable, "categories_" + companyId);
			if (optionalCategories.isPresent()) {
				return optionalCategories.get();
			}
		}
		return null;
	}
}