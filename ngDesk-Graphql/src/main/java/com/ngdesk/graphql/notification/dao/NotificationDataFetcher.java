package com.ngdesk.graphql.notification.dao;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.notification.NotificationRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class NotificationDataFetcher implements DataFetcher<Notification> {

	@Autowired
	AuthManager authManager;

	@Autowired
	NotificationRepository notificationRepository;

	@Override
	public Notification get(DataFetchingEnvironment environment) throws Exception {
		String companyId = authManager.getUserDetails().getCompanyId();
		String recipientId = authManager.getUserDetails().getUserId();
        String notificationId = environment.getArgument("notificationId");

		Optional<Notification> optionalNotification = notificationRepository
				.findNotificationByCompanyIdAndDataId(companyId, recipientId, notificationId,"notifications");
		if (optionalNotification.isPresent()) {
			return optionalNotification.get();
		}
		return null;
	}
}