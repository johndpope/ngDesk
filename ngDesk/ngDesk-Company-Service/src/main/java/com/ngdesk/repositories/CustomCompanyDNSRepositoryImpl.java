package com.ngdesk.repositories;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.Assert;

public class CustomCompanyDNSRepositoryImpl implements CustomCompanyDNSRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<Map<String, Object>> findByCompanyId(String comapnyId, String collectionName) {
		Assert.notNull(comapnyId, "Company id must not be null");
		Assert.notNull(collectionName, "collection name must not be null");

		Query query = new Query(Criteria.where("COMPANY_ID").is(comapnyId));

		return Optional.ofNullable(mongoOperations.findOne(query, Map.class, collectionName));
	}

	@Override
	public void updateDnsRecord(Map<String, Object> update, String collectionName) {
		String id = update.remove("_id").toString();

		Query query = new Query(Criteria.where("_id").is(id));

		mongoOperations.findAndReplace(query, update, collectionName);
	}

}
