package com.ngdesk.repositories;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class CustomDNSRecordRepositoryImpl implements CustomDNSRecordRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<Map<String, Object>> findDNSRecordBySubDomain(String subDomain, String collectionName) {
		Criteria criteria = new Criteria();
		Query query = new Query(Criteria.where("COMPANY_SUBDOMAIN").is(subDomain));
		return Optional.ofNullable(mongoOperations.findOne(query, Map.class, collectionName));
	}
	@Override
	public Optional<Map<String, Object>> findDNSRecordByCname(String cname, String collectionName) {
		Criteria criteria = new Criteria();
		Query query = new Query(Criteria.where("CNAME").is(cname));
		return Optional.ofNullable(mongoOperations.findOne(query, Map.class, collectionName));
	}
	
}
