package com.ngdesk.repositories;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.ngdesk.sam.controllers.dao.Log;

@Repository
public class CustomLogsRepositoryImpl implements CustomLogsRepository {

	@Autowired
	MongoOperations mongoOperations;
	
	@Override
	public Page<Log> findAllApplicationLogs(String controllerId, String applicationName, Pageable pageable, String collectionName) {
		
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("APPLICATION").is(applicationName), Criteria.where("CONTROLLER_ID").is(controllerId));
		
		Long count = mongoOperations.count(new Query(criteria), Log.class, collectionName);
		
		List<Log> logs = mongoOperations.find(new Query(criteria).with(pageable), Log.class, collectionName);
		Collections.reverse(logs);
		return new PageImpl<>(logs, pageable, count);
	}
	
}
