package com.ngdesk.graphql.knowledgebase.article.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
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
public class ArticlesNoAuthCountFetcher implements DataFetcher<Integer> {

	@Autowired
	ArticleRepository articleRepository;

	@Autowired
	SessionManager sessionManager;

	@Autowired
	CompanyRepository companyRepository;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Autowired
	ArticleService articleService;

	@Override
	public Integer get(DataFetchingEnvironment environment) throws Exception {
		String subdomain = (String) sessionManager.getSessionInfo().get("subdomain");
		Company company = companyRepository.findByCompanySubdomain(subdomain).orElse(null);

		if (company == null) {
			throw new BadRequestException("INVALID_COMPANY", null);
		}
		System.out.println("counttt");
		String companyId = company.getCompanyId();
		String search = environment.getArgument("search");
		System.out.println("search" + search);
		String collectionName = "articles_" + companyId;
		Optional<Map<String, Object>> optionalTeamId = moduleEntryRepository.getPublicTeams("Teams_" + companyId);
		String publicTeamId = "";
		if (optionalTeamId.isPresent()) {
			publicTeamId = optionalTeamId.get().get("_id").toString();
		}
		List<String> teams = new ArrayList<String>();
		teams.add(publicTeamId);
		if (search != null) {
			List<ObjectId> ids = articleService.getIdsFromElastic(companyId, search, teams, false);
			return articleRepository.findArticleCountBySearch(ids, collectionName);
		} else {
			return articleRepository.findArticleCountByPublicTeamId(publicTeamId, collectionName);
		}
	}

}
