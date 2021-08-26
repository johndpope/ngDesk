package com.ngdesk.repositories.dashboards;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.ngdesk.commons.models.Dashboard;

@Repository
public class CustomDashboardRepositoryImpl implements CustomDashboardRepository {
	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<Dashboard> findByCompanyIdAndId(String companyId, String id, String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("companyId").is(companyId), Criteria.where("_id").is(id));
		Query query = new Query(criteria);
		return Optional.ofNullable(mongoOperations.findOne(query, Dashboard.class, collectionName));
	}

	@Override
	public Optional<List<Dashboard>> findAllDashboardsInCompany(Pageable pageable, String companyId,
			String collectionName) {
		Query query = new Query(Criteria.where("companyId").is(companyId));
		query.with(pageable);
		return Optional.ofNullable(mongoOperations.find(query, Dashboard.class, collectionName));
	}

	@Override
	public int dashboardCount(String companyId, String collectionName) {
		Query query = new Query(Criteria.where("companyId").is(companyId));
		return (int) mongoOperations.count(query, collectionName);
	}

}
