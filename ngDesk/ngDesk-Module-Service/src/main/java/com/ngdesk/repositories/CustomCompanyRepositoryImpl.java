package com.ngdesk.repositories;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import com.ngdesk.module.company.Company;

@Repository
public class CustomCompanyRepositoryImpl implements CustomCompanyRepository {
	@Autowired
	private MongoOperations mongoOperations;

	@Override
	public Optional<Company> findCompanyBySubdomain(String subdomain) {
		Assert.notNull(subdomain, "The given subdomain must not be null!");
		return Optional.ofNullable(mongoOperations.findOne(new Query(Criteria.where("COMPANY_SUBDOMAIN").is(subdomain)),
				Company.class, "companies"));
	}

}
