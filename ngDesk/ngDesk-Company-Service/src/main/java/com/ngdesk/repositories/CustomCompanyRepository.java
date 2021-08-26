package com.ngdesk.repositories;

import java.util.Map;
import java.util.Optional;

import com.ngdesk.company.dao.Company;
import com.ngdesk.company.uifaillogs.dao.UIFailLog;

public interface CustomCompanyRepository {

	public Optional<Company> findByCompanySubdomain(String companySubdomain);

	public void createCollection(String collectionName);

	Company updateEntry(Company entry, String collectionName);

	UIFailLog saveUIFailLog(UIFailLog entry, String collectionName);
}
