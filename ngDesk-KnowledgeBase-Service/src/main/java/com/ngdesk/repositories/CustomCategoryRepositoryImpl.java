package com.ngdesk.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

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
}
