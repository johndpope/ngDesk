package com.ngdesk.repositories.categories;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.ngdesk.graphql.categories.dao.Category;

@Repository
public class CustomCategoryRepositoryImpl implements CustomCategoryRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<List<Category>> findAllCategories(Pageable pageable, String collectionName) {
		Query query = new Query();
		query.with(pageable);
		return Optional.ofNullable(mongoOperations.find(query, Category.class, collectionName));
	}

	@Override
	public Optional<List<Category>> findCategoriesByPublicTeamId(String teamId, Pageable pageable,
			String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("visibleTo").is(teamId), Criteria.where("isDraft").is(false));
		Query query = new Query(criteria);
		query.with(pageable);
		return Optional.ofNullable(mongoOperations.find(query, Category.class, collectionName));
	}

	@Override
	public int categoriesCount(String collectionName) {
		Query query = new Query();
		return (int) mongoOperations.count(query, collectionName);
	}

	@Override
	public int categoriesCountByPublicTeamId(String teamId, String collectionName) {

		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("visibleTo").is(teamId), Criteria.where("isDraft").is(false));
		Query query = new Query(criteria);
		return (int) mongoOperations.count(query, collectionName);
	}

	@Override
	public int categoriesCountByVisibleTo(List<String> teamIds, String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("visibleTo").in(teamIds));
		Query query = new Query(criteria);
		return (int) mongoOperations.count(query, collectionName);
	}

	@Override
	public Optional<List<Category>> findCategoriesByVisibleTo(List<String> teamIds, Pageable pageable,
			String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("visibleTo").in(teamIds));
		Query query = new Query(criteria);
		query.with(pageable);
		return Optional.ofNullable(mongoOperations.find(query, Category.class, collectionName));
	}
}
