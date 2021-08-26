package com.ngdesk.repositories;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

public class CustomModuleEntryRepositoryImpl implements CustomModuleEntryRepository {

	@Autowired
	private MongoOperations mongoOperations;

	@Override
	public void updateOCRToMetadata(String dataId, String receipt, String fieldName, String collectionName) {
		Update update = new Update();
		update.addToSet("META_DATA." + fieldName, receipt);
		Criteria criteria = new Criteria();
		criteria.where("_id").is(dataId);
		mongoOperations.updateFirst(new Query(Criteria.where("_id").is(dataId)), update, collectionName);

	}

}