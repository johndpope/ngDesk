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
		String requestorId = authManager.getUserDetails().getUserId();
		Optional<Notification> optionalNotification = notificationRepository.findByIdandRequestorId(notificationId,
				requestorId, "notifications");
		if (optionalNotification.isEmpty()) {
			String vars[] = { "NOTIFICATION" };
			throw new NotFoundException("DAO_NOT_FOUND", vars);
		}
		if (optionalNotification != null) {
			Notification notification = optionalNotification.get();
			notification.setRead(true);
			notificationRepository.save(notification, "notifications");
		}
	}

	@PutMapping("/notificationsByModule")
	public void makeNotificationsRead(@RequestBody Notification notification) {
		notificationService.checkValidUserId(notification.getRecipientId(), "Users_" + notification.getCompanyId());
		String moduleName = notificationService.checkValidModuleId(notification.getModuleId(),
				"modules_" + notification.getCompanyId());
		notificationService.checkValidDataId(notification.getDataId(),
				moduleName.replaceAll("\\s+", "_") + "_" + notification.getCompanyId());
		notificationRepository.markNotificationsRead(notification, "notifications");

	}
}
