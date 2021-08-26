package com.ngdesk.graphql.company.dao;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.company.CompanyRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class CompanyDataFetcher implements DataFetcher<Company> {

	@Autowired
	AuthManager authManager;

	@Autowired
	CompanyRepository companyRepository;

	@Override
	public Company get(DataFetchingEnvironment environment) throws Exception {
		String companySubdomain = authManager.getUserDetails().getCompanySubdomain();
		Optional<Company> company = companyRepository.findByCompanySubdomain(companySubdomain);
		return company.get();

	}
}
