package com.ngdesk.repositories;

import java.util.Optional;

import org.apache.http.util.Asserts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.ngdesk.commons.models.Dashboard;

@Repository
public class CustomDashboardRepositoryImpl implements CustomDashboardRepository {

	@Autowired
	private MongoOperations mongoOperations;

	@Override
	public Optional<Dashboard> findDashboardByName(String name, String collectionName) {
		Asserts.notNull(name, "Dashboard Name must not be null");
		Asserts.notNull(collectionName, "Collection Name must not be null");
		Query query = new Query(Criteria.where("name").is(name));
		return Optional.ofNullable(mongoOperations.findOne(query, Dashboard.class, collectionName));
	}

	@Override
	public Optional<Dashboard> findOtherDashboardsWithDuplicateName(String name, String dashboardId,
			String collectionName) {
		Asserts.notNull(name, "Personally Identifiable Information Name must not be null");
		Asserts.notNull(collectionName, "Collection Name must not be null");
		Query query = new Query();
		query.addCriteria(
				new Criteria().andOperator(Criteria.where("_id").ne(dashboardId), Criteria.where("name").is(name)));
		return Optional.ofNullable(mongoOperations.findOne(query, Dashboard.class, collectionName));
	}

}
