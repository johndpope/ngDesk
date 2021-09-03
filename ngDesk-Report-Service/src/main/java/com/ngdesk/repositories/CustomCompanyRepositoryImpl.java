package com.ngdesk.repositories;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;

import com.ngdesk.report.company.dao.Company;

public class CustomCompanyRepositoryImpl implements CustomCompanyRepository {
	@Autowired
	MongoOperations mongoOperations;

	@Override
	public List<Company> findAllCompanies(String collectionName) {
		Query query = new Query();
		query.fields().include("COMPANY_SUBDOMAIN");
		return mongoOperations.find(new Query(), Company.class, collectionName);
	}

}
