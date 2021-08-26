package com.ngdesk.repositories.company;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.ngdesk.graphql.company.dao.Company;

@Repository
public class CustomCompanyRepositoryImpl implements CustomCompanyRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<Company> findByCompanySubdomain(String companySubdomain) {
		Query query = new Query(Criteria.where("COMPANY_SUBDOMAIN").is(companySubdomain));
		return Optional.ofNullable(mongoOperations.findOne(query, Company.class, "companies"));
	}


}
