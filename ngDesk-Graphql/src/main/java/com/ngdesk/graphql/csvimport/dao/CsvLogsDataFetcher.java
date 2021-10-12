package com.ngdesk.graphql.csvimport.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
public class CsvLogsDataFetcher implements DataFetcher<List<CsvImportLog>> {

	@Autowired
	AuthManager authManager;

	@Autowired
	CsvImportRepository csvImportRepository;

	@Autowired
	RoleService roleService;

	@Override
	public List<CsvImportLog> get(DataFetchingEnvironment environment) throws Exception {

		String csvImportId = environment.getArgument("csvImportId");
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
		if (sortBy == null || sortBy.isBlank()) {
			sort = Sort.by("lineNumber");
		} else {
			sort = Sort.by(sortBy);
		}
		if (orderBy == null || orderBy.isBlank()) {
			sort = sort.descending();
		} else {
			if (orderBy.equalsIgnoreCase("asc")) {
				sort = sort.ascending();
			} else {
				sort = sort.descending();
			}
		}
		Pageable pageable = PageRequest.of(page, pageSize, sort);

		Optional<CsvImport> optionalCsvImport = csvImportRepository
				.findCsvImportById(authManager.getUserDetails().getCompanyId(), csvImportId, "csv_import");

		if (optionalCsvImport.isPresent() && roleService.isSystemAdmin(authManager.getUserDetails().getRole())) {
			List<CsvImportLog> csvLog = optionalCsvImport.get().getLogs();
			if(csvLog.isEmpty()) {
				return null;
			}
			
			int round = (int) Math.ceil((float)csvLog.size() / pageSize);
			if(page >= round) {
				return null;				
			}
			
			int max = (pageSize * (page + 1) > csvLog.size()) ? csvLog.size() : pageSize * (page + 1);
			Page<CsvImportLog> response = new PageImpl<CsvImportLog>(csvLog.subList(page * pageSize, max), pageable,
					csvLog.size());
			return response.getContent();
		}
		return null;
	}

}
