package com.ngdesk.graphql.knowledgebase.article.dao;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.graphql.role.layout.dao.RoleService;
import com.ngdesk.repositories.knowledgebase.article.ArticleRepository;
import com.ngdesk.repositories.modules.data.ModuleEntryRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class ArticlesBySectionIdDataFetcher implements DataFetcher<List<Article>> {

	@Autowired
	AuthManager authManager;

	@Autowired
	ArticleRepository articleRepository;

	@Autowired
	RoleService roleService;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Autowired
	ArticleService articleService;

	@Override
	public List<Article> get(DataFetchingEnvironment environment) throws Exception {
		String companyId = authManager.getUserDetails().getCompanyId();
		String collectionName = "articles_" + companyId;
		Integer page = environment.getArgument("pageNumber");
		Integer pageSize = environment.getArgument("pageSize");
		String sortBy = environment.getArgument("sortBy");
		String orderBy = environment.getArgument("orderBy");
		String sectionId = environment.getArgument("sectionId");
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
		String userId = authManager.getUserDetails().getUserId();
		Optional<Map<String, Object>> user = moduleEntryRepository.findById(userId, "Users_" + companyId);
		List<String> teams = (List<String>) user.get().get("TEAMS");
		boolean isSystemAdmin = roleService.isSystemAdmin(authManager.getUserDetails().getRole());

		if (isSystemAdmin) {
			return articleRepository.findAllWithPageableBySectionId(sectionId, pageable, collectionName);
		} else {
			return articleRepository.findAllWithPageableAndTeamBySectionId(sectionId, teams, pageable, collectionName);
		}

	}

}
