package com.ngdesk.repositories.companies;

import java.util.List;
import java.util.Optional;

import com.ngdesk.data.company.dao.Company;

public interface CustomCompaniesRepository {

	public Optional<Company> findCompanyBySubdomain(String subdomain);

	public List<Company> findAllCompanies(String collectionName);
}
