package com.ngdesk.repositories;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import com.ngdesk.commons.models.Dashboard;
import com.ngdesk.knowledgebase.categories.dao.Category;

@Repository
public class CustomCategoryRepositoryImpl implements CustomCategoryRepository {
	@Autowired
	MongoOperations mongoOperations;

	@Override
	public int getCount(String collectionName) {
		Assert.notNull(collectionName, "The given collectionName must not be null!");
		Query query = new Query();
		return (int) mongoOperations.count(query, collectionName);
	}

	@Override
	public Optional<Category> validateDuplicateCategory(String name, String collectionName) {
		Query query = new Query(Criteria.where("name").is(name));
		return Optional.ofNullable(mongoOperations.findOne(query, Category.class, collectionName));
	}
}
