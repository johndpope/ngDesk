package com.ngdesk.notifications.dao;

import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ngdesk.commons.exceptions.NotFoundException;
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

	@PutMapping("/notification/{notificationId}")
	public void markNotificationAsRead(@PathVariable String notificationId) {
		Optional<Notification> optional = notificationRepository.findById(notificationId, "notifications");
		String requestorId = authManager.getUserDetails().getUserId();
		if (optional.isEmpty()) {
			String vars[] = { "NOTIFICATION" };
			throw new NotFoundException("DAO_NOT_FOUND", vars);
		}
		if (requestorId.equals(optional.get().getRecipientId())) {
			notificationRepository.markNotificationAsRead(notificationId, "notifications");
		}
	}

}
