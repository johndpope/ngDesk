package com.ngdesk.graphql.reports.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.reports.ReportRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class ReportCountDataFetcher implements DataFetcher<Integer> {

	@Autowired
	AuthManager authManager;

	@Autowired
	ReportRepository reportRepository;

	@Override
	public Integer get(DataFetchingEnvironment environment) {
		String companyId = authManager.getUserDetails().getCompanyId();
		return reportRepository.reportCount("reports_" + companyId);
	}
}