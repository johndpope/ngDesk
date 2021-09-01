package com.ngdesk.repositories.csvimport;

import org.springframework.stereotype.Repository;

import com.ngdesk.data.company.dao.Company;
import com.ngdesk.repositories.CustomNgdeskRepository;

@Repository
public interface CsvImportRepository extends CustomCsvImportRepository, CustomNgdeskRepository<Company, String> {

}
