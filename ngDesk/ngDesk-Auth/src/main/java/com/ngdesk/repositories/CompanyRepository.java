package com.ngdesk.repositories;

import org.springframework.stereotype.Repository;

import com.ngdesk.auth.company.dao.Company;

@Repository
public interface CompanyRepository extends CustomNgdeskRepository<Company, String>, CustomCompanyRepository {

}
