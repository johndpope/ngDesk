package com.ngdesk.graphql.knowledgebase.article.dao;

import java.util.Map;
import java.util.Optional;

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
public class ArticleNoAuthDataFetcher implements DataFetcher<Article> {

	@Autowired
	ArticleRepository articleRepository;

	@Autowired
	CompanyRepository companyRepository;

	@Autowired
	SessionManager sessionManager;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Override
	public Article get(DataFetchingEnvironment environment) throws Exception {
		String subdomain = (String) sessionManager.getSessionInfo().get("subdomain");
		Company company = companyRepository.findByCompanySubdomain(subdomain).orElse(null);

		if (company == null) {
			throw new BadRequestException("INVALID_COMPANY", null);
		}

		String companyId = company.getCompanyId();
		String articleId = environment.getArgument("articleId");
		Optional<Map<String, Object>> optionalTeamId = moduleEntryRepository.getPublicTeams("Teams_" + companyId);
		String publicTeamId = "";
		if (optionalTeamId.isPresent()) {
			publicTeamId = optionalTeamId.get().get("_id").toString();
		}
		Optional<Article> optionalArticleId = articleRepository.findArticleByPublicTeamId(articleId, publicTeamId,
				"articles_" + companyId);
		if (optionalArticleId.isEmpty()) {
			return null;
		}
		return optionalArticleId.get();
	}

}
