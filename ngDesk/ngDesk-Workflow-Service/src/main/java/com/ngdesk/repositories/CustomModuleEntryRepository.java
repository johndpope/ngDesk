package com.ngdesk.repositories;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.ngdesk.data.dao.DiscussionMessage;

public interface CustomModuleEntryRepository {

	public Optional<Map<String, Object>> findEntryById(String entryId, String collectionName);

	public List<Map<String, Object>> findEntriesByVariable(String variable, String value, String collectionName);

	public Optional<Map<String, Object>> findEntryByVariable(String variable, String value, String collectionName);

	public Map<String, Object> findUserIdByUuid(String variable, String value, String collectionName);

	public List<Map<String, Object>> findEntriesByTeamIds(List<String> teamIds, String collectionName);

	public List<Map<String, Object>> findContactsByUserIds(List<String> entryIds, String collectionName);

	public List<Map<String, Object>> findEntriesByIds(List<String> entryIds, String collectionName);

	public void updateEntry(String dataId, Map<String, Object> metaData, String collectionName);

	public void updateMetadataEvents(String entryId, DiscussionMessage eventsDiscussionMessage, String collectionName);
	
	public Optional<Map<String,Object>> findChatBysessionUUID(String sessionUUID,String collectionName);

	public List<Map<String, Object>> findEntriesByCollectionName(String collectionName);

	public Optional<Map<String, Object>> findEntriesBySessionUuid(String sessionUuid, String collectionName);
}
