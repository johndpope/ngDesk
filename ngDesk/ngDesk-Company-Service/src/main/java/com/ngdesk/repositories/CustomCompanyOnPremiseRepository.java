package com.ngdesk.repositories;

import java.util.Optional;

import com.ngdesk.company.onpremise.dao.CompanyOnPremise;

public interface CustomCompanyOnPremiseRepository {
	
	public Optional<CompanyOnPremise> findCompanyByLicenseKey(String licenseKey, String collectionName);
}
