package com.ngdesk.graphql.csvimport.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.graphql.role.layout.dao.RoleService;
import com.ngdesk.repositories.csvimport.CsvImportRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class CsvImportsDataFetcher implements DataFetcher<List<CsvImport>> {

	@Autowired
	AuthManager authManager;

	@Autowired
	CsvImportRepository csvImportRepository;

	@Autowired
	RoleService roleService;

	@Override
	public List<CsvImport> get(DataFetchingEnvironment environment) throws Exception {

		Integer page = environment.getArgument("pageNumber");
		Integer pageSize = environment.getArgument("pageSize");
		String sortBy = environment.getArgument("sortBy");
		String orderBy = environment.getArgument("orderBy");

		if (page == null || page < 0) {
			page = 0;
		}

		if (pageSize == null || pageSize < 0) {
			pageSize = 20;
		}
		Sort sort = null;
		if (sortBy == null) {
			sort = Sort.by("DATE_CREATED");
		} else {
			sort = Sort.by(sortBy);
		}
		if (orderBy == null) {
			sort = sort.descending();
		} else {
			if (orderBy.equalsIgnoreCase("asc")) {
				sort = sort.ascending();
			} else {
				sort = sort.descending();
			}
		}
		Pageable pageable = PageRequest.of(page, pageSize, sort);
		Optional<List<CsvImport>> optionalCsvImports = csvImportRepository.findAllCsvImports(pageable,
				authManager.getUserDetails().getCompanyId(), "csv_import");

		if (optionalCsvImports.isPresent() && roleService.isSystemAdmin(authManager.getUserDetails().getRole())) {
			return optionalCsvImports.get();
		}
		return null;
	}

}
