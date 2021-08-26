package com.ngdesk.repositories.company;

import java.util.Optional;

import com.ngdesk.graphql.company.dao.Company;

public interface CustomCompanyRepository {

	public Optional<Company> findByCompanySubdomain(String companySubdomain);

}
