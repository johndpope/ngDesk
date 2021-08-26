package com.ngdesk.graphql.knowledgebase.article.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.graphql.role.layout.dao.RoleService;
import com.ngdesk.repositories.knowledgebase.article.ArticleRepository;
import com.ngdesk.repositories.modules.data.ModuleEntryRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class ArticleDataFetcher implements DataFetcher<Article> {

	@Autowired
	AuthManager authManager;

	@Autowired
	ArticleRepository articleRepository;

	@Autowired
	RoleService roleService;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Override
	public Article get(DataFetchingEnvironment environment) throws Exception {
		String articleId = environment.getArgument("articleId");
		String collectionName = "articles_" + authManager.getUserDetails().getCompanyId();
		String userId = authManager.getUserDetails().getUserId();
		Optional<Map<String, Object>> user = moduleEntryRepository.findById(userId, "Users_" + authManager.getUserDetails().getCompanyId());
		List<String> teams = (List<String>) user.get().get("TEAMS");
		if (roleService.isSystemAdmin(authManager.getUserDetails().getRole())) {
			return articleRepository.findById(articleId, collectionName).get();
		} else {
			return articleRepository.findByIdWithTeam(articleId, teams, collectionName).get();
		}
	}

}
