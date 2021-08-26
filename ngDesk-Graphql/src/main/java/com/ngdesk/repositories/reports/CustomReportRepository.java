package com.ngdesk.repositories.reports;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.ngdesk.graphql.reports.dao.Report;

public interface CustomReportRepository {

	public Optional<List<Report>> findAllReportInCompany(Pageable pageable, String collectionName);

	public int reportCount(String collectionName);

}
