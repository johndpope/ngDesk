package com.ngdesk.notifications.dao;

import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.NotificationRepository;

@RestController
public class NotificationAPI {

	@Autowired
	AuthManager authManager;

	@Autowired
	NotificationService notificationService;

	@Autowired

	private NotificationRepository notificationRepository;

	@PutMapping("/notification")

	public Notification updateNotification(@Valid @RequestBody Notification notification)

	{

		return notificationService.updateNotification(notification);
	}

	@PutMapping("/notifications")
	public void markAllNotificationsAsRead() {
		String companyId = authManager.getUserDetails().getCompanyId();
		String userId = authManager.getUserDetails().getUserId();

		notificationRepository.markAllNotificationsAsRead(companyId, userId, "notifications");

	}

	@PostMapping("/notification")
	public Notification addNotification(@Valid @RequestBody Notification notification) {

		return notificationService.addNotification(notification);

	}

}
