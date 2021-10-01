package com.ngdesk.repositories;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.report.dao.Report;

@Repository
public class CustomReportRepositoryImpl implements CustomReportRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Autowired
	AuthManager authManager;

	@Override
	public Optional<Report> findReportByName(String name, String collectionName) {
		return Optional.ofNullable(mongoOperations.findOne(new Query(Criteria.where("reportName").is(name)),
				Report.class, collectionName));
	}

	@Override
	public Optional<Report> findOtherReportWithDuplicateName(String name, String reportId, String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("reportName").is(name), Criteria.where("_id").ne(reportId));
		return Optional.ofNullable(mongoOperations.findOne(new Query(criteria), Report.class, collectionName));
	}

	@Override
	public Optional<Map<String, Object>> findByCollectionNameAndUuid(String uuid, String collectionName) {
		Query query = new Query(Criteria.where("UUID").is(uuid));
		return Optional.ofNullable(mongoOperations.findOne(query, Map.class, collectionName));
	}

	@Override
	public List<Report> findByCollectionName(String collectionName) {
		return mongoOperations.find(new Query(), Report.class, collectionName);
	}

}
