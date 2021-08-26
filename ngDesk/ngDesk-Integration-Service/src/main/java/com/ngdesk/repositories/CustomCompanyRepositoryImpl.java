package com.ngdesk.repositories;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.ngdesk.integration.company.dao.Company;

public class CustomCompanyRepositoryImpl implements CustomCompanyRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<Company> getCompanyBySubdomain(String subdomain) {

		Query query = new Query(Criteria.where("COMPANY_SUBDOMAIN").is(subdomain));

		return Optional.ofNullable(mongoOperations.findOne(query, Company.class, "companies"));
	}
}
