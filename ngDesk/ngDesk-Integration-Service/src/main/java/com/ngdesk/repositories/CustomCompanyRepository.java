package com.ngdesk.repositories;

import java.util.Optional;

import com.ngdesk.integration.company.dao.Company;

public interface CustomCompanyRepository {

	public Optional<Company> getCompanyBySubdomain(String subdomain);

}
