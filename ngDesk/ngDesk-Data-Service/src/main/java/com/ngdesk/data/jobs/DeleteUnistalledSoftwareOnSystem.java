package com.ngdesk.data.jobs;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoOperationsExtensionsKt;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ngdesk.data.company.dao.Company;
import com.ngdesk.repositories.companies.CompanyRepository;
import com.ngdesk.repositories.module.entry.ModuleEntryRepository;

//@Component
public class DeleteUnistalledSoftwareOnSystem {

	@Autowired
	CompanyRepository companyRepository;

	@Autowired
	ModuleEntryRepository moduleEntyRepository;

	@Autowired
	MongoOperations mongoOperations;

	@Scheduled(fixedRate = 24 * 60 * 60 * 1000)
	public void updateStatusField() {
		List<Company> companies = companyRepository.findAllCompanies("companies");
		for (Company company : companies) {
			String collectionName = "Software_Installation_" + company.getCompanyId();
			moduleEntyRepository.updateSoftwareNotFound(collectionName);
			moduleEntyRepository.updateSoftwareUninstalled(collectionName);
		}

	}

}
