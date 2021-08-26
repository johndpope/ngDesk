package com.ngdesk.repositories.notification;

import org.springframework.stereotype.Repository;

import com.ngdesk.graphql.notification.dao.Notification;
import com.ngdesk.repositories.CustomNgdeskRepository;

@Repository
public interface NotificationRepository extends CustomNotificationRepository ,CustomNgdeskRepository<Notification, String>{


}
