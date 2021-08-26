package com.ngdesk.repositories;

import org.springframework.stereotype.Repository;

import com.ngdesk.report.dao.Report;

@Repository
public interface ReportRepository extends CustomReportRepository, CustomNgdeskRepository<Report, String> {

}
