package com.ngdesk.company.knowledgebase.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.repositories.KnowledgeBaseRepository;
import com.ngdesk.repositories.KnowledgeBaseTemplateRepository;

@Component
public class DefaultKnowledgeBaseService {

	@Autowired
	KnowledgeBaseTemplateRepository knowledgeBaseTemplateRepository;

	@Autowired
	KnowledgeBaseRepository knowledgeBaseRepository;

	@Autowired
	ArticleService articleService;

	public void postKnowledgeBase(String companyId) {
		// TODO: CONSTRUCT A CLASS FOR CATEGORY, SECTION, ARTICLE

		postCategories(companyId);
		postSections(companyId);
		postArticles(companyId);
	}

	public void postCategories(String companyId) {
		Optional<List<Map<String, Object>>> categories = knowledgeBaseTemplateRepository
				.findAllTemplates("category_templates");
		if (!categories.isEmpty()) {
			knowledgeBaseRepository.saveAll(categories.get(), "categories_" + companyId);
		}
	}

	public void postSections(String companyId) {
		Optional<List<Map<String, Object>>> sections = knowledgeBaseTemplateRepository
				.findAllTemplates("section_templates");
		if (!sections.isEmpty()) {
			knowledgeBaseRepository.saveAll(sections.get(), "sections_" + companyId);
		}
	}

	public void postArticles(String companyId) {
		Optional<List<Map<String, Object>>> articles = knowledgeBaseTemplateRepository
				.findAllTemplates("article_templates");
		if (!articles.isEmpty()) {
			knowledgeBaseRepository.saveAll(articles.get(), "articles_" + companyId);
			articleService.insertArticlesToElastic(articles.get(), companyId);
		}
	}

}
