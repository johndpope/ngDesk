package com.ngdesk.graphql.notification.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.notification.NotificationRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class UnReadNotificationsDataFetcherCount implements DataFetcher<Integer> {

	@Autowired
	AuthManager authManager;

	@Autowired
	NotificationRepository notificationRepository;

	@Override
	public Integer get(DataFetchingEnvironment environment) throws Exception {

		String companyId = authManager.getUserDetails().getCompanyId();
		String recipientId = authManager.getUserDetails().getUserId();

		return notificationRepository.findUnreadNotificationsCount(companyId, recipientId, "notifications");

	}

}
