package com.ngdesk.repositories;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.ngdesk.data.dao.DiscussionMessage;
import com.ngdesk.websocket.companies.dao.Phone;

public interface CustomModuleEntryRepository {

	public Optional<Map<String, Object>> findEntryById(String entryId, String collectionName);

	public void addDiscussionToEntry(DiscussionMessage message, String discussionFieldName, String entryId,
			String collectionName);

	public List<Map<String, Object>> findTeamsByIds(List<String> teamIds, String collectionName);

	public List<Map<String, Object>> findAllEntriesExceptGivenId(String fieldName, int value, String collectionName);

	public Optional<Map<String, Object>> findBySessionUuid(String uuid, String collectionName);

	public Optional<Map<String, Object>> findTeamByName(String teamName, String collectionName);

	public Optional<Map<String, Object>> findUserByEmailAddressIncludingDeleted(String emailAddress,
			String collectionName);

	public Optional<Map<String, Object>> findUserByEmailAddress(String emailAddress, String collectionName);

	public Optional<Map<String, Object>> findAccountByName(String accountName, String collectionName);

	public void updateTeamUser(String userId, String teamName, String collectionName);

	public void removeTeamUser(String userId, String teamName, String collectionName);

	public Optional<Map<String, Object>> findAndReplace(String id, Map<String, Object> entry, String collectionName);

	public void setUserPhoneNumberAndDeletedToFalse(String emailAddress, Phone phone, String collectionName);

	public void setUserDeletedToFalse(String emailAddress, String collectionName);

	public Integer findByAgentAndCollectionName(String agentId, String collectionName);

}
