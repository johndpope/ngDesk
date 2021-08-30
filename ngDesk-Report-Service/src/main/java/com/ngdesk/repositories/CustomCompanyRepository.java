package com.ngdesk.repositories;

import java.util.List;

import com.ngdesk.report.company.dao.Company;

public interface CustomCompanyRepository {

	public List<Company> findAllCompanies(String collectionName);

}
