package com.ngdesk.repositories.module.entry;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.aggregation.Aggregation;

import com.ngdesk.data.dao.DiscussionMessage;
import com.ngdesk.data.dao.Sender;
import com.ngdesk.data.modules.dao.Condition;
import com.ngdesk.data.modules.dao.Module;
import com.ngdesk.data.modules.dao.ModuleField;

public interface CustomModuleEntryRepository {

	public Optional<Map<String, Object>> findEntryById(String entryId, String collectionName);

	public Optional<Map<String, Object>> findUniqueEntryForPost(String variable, Object value, String collectionName);

	public Optional<Map<String, Object>> findUniqueEntryForPut(String variable, Object value, String collectionName,
			String dataId);

	public int getCountOfCollectionForAutonumber(String collectionName);

	public int getNextAutoNumber(String fieldName, String collectionName);

	public Optional<Map<String, Object>> updateEntry(Map<String, Object> entry, String collectionName);

	public Optional<List<Map<String, Object>>> findAllEntriesByFieldName(List<String> values, String fieldName,
			String collectionName);

	public long getSystemAdminCount(String systemAdminRoleId, String collectionName);

	public Optional<Map<String, Object>> getAggregationValues(Aggregation aggregation, String collectionName);

	public Optional<Map<String, Object>> findEntryWithAggregation(Module currentModule, String entryId);

	public Page<Map<String, Object>> findAllPaginatedEntriesByIds(Pageable pageable, Module currentModule,
			List<String> entryIds);

	public Page<Map<String, Object>> findAllPaginatedEntries(Pageable pageable, Module currentModule);

	public Page<Map<String, Object>> findAllListLayoutEntries(Pageable pageable, Module currentModule,
			List<Condition> conditions, List<String> fieldIds);

	public List<Map<String, Object>> findTeamsByIds(List<String> teamIds, String collectionName);

	public Optional<Map<String, Object>> findEntryByFieldName(String fieldName, Object value, String collectionName);

	public Optional<Map<String, Object>> findEntryByAlternatePrimaryKeys(Map<String, Object> keyValuePairs,
			String collectionName);

	public Optional<List<Map<String, Object>>> findAllTeamsOfCurrentUser();

	public Page<Map<String, Object>> findAllRelationshipEntries(Pageable pageable, Module relationshipModule,
			ModuleField field, List<String> entryIds);

	public void updateSoftwareNotFound(String collectionName);

	public void updateSoftwareUninstalled(String collectionName);

	// METHODS FOR DELETE CALL ONLY
	public void findAndPull(String fieldName, String dataId, String collectionName);

	public void findAndUnset(String fieldName, String dataId, String collectionName);

	public void findAndUpdateMany(String fieldName, String value, String currentValue, String collectionName);

	public void findAndAddGhost(String fieldName, String userId, String ghostUserId, String collectionName);

	public Optional<List<Map<String, Object>>> findAllTeamIdsToDelete(String fieldName, String deleteUserId,
			List<String> roleNamesToFilter, String collectionName);

	public void deleteTeamEntries(List<String> teamIds, String fieldName, String companyId);

	public void updateSenderInDiscussion(String fieldName, Sender sender, String userUUID, String collectionName);

	public Optional<List<Map<String, Object>>> findChildEntryIds(String parentName, String parentId,
			String collectionName);

	public Optional<Map<String, Object>> findEntryWithAggregationRelationship(Module currentModule, String entryId);

	public Optional<List<Map<String, Object>>> findAllEntries(String collectionName);

	public long findCountOfEntries(String collectionName);

	public List<Map<String, Object>> findAllEntries(String collectionName, Pageable pageable);

	public List<Map<String, Object>> findAllSoftwareModelsForDiscoveryMap(String collectionName, String discoveryMapId,
			String platform, String language);

	public PageImpl<Map<String, Object>> findAllPendingInvitedUsers(Pageable pageable, String contactCollectionName,
			String userCollectionName);

	public void pull(String fieldName, String dataId, String collectionName, String value);

	void push(String fieldName, String dataId, String collectionName, String value);

	public void addMetadataEntry(String entryId, Map<String, Object> metaData, String collectionName);

	public void updateMetadataEvents(String entryId, DiscussionMessage eventsDiscussionMessage, String collectionName);

	public void updateOCRToMetadata(String dataId, List<String> receipts, String fieldName, String collectionName);

	public Optional<List<Map<String, Object>>> findAllTeams(List<String> teamIds, String userId, String companyId);

	public Optional<Map<String, Object>> findEntriesByVariableForRelationship(String collectionName,
			String fieldName,String value ,String id);

}
