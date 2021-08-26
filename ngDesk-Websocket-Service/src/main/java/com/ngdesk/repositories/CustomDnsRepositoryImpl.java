package com.ngdesk.repositories;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import com.ngdesk.websocket.companies.dao.DnsRecord;

@Repository
public class CustomDnsRepositoryImpl implements CustomDnsRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<DnsRecord> getDnsRecordByCname(String cname) {
		Assert.notNull(cname, "The given cname must not be null!");
		return Optional.ofNullable(
				mongoOperations.findOne(new Query(Criteria.where("CNAME").is(cname)), DnsRecord.class, "dns_records"));
	}

}
