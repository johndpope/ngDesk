package com.ngdesk.repositories.csvimport;

import org.springframework.stereotype.Repository;

import com.ngdesk.data.csvimport.dao.CsvImport;
import com.ngdesk.repositories.CustomNgdeskRepository;

@Repository
public interface CsvImportRepository extends CustomCsvImportRepository, CustomNgdeskRepository<CsvImport, String> {

}
