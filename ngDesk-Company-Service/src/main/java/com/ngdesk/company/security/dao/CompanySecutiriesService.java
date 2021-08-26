package com.ngdesk.company.security.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.company.dao.Company;
import com.ngdesk.repositories.CompanySecurityRepository;

@Component
public class CompanySecutiriesService {

	@Autowired
	CompanySecurityRepository companySecurityRepository;

	public void postCompanySecurity(Company company) {
		CompanySecurity security = new CompanySecurity();
		security.setMaxLoginRetries(5);
		security.setEnableSignUps(true);
		security.setCompanyId(company.getCompanyId());
		companySecurityRepository.save(security, "companies_security");
	}
}
