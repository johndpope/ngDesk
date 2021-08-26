package com.ngdesk.repositories;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import com.ngdesk.company.dao.CustomLogin;

@Repository
public class CustomLoginRepositoryImpl implements CustomLoginRepository {
	
	@Autowired
	private MongoOperations mongoOperations;
	

	@Override
	public Optional<CustomLogin> findLoginTemplate(String collectionName) {
		Assert.notNull(collectionName, "The given collectionName must not be null!");
		
		Query query = new Query();
		return Optional.ofNullable(mongoOperations.findOne(query, CustomLogin.class, collectionName));
		
	}
	
	

}
