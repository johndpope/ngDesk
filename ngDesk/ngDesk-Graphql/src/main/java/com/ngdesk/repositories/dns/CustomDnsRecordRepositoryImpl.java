package com.ngdesk.repositories.dns;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

public class CustomDnsRecordRepositoryImpl implements CustomDnsRecordRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<Map<String, Object>> findDNSRecordByCname(String cname, String collectionName) {
		Criteria criteria = new Criteria();
		Query query = new Query(Criteria.where("CNAME").is(cname));
		return Optional.ofNullable(mongoOperations.findOne(query, Map.class, collectionName));
	}
}
