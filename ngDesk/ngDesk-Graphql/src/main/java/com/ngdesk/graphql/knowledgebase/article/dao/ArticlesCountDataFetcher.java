package com.ngdesk.graphql.knowledgebase.article.dao;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.graphql.role.layout.dao.RoleService;
import com.ngdesk.repositories.knowledgebase.article.ArticleRepository;
import com.ngdesk.repositories.modules.data.ModuleEntryRepository;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class ArticlesCountDataFetcher implements DataFetcher<Integer> {

	@Autowired
	ArticleRepository articleRepository;

	@Autowired
	AuthManager authManager;

	@Autowired
	RoleService roleService;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Autowired
	ArticleService articleService;

	@Override
	public Integer get(DataFetchingEnvironment environment) throws Exception {
		String collectionName = "articles_" + authManager.getUserDetails().getCompanyId();
		String userId = authManager.getUserDetails().getUserId();
		String search = environment.getArgument("search");
		Optional<Map<String, Object>> user = moduleEntryRepository.findById(userId,
				"Users_" + authManager.getUserDetails().getCompanyId());
		List<String> teams = (List<String>) user.get().get("TEAMS");
		boolean isSystemAdmin = roleService.isSystemAdmin(authManager.getUserDetails().getRole());
		if (search != null) {
			List<ObjectId> ids = articleService.getIdsFromElastic(authManager.getUserDetails().getCompanyId(), search,
					teams, isSystemAdmin);
			return articleRepository.articlesCountBySearch(ids, collectionName);
		} else {
			if (isSystemAdmin) {
				return articleRepository.articlesCount(collectionName);
			} else {
				return articleRepository.articlesCountByVisibleTo(teams, collectionName);

			}
		}

	}
}
