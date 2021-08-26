package com.ngdesk.repositories;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Pageable;

import com.ngdesk.report.dao.Filter;
import com.ngdesk.report.dao.Report;
import com.ngdesk.report.module.dao.Module;
import com.ngdesk.report.module.dao.ModuleField;

public interface CustomModuleEntryRepository {

	public Optional<List<Map<String, Object>>> findAllEntries(String collectionName);

	public int reportCount(List<Module> modules, List<Filter> filters, List<ModuleField> fields, String collectionName,
			Report report);

	public Optional<List<Map<String, Object>>> findAllTeamsOfCurrentUser(String companyId);

	public Optional<List<Map<String, Object>>> findAllTeamsOfGivenUser(String userId, String companyId);

	public List<Map<String, Object>> findEntriesByVariable(List<Module> modules, List<ModuleField> allFields,
			List<Filter> filters, Pageable pageable, String collectionName, Report report, Set<String> teamIds);

	public List<Map<String, Object>> findEntriesByRole(List<Module> modules, List<ModuleField> allFields,
			List<Filter> filters, Pageable pageable, String collectionName, Report report, String role,
			Set<String> teamIds, String companyId);

	public Map<String, Object> findAllEntriesWithGivenValue(Report report, String fieldName, String value,
			String aggregationField, String collectionName);

}
