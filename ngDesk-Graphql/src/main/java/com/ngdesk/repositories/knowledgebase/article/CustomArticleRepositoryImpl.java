package com.ngdesk.repositories.knowledgebase.article;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.ngdesk.graphql.knowledgebase.article.dao.Article;

@Repository
public class CustomArticleRepositoryImpl implements CustomArticleRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public List<Article> findAllWithPageableAndTeam(List<String> teams, Pageable pageable, String collectionName) {
		Criteria criteria = new Criteria();
		List<Criteria> orCriterias = new ArrayList<Criteria>();
		for (String team : teams) {
			orCriterias.add(Criteria.where("visibleTo").is(team));
		}
		Criteria[] criteriasArray = new Criteria[orCriterias.size()];
		criteriasArray = orCriterias.toArray(criteriasArray);
		criteria.orOperator(criteriasArray);

		Query query = new Query();
		query.addCriteria(criteria).with(pageable);

		return mongoOperations.find(query, Article.class, collectionName);
	}

	@Override
	public List<Article> findAllWithPageable(Pageable pageable, String collectionName) {
		Query query = new Query();
		query.with(pageable);
		return mongoOperations.find(query, Article.class, collectionName);
	}

	@Override
	public Optional<Article> findByIdWithTeam(String articleId, List<String> teams, String collectionName) {
		Criteria criteria = new Criteria();
		List<Criteria> orCriterias = new ArrayList<Criteria>();
		for (String team : teams) {
			orCriterias.add(Criteria.where("visibleTo").is(team));
		}
		Criteria[] criteriasArray = new Criteria[orCriterias.size()];
		criteriasArray = orCriterias.toArray(criteriasArray);
		criteria.orOperator(criteriasArray);
		criteria.andOperator(Criteria.where("_id").is(articleId), criteria, Criteria.where("publish").is(true));
		Query query = new Query();
		query.addCriteria(criteria);

		return Optional.ofNullable(mongoOperations.findOne(query, Article.class, collectionName));
	}

	@Override
	public Optional<Article> findArticleByPublicTeamId(String articleId, String publicTeamId, String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("visibleTo").is(publicTeamId), Criteria.where("_id").is(articleId),
				Criteria.where("publish").is(true));
		Query query = new Query(criteria);
		return Optional.ofNullable(mongoOperations.findOne(query, Article.class, collectionName));
	}

	@Override
	public List<Article> findAllArticlesByTeam(String publicTeamId, Pageable pageable, String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("visibleTo").is(publicTeamId), Criteria.where("publish").is(true));
		Query query = new Query(criteria);
		query.with(pageable);
		return mongoOperations.find(query, Article.class, collectionName);
	}

	@Override
	public int findArticleCountByPublicTeamId(String teamId, String collectionName) {

		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("visibleTo").is(teamId), Criteria.where("publish").is(true));
		Query query = new Query(criteria);
		return (int) mongoOperations.count(query, collectionName);
	}

	@Override
	public int articlesCount(String collectionName) {
		Query query = new Query();
		return (int) mongoOperations.count(query, collectionName);
	}

	@Override
	public int articlesCountByVisibleTo(List<String> teams, String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("visibleTo").in(teams));
		Query query = new Query(criteria);
		return (int) mongoOperations.count(query, collectionName);
	}

	@Override
	public List<Article> findAllWithPageableAndSearch(Pageable pageable, List<ObjectId> ids, String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("_id").in(ids));
		Query query = new Query(criteria);
		query.with(pageable);
		return mongoOperations.find(query, Article.class, collectionName);

	}

	@Override
	public List<Article> findAllArticlesWithSearch(Pageable pageable, List<ObjectId> ids, String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("_id").in(ids), Criteria.where("publish").is(true));
		Query query = new Query(criteria);
		query.with(pageable);
		return mongoOperations.find(query, Article.class, collectionName);

	}

	@Override
	public int articlesCountBySearch(List<ObjectId> ids, String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("_id").in(ids));
		Query query = new Query(criteria);
		return (int) mongoOperations.count(query, collectionName);
	}

	@Override
	public int findArticleCountBySearch(List<ObjectId> ids, String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("_id").in(ids), Criteria.where("publish").is(true));
		Query query = new Query(criteria);
		return (int) mongoOperations.count(query, collectionName);
	}

	@Override
	public List<Article> findAllWithPageableBySectionId(String sectionId, Pageable pageable, String collectionName) {
		Query query = new Query();
		query.addCriteria(Criteria.where("section").is(sectionId));
		query.with(pageable);
		return mongoOperations.find(query, Article.class, collectionName);
	}

	@Override
	public List<Article> findAllWithPageableAndTeamBySectionId(String sectionId, List<String> teams, Pageable pageable,
			String collectionName) {
		Criteria criteria = new Criteria();
		List<Criteria> orCriterias = new ArrayList<Criteria>();
		for (String team : teams) {
			orCriterias.add(Criteria.where("visibleTo").is(team));
		}
		Criteria[] criteriasArray = new Criteria[orCriterias.size()];
		criteriasArray = orCriterias.toArray(criteriasArray);
		criteria.orOperator(criteriasArray);
		criteria.where("section").is(sectionId);
		Query query = new Query();
		query.addCriteria(criteria).with(pageable);

		return mongoOperations.find(query, Article.class, collectionName);
	}

	@Override
	public List<Article> findAllWithPageableAndTeamBySectionId(String sectionId, String publicTeamId, Pageable pageable,
			String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("visibleTo").is(publicTeamId), Criteria.where("publish").is(true));
		criteria.where("section").is(sectionId);
		Query query = new Query(criteria);

		query.with(pageable);
		return mongoOperations.find(query, Article.class, collectionName);
	}
}
