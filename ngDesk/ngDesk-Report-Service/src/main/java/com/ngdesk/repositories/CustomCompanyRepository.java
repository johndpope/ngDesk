package com.ngdesk.repositories;

import java.util.List;
import java.util.Optional;

import com.ngdesk.report.company.dao.Company;

public interface CustomCompanyRepository {

	public Optional<List<Company>> findAllCompanies(String collectionName);

}
