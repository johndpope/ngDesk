package com.ngdesk.repositories;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.ngdesk.report.dao.Report;

public interface CustomReportRepository {

	public Optional<Report> findReportByName(String name, String collectionName);

	public Optional<Report> findOtherReportWithDuplicateName(String name, String reportId, String collectionName);

	public Optional<Map<String, Object>> findByCollectionNameAndUuid(String uuid, String collectionName);

	public List<Report> findByCollectionName(String collectionName);

}
