package com.ngdesk.repositories;

import com.ngdesk.websocket.companies.dao.Company;

public interface CompaniesRepository extends CustomCompaniesRepository, CustomNgdeskRepository<Company, String> {

}
