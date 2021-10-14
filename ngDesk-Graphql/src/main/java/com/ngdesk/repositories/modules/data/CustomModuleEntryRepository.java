package com.ngdesk.repositories.modules.data;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Pageable;

import com.ngdesk.commons.models.OrderBy;
import com.ngdesk.graphql.modules.dao.Condition;
import com.ngdesk.graphql.modules.dao.Module;
import com.ngdesk.graphql.modules.dao.ModuleField;

public interface CustomModuleEntryRepository {

	public List<Map<String, Object>> findEntriesByIds(List<String> ids, String collectionName);

	public Optional<Map<String, Object>> findEntryById(String entryId, String collectionName);

	public Optional<Map<String, Object>> findFirstEntryByFieldValue(String fieldName, String fieldValue,
			String collectionName);

	public int getCount(String collectionName);

	public int getCountForSearch(List<String> entryIds, String collectionName);

	public int getCountForSearchIncludingConditions(List<String> entryIds, List<Condition> conditions,
			List<Module> modules, List<ModuleField> allFields, String collectionName);

	public int getCountForLayouts(List<Module> modules, List<ModuleField> allFields, List<Condition> conditions,
			String collectionName, Set<String> teamIds);

	public List<Map<String, Object>> findEntriesForLayout(List<Module> modules, List<ModuleField> allFields,
			List<Condition> conditions, Pageable pageable, String collectionName, Set<String> teamIds,
			Module currentModule);

	public List<Map<String, Object>> findEntries(Pageable pageable, Set<String> teamIds, String collectionName);

	public List<Map<String, Object>> findAllTeamsOfCurrentUser(String companyId, Boolean isAdmin);

	public List<Map<String, Object>> findEntriesWithSearch(List<String> entryIds, Pageable pageable,
			String collectionName);

	public List<Map<String, Object>> findEntriesWithSearchIncludingConditions(List<String> entryIds,
			List<Condition> conditions, List<Module> modules, List<ModuleField> allFields, Pageable pageable,
			String collectionName);

	public int getCountForWidgets(List<Module> modules, List<ModuleField> allFields, List<Condition> conditions,
			String collectionName, Integer limit, Boolean limitEntries);

	public List<String> findEntriesDistinctValues(String fieldName, Pageable pageable, String collectionName);

	public List<String> findEntriesDistinctValuesWithSearch(List<String> entryIds, String fieldName, Pageable pageable,
			String collectionName);

	public List<Map<String, Object>> findAllEntriesWithGivenValue(String fieldName, String Value,
			String collectionName);

	public int getCountForField(List<Module> modules, List<ModuleField> allFields, String fieldName, Object fieldValue,
			Integer limit, List<Condition> conditions, Boolean limitEntries, String collectionName);

	public List<Map<String, Object>> findEntriesByVariable(List<Module> modules, List<ModuleField> allFields,
			List<Condition> conditions, Pageable pageable, String variable, String value, String collectionName);

	public int getOneToManyCountValue(List<Module> modules, List<ModuleField> allFields, List<Condition> conditions,
			String variable, String value, String collectionName);

	public List<Object> findDistinctEntriyValues(String fieldName, String collectionName);

	public List<Map<String, Object>> findAllByDashboardRelationship(String primaryDisplayField, String collectionName);

	public List<Map<String, Object>> getAllEntriesField(List<Module> modules, List<ModuleField> allFields,
			String fieldName, List<Object> fieldValue, List<Condition> conditions, String collectionName,
			Boolean limitEntries, Integer limit);

	public List<Map<String, Object>> findAllByAggregationField(List<Module> modules, List<ModuleField> allFields,
			String aggFieldName, Integer limit, List<Condition> conditions, Boolean limitEntries,
			List<Object> uniqueValues, String fieldName, String type, OrderBy orderBy, String collectionName);

	public List<Map<String, Object>> findAllByScoreCardAggregationField(List<Module> modules,
			List<ModuleField> allFields, String aggFieldName, Integer limit, List<Condition> conditions,
			Boolean limitEntries, String type, OrderBy orderBy, String collectionName);

	public Optional<List<Map<String, Object>>> findAllTeams(List<String> teamIds, String userId, String companyId);

	public Optional<Map<String, Object>> findAggregationFieldValue(String fieldName, String value,
			String aggregationField, String aggregationType, Set<String> teamIds, List<Condition> conditions,
			List<Module> modules, List<ModuleField> allFields, String collectionName);

	public List<Map<String, Object>> findUnmappedEntriesWithSearch(List<String> entryIds, Pageable pageable,
			String collectionName, String fieldName);

	int getCountForUnmappedEntriesSearch(List<String> entryIds, String collectionName, String fieldName);

	public Optional<Map<String, Object>> getPublicTeams(String collectionName);

	public Optional<List<Map<String, Object>>> findEntriesWithConditions(List<Condition> conditionsList,
			Pageable pageable, String collectionName, List<Module> modules, List<ModuleField> fields,
			Set<String> teamIds);

}
