package com.ngdesk.repositories;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import com.ngdesk.knowledgebase.article.dao.Article;
import com.ngdesk.knowledgebase.article.dao.Attachment;
import com.ngdesk.knowledgebase.article.dao.CommentMessage;

@Repository
public class CustomArticleRepositoryImpl implements CustomArticleRepository {
	@Autowired
	private MongoOperations mongoOperations;

	@Override
	public Optional<Article> findArticleByTitle(String articleTitle, String collectionName) {
		return Optional.ofNullable(mongoOperations.findOne(new Query(Criteria.where("title").is(articleTitle)),
				Article.class, collectionName));

	}

	@Override
	public Optional<Article> findArticleDuplicateTitle(String articleTitle, String articleId, String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("title").is(articleTitle), Criteria.where("_id").ne(articleId));
		return Optional.ofNullable(mongoOperations.findOne(new Query(criteria), Article.class, collectionName));
	}

	@Override
	public Attachment saveAttachment(Attachment newAttachment, String collectionName) {
		Assert.notNull(newAttachment, "Entity must not be null!");
		return mongoOperations.save(newAttachment, collectionName);
	}

	@Override
	public Optional<Attachment> findHashById(String hash, String collectionName) {

		return Optional.ofNullable(
				mongoOperations.findOne(new Query(Criteria.where("hash").is(hash)), Attachment.class, collectionName));
	}

	@Override
	public int getCount(String collectionName) {
		Assert.notNull(collectionName, "The given collectionName must not be null!");
		Query query = new Query();
		return (int) mongoOperations.count(query, collectionName);
	}

	@Override
	public void saveComments(String articleId, CommentMessage message, String collectionName) {
		Update update = new Update();
		update.addToSet("comments", message);
		mongoOperations.updateFirst(new Query(Criteria.where("_id").is(articleId)), update, collectionName);

	}

}
