package com.ngdesk.repositories;

import java.util.Map;
import java.util.Optional;
import com.ngdesk.notifications.dao.Module;
import com.ngdesk.notifications.dao.Notification;

public interface CustomNotificationRepository {

	public Optional<Module> findByModuleId(String recipientId, String collectionName);

	public Optional<Map<String, Object>> findByDataId(String dataId, String collectionName);

	public void markAllNotificationsAsRead(String companyId, String userId, String collectionName);

	public Optional<Notification>  findByIdandRequestorId(String notificationId, String requestorId, String string);
}
