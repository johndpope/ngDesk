package com.ngdesk.repositories.notification;

import java.util.List;

import java.util.Optional;

import org.springframework.data.domain.Pageable;
import com.ngdesk.graphql.notification.dao.Notification;

public interface CustomNotificationRepository {

	public Optional<Notification> findNotificationByCompanyIdAndDataId(String companyId, String recipientId,
			String notificationId, String collectionName);

	public Optional<List<Notification>> findAllNotificationsInCompany(Pageable pageable, String companyId,String userId,
			String collectionName);

	public Optional<List<Notification>> findUnreadNotifications(Pageable pageable, String companyId, String recipientId,
			Boolean read, String collectionName);

	public int findUnreadNotificationsCount(String companyId, String recipientId, String collectionName);

}
