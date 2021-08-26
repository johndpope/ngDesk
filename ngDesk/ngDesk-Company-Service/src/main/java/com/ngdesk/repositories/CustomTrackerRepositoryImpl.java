package com.ngdesk.repositories;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.ngdesk.company.dao.Tracker;

@Repository
public class CustomTrackerRepositoryImpl implements CustomTrackerRepository {
	
	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Tracker getFirstTracker(String collectionName) {
		Query query=new Query();
		return mongoOperations.findOne(query, Tracker.class, collectionName);
	}

}
