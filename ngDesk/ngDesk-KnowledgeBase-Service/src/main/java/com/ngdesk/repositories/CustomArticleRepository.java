package com.ngdesk.repositories;

import java.util.Optional;

import com.ngdesk.knowledgebase.article.dao.Article;
import com.ngdesk.knowledgebase.article.dao.Attachment;
import com.ngdesk.knowledgebase.article.dao.CommentMessage;

public interface CustomArticleRepository {

	public Optional<Article> findArticleByTitle(String articleTitle, String collectionName);

	public Optional<Article> findArticleDuplicateTitle(String articleTitle, String articleId, String collectionName);

	public Attachment saveAttachment(Attachment newAttachment, String collectionName);

	public Optional<Attachment> findHashById(String hash, String collectionName);
	
	public int getCount(String collectionName);
}
