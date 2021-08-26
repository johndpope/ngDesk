package com.ngdesk.graphql.knowledgebase.article.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.managers.SessionManager;
import com.ngdesk.graphql.company.dao.Company;
import com.ngdesk.repositories.company.CompanyRepository;
import com.ngdesk.repositories.knowledgebase.article.ArticleRepository;
import com.ngdesk.repositories.modules.data.ModuleEntryRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class ArticlesNoAuthDataFetcher implements DataFetcher<List<Article>> {

	@Autowired
	ArticleRepository articleRepository;

	@Autowired
	CompanyRepository companyRepository;

	@Autowired
	SessionManager sessionManager;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Autowired
	ArticleService articleService;

	@Override
	public List<Article> get(DataFetchingEnvironment environment) throws Exception {
		String subdomain = (String) sessionManager.getSessionInfo().get("subdomain");
		Company company = companyRepository.findByCompanySubdomain(subdomain).orElse(null);
		System.out.println("****************");
		if (company == null) {
			throw new BadRequestException("INVALID_COMPANY", null);
		}

		String companyId = company.getCompanyId();
		String collectionName = "articles_" + companyId;
		Integer page = environment.getArgument("pageNumber");
		Integer pageSize = environment.getArgument("pageSize");
		String sortBy = environment.getArgument("sortBy");
		String orderBy = environment.getArgument("orderBy");
		String search = environment.getArgument("search");
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
		Optional<Map<String, Object>> optionalTeamId = moduleEntryRepository.getPublicTeams("Teams_" + companyId);
		String publicTeamId = "";
		if (optionalTeamId.isPresent()) {
			publicTeamId = optionalTeamId.get().get("_id").toString();
		}
		List<String> teams = new ArrayList<String>();
		teams.add(publicTeamId);
		if (search != null) {
			List<ObjectId> ids = articleService.getIdsFromElastic(companyId, search, teams, false);
			return articleRepository.findAllArticlesWithSearch(pageable, ids, collectionName);
		} else {
			return articleRepository.findAllArticlesByTeam(publicTeamId, pageable, collectionName);
		}

	}

}
