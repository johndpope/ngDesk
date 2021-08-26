package com.ngdesk.repositories;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.ngdesk.module.slas.dao.DiscussionMessage;

public interface CustomModuleEntryRepository {

	public Optional<Map<String, Object>> findEntryById(String entryId, String collectionName);

	public Optional<Map<String, Object>> findEntryByName(String fieldName, String fieldValue, String collectionName);

	public Optional<List<Map>> findAllEntriesByCollectionName(String collectionName);

	public Optional<Map<String, Object>> updateEntry(Map<String, Object> entry, String collectionName);

	public Optional<Map<String, Object>> findTeamByName(String teamName, String collectionName);

	public void updateMetadataEvents(String dataId, DiscussionMessage eventsDiscussionMessage, String collectionName);

	public void findEntryAndUpdateUnset(String collectionName, String dataId, String fieldName);

	public void findEntryAndUpdate(String collectionName, String dataId, String fieldName, Date currentTimestamp);

	public void addDiscussionMessage(String entryId, String fieldName, DiscussionMessage discussionMessage,
			String collectionName);

}
