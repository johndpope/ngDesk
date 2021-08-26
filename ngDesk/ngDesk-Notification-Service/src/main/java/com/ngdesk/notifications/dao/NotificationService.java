package com.ngdesk.notifications.dao;

import java.util.Date;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.exceptions.NotFoundException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.NotificationRepository;

@Service

public class NotificationService {

	@Autowired
	private NotificationRepository notificationRepository;

	@Autowired
	AuthManager auth;

	public void checkValidUserId(String userId, String companyId) {
		Optional<Map<String, Object>> user_Id = notificationRepository.findByDataId(userId, companyId);
		if (user_Id.isEmpty()) {
			throw new BadRequestException("NOT_VALID_USER_ID", null);
		}
	}

	public String checkValidModuleId(String moduleId, String companyId) {
		Optional<Module> module_Id = notificationRepository.findByModuleId(moduleId, companyId);
		if (!module_Id.isEmpty()) {
			String moduleName = module_Id.get().getName();
			return moduleName;
		}
		if (module_Id.isEmpty()) {
			throw new BadRequestException("NOT_VALID_MODULE_ID", null);
		}
		return null;
	}

	public Map<String, Object> checkValidDataId(String dataId, String collectionName) {
		Optional<Map<String, Object>> optionalNotification = notificationRepository.findByDataId(dataId,
				collectionName);
		if (optionalNotification.isEmpty()) {
			String vars[] = { "NOTIFICATION" };

			throw new NotFoundException("DAO_NOT_FOUND", vars);

		}
		return optionalNotification.get();

	}

	public Notification addNotification(Notification notification) {

		notification.setDateCreated(new Date());
		notification.setDateUpdated(new Date());

		checkValidUserId(notification.getRecipientId(), "Users_" + notification.getCompanyId());
		checkValidModuleId(notification.getModuleId(), "modules_" + notification.getCompanyId());

		return notificationRepository.save(notification, "notifications");

	}

	public Notification updateNotification(Notification notification) {

		checkValidUserId(notification.getRecipientId(), "Users_" + notification.getCompanyId());
		String moduleName = checkValidModuleId(notification.getModuleId(), "modules_" + notification.getCompanyId());
		checkValidDataId(notification.getDataId(),
				moduleName.replaceAll("\\s+", "_") + "_" + notification.getCompanyId());
		notification.setDateUpdated(new Date());
		notification.setRead(notification.getRead());
		notification.setDataId(notification.getId());

		return notificationRepository.save(notification, "notifications");

	}

}
