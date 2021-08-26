package com.ngdesk.repositories;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.Assert;

import com.ngdesk.company.dao.Company;
import com.ngdesk.company.uifaillogs.dao.UIFailLog;

public class CustomCompanyRepositoryImpl implements CustomCompanyRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<Company> findByCompanySubdomain(String companySubdomain) {
		Query query = new Query(Criteria.where("COMPANY_SUBDOMAIN").is(companySubdomain));
		return Optional.ofNullable(mongoOperations.findOne(query, Company.class, "companies"));
	}

	@Override
	public Company updateEntry(Company entry, String collectionName) {
		Assert.notNull(entry, "Entry must not be null");
		Assert.notNull(entry.getCompanyId(), "Data ID must not be null");
		Query query = new Query();
		query.addCriteria(Criteria.where("_id").is(entry.getCompanyId().toString()));
		return mongoOperations.findAndReplace(query, entry, collectionName);
	}
	
	@Override
	public UIFailLog saveUIFailLog(UIFailLog entry, String collectionName) {
		
		return mongoOperations.save(entry, collectionName);
	}

	@Override
	public void createCollection(String collectionName) {

		mongoOperations.createCollection(collectionName);
	}

}
