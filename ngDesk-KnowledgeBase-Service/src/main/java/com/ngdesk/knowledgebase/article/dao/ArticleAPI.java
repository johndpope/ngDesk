package com.ngdesk.knowledgebase.article.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ngdesk.commons.exceptions.NotFoundException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.ArticleRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RefreshScope
public class ArticleAPI {

	@Autowired
	AuthManager authManager;

	@Autowired
	ArticleRepository articleRepository;

	@Autowired
	ArticleService articleService;

	@PostMapping("/article")
	@Operation(summary = "Post Article", description = "Post a single Article")
	public Article postArticle(@Valid @RequestBody Article article) {
		// form collection Name
		String collectionName = "articles_" + authManager.getUserDetails().getCompanyId();
		article = articleService.SetCommentsUUID(article);
		article = articleService.setDefaultValues(article);
		articleRepository.save(article, collectionName);
		if (article.getAttachments() != null) {
			article = articleService.setAttachments(article);
		}
		articleService.postArticleToElastic(article, authManager.getUserDetails().getCompanyId());
		return articleRepository.save(article, collectionName);
	}

	@PutMapping("/article")
	@Operation(summary = "Put Article", description = "update a single Article")
	public Article putArticle(@Valid @RequestBody Article article) {
		// form collection Name
		String collectionName = "articles_" + authManager.getUserDetails().getCompanyId();

		// Check if article id exist
		Optional<Article> articleOptional = articleRepository.findById(article.getArticleId(), collectionName);
		if (articleOptional.isEmpty()) {
			String[] vars = { "ARTICLE" };
			throw new NotFoundException("DAO_NOT_FOUND", vars);
		}
		// use old article to keep the memory of everything
		Article oldArticle = articleOptional.get();
		article = articleService.setDefaultValuesForUpdate(article, oldArticle);
		article = articleService.SetCommentsPut(article, oldArticle);
		if (article.getAttachments() != null) {
			article = articleService.setAttachments(article);
		}
		articleService.putArticleToElastic(article, authManager.getUserDetails().getCompanyId());
		return articleRepository.save(article, collectionName);
	}

	@DeleteMapping("/article/{articleId}")
	@Operation(summary = "Delete Article", description = "Delete a single Article")
	public void deleteArticle(
			@Parameter(description = "Article ID", required = true) @PathVariable("articleId") String articleId) {
		String collectionName = "articles_" + authManager.getUserDetails().getCompanyId();
		// Check if article id exist
		Optional<Article> articleOptional = articleRepository.findById(articleId, collectionName);
		if (articleOptional.isEmpty()) {
			String[] vars = { "ARTICLE" };
			throw new NotFoundException("DAO_NOT_FOUND", vars);
		}
		articleService.deleteArticleFromElastic(articleId, authManager.getUserDetails().getCompanyId());
		articleRepository.deleteById(articleId, collectionName);
	}

	@PostMapping("/articles/{articleId}/comments")
	public CommentMessage postComments(@RequestBody CommentMessage commentMessages,
			@PathVariable("articleId") String articleId) {
		List<CommentMessage> messages = new ArrayList<CommentMessage>();
		String collectionName = "articles_" + authManager.getUserDetails().getCompanyId();
		Optional<Article> articleOptional = articleRepository.findById(articleId, collectionName);
		if (articleOptional.isEmpty()) {
			String[] vars = { "ARTICLE" };
			throw new NotFoundException("DAO_NOT_FOUND", vars);
		}
		Article article = articleOptional.get();
		commentMessages.setSender(authManager.getUserDetails().getUserId());
		commentMessages.setMessageId(UUID.randomUUID().toString());
		commentMessages.setDateCreated(new Date());
		messages.add(commentMessages);
		article.setComments(messages);
		articleService.postCommentsToElastic(article, authManager.getUserDetails().getCompanyId());
		articleRepository.saveComments(article.getArticleId(), commentMessages,
				"articles_" + authManager.getUserDetails().getCompanyId());

		return commentMessages;
	}
}