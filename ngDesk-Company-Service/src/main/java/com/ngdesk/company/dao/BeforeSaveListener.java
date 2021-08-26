package com.ngdesk.company.dao;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.repositories.CompanyRepository;

@Component
public class BeforeSaveListener extends AbstractMongoEventListener<Company> {

	@Autowired
	CompanyRepository companyRepository;

	String[] restrictedSubdomains = new String[] { "api", "www", "test", "tst", "qa", "download", "downloads", "public",
			"private", "stg", "stage", "signup", "developer", "sso", "ngdesk", "mail", "analytics", "dev", "prd",
			"inbound-email", "cdn", "voip" };

	@Override
	public void onBeforeConvert(BeforeConvertEvent<Company> event) {
		Company company = event.getSource();

		checkDuplicateSubdomain(company.getCompanySubdomain());
		restrictedSubdomainCheck(company.getCompanySubdomain());
	}

	private void checkDuplicateSubdomain(String companySubdomain) {
		Optional<Company> optionalCompany = companyRepository.findByCompanySubdomain(companySubdomain);
		if (optionalCompany.isPresent()) {
			String[] vars = { "COMPANY", "SUBDOMAIN" };
			throw new BadRequestException("DAO_VARIABLE_ALREADY_EXISTS", vars);
		}
	}

	private void restrictedSubdomainCheck(String companySubdomain) {
		List<String> restrictedSubdomain = Arrays.asList(restrictedSubdomains);
		if (restrictedSubdomain.contains(companySubdomain)) {
			String[] vars = { "COMPANY", "SUBDOMAIN" };
			throw new BadRequestException("DAO_VARIABLE_ALREADY_EXISTS", vars);
		}
	}
}
