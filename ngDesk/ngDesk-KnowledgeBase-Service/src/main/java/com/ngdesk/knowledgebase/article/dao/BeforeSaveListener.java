package com.ngdesk.knowledgebase.article.dao;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.exceptions.NotFoundException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.knowledgebase.section.dao.Section;
import com.ngdesk.repositories.ArticleRepository;
import com.ngdesk.repositories.ModuleEntryRepository;
import com.ngdesk.repositories.section.SectionRepository;

@Component
public class BeforeSaveListener extends AbstractMongoEventListener<Article> {

	@Autowired
	AuthManager authManager;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;
	
	@Autowired
	SectionRepository sectionRepository;
	
	@Autowired 
	ArticleRepository articleRepository;
	
	String[] languageCodes={ "ar", "de", "el", "en", "es", "fr", "hi", "it", "ms", "pt", "ru", "zh", "no" };
	
	@Override
	public void onBeforeConvert(BeforeConvertEvent<Article> event) {
		Article article = event.getSource();
		ValidateTitleDuplicate(article);
		ValidateAuthor(article);
		ValidateVisibleTo(article);
		ValidateComment(article);
		ValidateSection(article);
		ValidateLanguageSource(article);
	}
	
	private void ValidateTitleDuplicate(Article article) {
		String collectionName="articles_"+authManager.getUserDetails().getCompanyId();
		Optional<Article>articleDuplicate=articleRepository.findArticleDuplicateTitle(article.getTitle(), article.getArticleId(), collectionName);
		if(!articleDuplicate.isEmpty())
		{
			String[] vars= {"ARTICLE","TITLE"};
			throw  new BadRequestException("DAO_VARIABLE_ALREADY_EXISTS",vars);
		}
	}
	
	// Check if Author exist in mongoDB
	private void ValidateAuthor(Article article) {
		String collectionName = "Users_" + authManager.getUserDetails().getCompanyId();
		Optional<Map<String, Object>> authorExist = moduleEntryRepository.findEntryById(article.getAuthor(),
				collectionName);
		if (authorExist.isEmpty()) {
			String[] vars = { "USER" };
			throw new NotFoundException("DAO_NOT_FOUND", vars);
		}
	}

	// validate if the values in visible to are valid Teams in mongoDB
	private void ValidateVisibleTo(Article article) {
		String collectionName = "Teams_" + authManager.getUserDetails().getCompanyId();
		for (String teamId : article.getVisibleTo()) {
			Optional<Map<String, Object>> teamExist = moduleEntryRepository.findEntryById(teamId, collectionName);
			if (teamExist.isEmpty()) {
				String[] vars = { "TEAM" };
				throw new NotFoundException("DAO_NOT_FOUND", vars);
			}
		}
	}

	// Validate if Article is open for comment if not remove them
	// else make check if Comment contains desired UUID
	private void ValidateComment(Article article) {
		String collectionName = "Users_" + authManager.getUserDetails().getCompanyId();
		if (article.isOpenForComments()) {
			if (article.getComments() != null) {
				for (int i = 0; i < article.getComments().size(); i++) {
					String senderId = article.getComments().get(i).getSender();
					Optional<Map<String, Object>> userOptional = moduleEntryRepository.findEntryById(senderId,
							collectionName);
					if (userOptional.isEmpty()) {
						String[] vars = { "USER" };
						throw new NotFoundException("DAO_NOT_FOUND", vars);
					}
				}
			}
		}
	}
	
	private void ValidateSection(Article article) {
		String collectionName="sections_"+authManager.getUserDetails().getCompanyId();
		Optional<Section> sectionOptional=sectionRepository.findById(article.getSection(),collectionName);
		if (sectionOptional.isEmpty()) {
			String[] vars = { "SECTION" };
			throw new NotFoundException("DAO_NOT_FOUND", vars);
		}
	}
	
	private void ValidateLanguageSource(Article article) {
		List<String> languageCode = Arrays.asList(languageCodes);
		if (!languageCode.contains(article.getSourceLanguage())) {
			String[] vars = { "ARTICLE" };
			throw new BadRequestException("SOURCE_LANGUAGE_DOES_NOT_EXIST", vars);
		}
	}
}
