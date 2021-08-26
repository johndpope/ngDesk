package com.ngdesk.graphql.reports.dao;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.reports.ReportRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class ReportDataFetcher implements DataFetcher<Report> {

	@Autowired
	AuthManager authManager;

	@Autowired
	ReportRepository reportRepository;

	@Override
	public Report get(DataFetchingEnvironment environment) {

		String companyId = authManager.getUserDetails().getCompanyId();
		String id = environment.getArgument("id");

		Optional<Report> optionalRoleId = reportRepository.findById(id, "reports_" + companyId);
		if (optionalRoleId.isEmpty()) {
			return null;
		}
		return optionalRoleId.get();

	}
}
