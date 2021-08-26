package com.ngdesk.repositories;

import java.util.Optional;

import com.ngdesk.auth.company.dao.Company;

public interface CustomCompanyRepository {

	public Optional<Company> findByCompanySubdomain(String companySubdomain);

	public Company findFirstCompany(String collectionName);

}
