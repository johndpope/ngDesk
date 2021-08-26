package com.ngdesk.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;

import com.ngdesk.report.company.dao.Company;

public class CustomCompanyRepositoryImpl implements CustomCompanyRepository {
	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<List<Company>> findAllCompanies(String collectionName) {
		return Optional.ofNullable(mongoOperations.find(new Query(), Company.class, collectionName));
	}

}
