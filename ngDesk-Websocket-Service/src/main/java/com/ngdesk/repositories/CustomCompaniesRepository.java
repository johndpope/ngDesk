package com.ngdesk.repositories;

import java.util.Optional;

import com.ngdesk.websocket.companies.dao.Company;

public interface CustomCompaniesRepository {

	public Optional<Company> findCompanyBySubdomain(String subdomain);

	public Optional<Company> findCompanyByUUID(String uuid);

}
