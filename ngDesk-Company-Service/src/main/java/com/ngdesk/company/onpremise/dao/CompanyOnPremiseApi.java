package com.ngdesk.company.onpremise.dao;

import java.util.Date;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.exceptions.ForbiddenException;
import com.ngdesk.repositories.CompanyOnPremiseAuditRepository;
import com.ngdesk.repositories.CompanyOnPremiseRepository;

@RestController
public class CompanyOnPremiseApi {

	@Autowired
	private CompanyOnPremiseRepository companyOnPremiseRepository;

	@Autowired
	private CompanyOnPremiseAuditRepository onPremiseAuditRepository;

	@PostMapping("/company/onpremise")
	public CompanyOnPremise postCompany(@RequestBody @Valid CompanyOnPremise companyOnPremise,
			@RequestParam("secret") String secret) {
		if (secret == null || !secret.equals("177134b2-fd36-4b54-a1b3-0fe9272ab17f")) {
			throw new ForbiddenException("FORBIDDEN");
		}
		companyOnPremise.setDateCreated(new Date());
		companyOnPremiseRepository.save(companyOnPremise, "companies_on_premise");
		return companyOnPremise;
	}

	@PostMapping("/company/onpremise/users")
	public SubscriptionStatus updateUsers(@RequestBody @Valid CompanyOnPremiseAudit onPremiseAudit,
			@RequestParam("secret") String secret) {
		if (secret == null || !secret.equals("177134b2-fd36-4b54-a1b3-0fe9272ab17f")) {
			throw new ForbiddenException("FORBIDDEN");
		}
		if (onPremiseAudit.getNoOfUsers() <= 0) {
			throw new BadRequestException("INVALID_USERS", null);
		}

		Optional<CompanyOnPremise> optionalCompany = companyOnPremiseRepository
				.findCompanyByLicenseKey(onPremiseAudit.getLicenseKey(), "companies_on_premise");
		
		if (optionalCompany.isEmpty()) {
			throw new BadRequestException("INVALID_LICENSE_KEY", null);
		}
		
		onPremiseAudit.setDateCreated(new Date());
		onPremiseAuditRepository.save(onPremiseAudit, "companies_on_premise_audit");
		
		return new SubscriptionStatus("valid");
		
	}
}
