package com.ngdesk.repositories;

import org.springframework.stereotype.Repository;
import com.ngdesk.notifications.dao.Notification;
import com.ngdesk.repositories.CustomNgdeskRepository;

@Repository
public interface NotificationRepository
		extends CustomNotificationRepository, CustomNgdeskRepository<Notification, String> {

	

}
