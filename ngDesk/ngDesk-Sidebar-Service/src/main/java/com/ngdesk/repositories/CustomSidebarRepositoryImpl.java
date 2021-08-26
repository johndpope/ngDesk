package com.ngdesk.repositories;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.Assert;

import com.ngdesk.sidebar.dao.CustomSidebar;

public class CustomSidebarRepositoryImpl implements CustomSidebarRepository {

	@Autowired
	private MongoOperations mongoOperations;

	public Optional<CustomSidebar> findCustomSidebarByCompanyId(String collectionName, String companyId) {
		Assert.notNull(companyId, "The company id must not be null!");
		Query query = new Query(Criteria.where("COMPANY_ID").is(companyId));
		return Optional.ofNullable(mongoOperations.findOne(query, CustomSidebar.class, collectionName));
	}

}
