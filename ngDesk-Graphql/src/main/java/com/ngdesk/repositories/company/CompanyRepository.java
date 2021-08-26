package com.ngdesk.repositories.company;

import org.springframework.stereotype.Repository;

import com.ngdesk.graphql.company.dao.Company;
import com.ngdesk.repositories.CustomNgdeskRepository;

@Repository
public interface CompanyRepository extends CustomNgdeskRepository<Company, String>, CustomCompanyRepository {

}

