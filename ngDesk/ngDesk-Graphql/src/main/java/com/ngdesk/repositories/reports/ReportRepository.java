package com.ngdesk.repositories.reports;

import org.springframework.stereotype.Repository;

import com.ngdesk.graphql.reports.dao.Report;
import com.ngdesk.repositories.CustomNgdeskRepository;

@Repository
public interface ReportRepository extends CustomReportRepository, CustomNgdeskRepository<Report, String> {

}
