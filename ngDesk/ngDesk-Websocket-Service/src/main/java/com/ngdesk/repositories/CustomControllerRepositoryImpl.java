package com.ngdesk.repositories;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import com.mongodb.client.model.Updates;

@Repository
public class CustomControllerRepositoryImpl implements CustomControllerRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public void updateControllerLastSeen(String controllerId, String companyId, String collectionName) {
		Assert.notNull(companyId, "The given controller ID must not be null!");
		Assert.notNull(controllerId, "The given company ID must not be null!");
		Assert.notNull(collectionName, "The given collectionName must not be null!");

		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("COMPANY_ID").is(companyId), Criteria.where("_id").is(controllerId));
		Query query = new Query(criteria);

		Update update = new Update();
		update.set("LAST_SEEN", new Date());
		update.set("STATUS", "Online");

		mongoOperations.updateFirst(query, update, collectionName);
	}

	@Override
	public void updateSubAppLastSeen(String controllerId, String applicationName, String companyId,
			String collectionName) {
		Assert.notNull(companyId, "The given controller ID must not be null!");
		Assert.notNull(controllerId, "The given company ID must not be null!");
		Assert.notNull(collectionName, "The given collectionName must not be null!");

		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("COMPANY_ID").is(companyId), Criteria.where("_id").is(controllerId));
		Query query = new Query(criteria);

		Update update = new Update();
		update.set("SUB_APPS.$[subapp].LAST_SEEN", new Date());
		update.set("SUB_APPS.$[subapp].STATUS", "Online");
		update.filterArray(Criteria.where("subapp.NAME").is(applicationName));
		
		mongoOperations.updateFirst(query, update, collectionName);
	}

}
