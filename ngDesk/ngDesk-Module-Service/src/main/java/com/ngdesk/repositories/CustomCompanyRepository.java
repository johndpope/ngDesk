package com.ngdesk.repositories;

import java.util.Optional;

import com.ngdesk.module.company.Company;

public interface CustomCompanyRepository {

	public Optional<Company> findCompanyBySubdomain(String subdomain);

}
