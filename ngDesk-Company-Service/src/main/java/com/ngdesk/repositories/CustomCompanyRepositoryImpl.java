package com.ngdesk.repositories;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.Assert;

import com.ngdesk.company.dao.Company;
import com.ngdesk.company.module.dao.Module;
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
	
	@Override
	public Optional<List<Company>> findAllCompaniesWithStartAndEndDate(String collectionName,Date startDate,Date endDate) {
		Assert.notNull(collectionName, "Collection name must not be null");
	
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DATE_CREATED").gt(endDate),Criteria.where("DATE_CREATED").lt(startDate));
		Query query = new Query(criteria);
		return Optional.ofNullable(mongoOperations.find(query, Company.class, collectionName));
	}

}
