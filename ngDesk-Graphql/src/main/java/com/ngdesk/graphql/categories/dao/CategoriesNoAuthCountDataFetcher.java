package com.ngdesk.graphql.categories.dao;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.SessionManager;
import com.ngdesk.repositories.categories.CategoryRepository;
import com.ngdesk.repositories.company.CompanyRepository;
import com.ngdesk.repositories.modules.data.ModuleEntryRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class CategoriesNoAuthCountDataFetcher implements DataFetcher<Integer> {
	@Autowired
	CategoryRepository categoryRepository;

	@Autowired
	SessionManager sessionManager;

	@Autowired
	CompanyRepository companyRepository;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Override
	public Integer get(DataFetchingEnvironment environment) {
		
		String companyId =(String) sessionManager.getSessionInfo().get("companyId"); 
		Optional<Map<String, Object>> optionalTeamId = moduleEntryRepository.getPublicTeams("Teams_" + companyId);
		String publicTeamId = "";
		if (optionalTeamId.isPresent()) {
			publicTeamId = optionalTeamId.get().get("_id").toString();
		}
		return categoryRepository.categoriesCountByPublicTeamId(publicTeamId, "categories_" + companyId);
	}
}
