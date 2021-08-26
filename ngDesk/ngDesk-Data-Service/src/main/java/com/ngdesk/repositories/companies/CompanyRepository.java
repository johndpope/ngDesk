package com.ngdesk.repositories.companies;

import org.springframework.stereotype.Repository;

import com.ngdesk.data.company.dao.Company;
import com.ngdesk.repositories.CustomNgdeskRepository;

@Repository
public interface CompanyRepository extends CustomCompaniesRepository, CustomNgdeskRepository<Company, String> {

}
