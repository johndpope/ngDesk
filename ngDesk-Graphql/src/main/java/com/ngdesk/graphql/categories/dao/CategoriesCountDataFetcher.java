package com.ngdesk.graphql.categories.dao;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.categories.CategoryRepository;
import com.ngdesk.repositories.company.CompanyRepository;
import com.ngdesk.repositories.modules.data.ModuleEntryRepository;
import com.ngdesk.repositories.role.RolesRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class CategoriesCountDataFetcher implements DataFetcher<Integer> {
	@Autowired
	CategoryRepository categoryRepository;

	@Autowired
	AuthManager authManager;

	@Autowired
	CompanyRepository companyRepository;

	@Autowired
	RolesRepository rolesRepository;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Override
	public Integer get(DataFetchingEnvironment environment) {

		String systemAdminId = rolesRepository
				.findRoleName("SystemAdmin", "roles_" + authManager.getUserDetails().getCompanyId()).get().getRoleId();
		String roleId = authManager.getUserDetails().getRole();
		String companyId = authManager.getUserDetails().getCompanyId();
		String userId = authManager.getUserDetails().getUserId();
		Optional<Map<String, Object>> user = moduleEntryRepository.findById(userId, "Users_" + companyId);
		List<String> currentUserteamIds = (List<String>) user.get().get("TEAMS");
		if (systemAdminId.equals(roleId)) {

			return categoryRepository.categoriesCount("categories_" + companyId);
		} else {
			return categoryRepository.categoriesCountByVisibleTo(currentUserteamIds, "categories_" + companyId);

		}
	}
}
