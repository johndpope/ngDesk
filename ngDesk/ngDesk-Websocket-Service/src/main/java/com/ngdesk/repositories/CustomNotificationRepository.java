package com.ngdesk.repositories;

import java.util.Map;
import java.util.Optional;

import com.ngdesk.websocket.modules.dao.Module;

public interface CustomNotificationRepository {

	public Optional<Module> findByModuleId(String moduleId, String collectionName);

	public Optional<Map<String, Object>> findByEntryId(String id, String collectionName);

}
