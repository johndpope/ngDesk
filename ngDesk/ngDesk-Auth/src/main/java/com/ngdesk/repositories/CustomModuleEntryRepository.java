package com.ngdesk.repositories;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.ngdesk.auth.reset.password.ResetPassword;

public interface CustomModuleEntryRepository {

	public Optional<Map<String, Object>> findUserById(String userUuid, String collectionName);

	public Optional<List<Map>> findUsersByRoleId(String roleId, String collectionName);

	public Optional<List<Map>> findAllByCollectionName(String collectionName);

	public long getCountOfPayingUsers(String roleId, String colletionName);

	public Optional<Map<String, Object>> findUserByEmail(String emailAddress, String collectionName);

	public Map<String, Object> findUserByUuid(String collectionName, String userUuid);

	public Optional<Map<String, Object>> updateUserEntry(Map<String, Object> entry, String collectionName);

}
