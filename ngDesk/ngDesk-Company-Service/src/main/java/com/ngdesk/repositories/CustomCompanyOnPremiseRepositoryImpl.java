package com.ngdesk.repositories;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import com.ngdesk.company.onpremise.dao.CompanyOnPremise;

@Repository
public class CustomCompanyOnPremiseRepositoryImpl implements CustomCompanyOnPremiseRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<CompanyOnPremise> findCompanyByLicenseKey(String licenseKey, String collectionName) {
		Assert.notNull(licenseKey, "license key must not be null");
		Assert.notNull(collectionName, "collection name must not be null");
		Query query = new Query(Criteria.where("licenseKey").is(licenseKey));

		return Optional.ofNullable(mongoOperations.findOne(query, CompanyOnPremise.class, collectionName));
	}

}
