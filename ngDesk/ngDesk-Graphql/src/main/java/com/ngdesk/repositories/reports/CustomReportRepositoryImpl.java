package com.ngdesk.repositories.reports;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.ngdesk.graphql.reports.dao.Report;

@Repository
public class CustomReportRepositoryImpl implements CustomReportRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<List<Report>> findAllReportInCompany(Pageable pageable, String collectionName) {
		Query query = new Query();
		query.with(pageable);
		return Optional.ofNullable(mongoOperations.find(query, Report.class, collectionName));
	}

	@Override
	public int reportCount(String collectionName) {
		return (int) mongoOperations.count(new Query(), collectionName);
	}

}
