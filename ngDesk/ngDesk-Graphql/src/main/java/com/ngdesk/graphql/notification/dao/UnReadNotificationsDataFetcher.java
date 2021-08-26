package com.ngdesk.graphql.notification.dao;

import java.util.List;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.notification.NotificationRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class UnReadNotificationsDataFetcher implements DataFetcher<List<Notification>> {

	@Autowired
	AuthManager authManager;

	@Autowired
	NotificationRepository notificationRepository;

	@Override
	public List<Notification> get(DataFetchingEnvironment environment) {
		String companyId = authManager.getUserDetails().getCompanyId();
		String recipientId = authManager.getUserDetails().getUserId();
		Integer page = environment.getArgument("pageNumber");
		Integer pageSize = environment.getArgument("pageSize");
		String sortBy = environment.getArgument("sortBy");
		String orderBy = environment.getArgument("orderBy");
		Boolean read = environment.getArgument("read");
		if (page == null || page < 0) {
			page = 0;
		}
		if (pageSize == null || pageSize < 0) {
			pageSize = 20;
		}
		Sort sort = null;
		if (sortBy == null) {
			sort = Sort.by("DATE_CREATED");
		} else {
			sort = Sort.by(sortBy);
		}
		if (orderBy == null) {
			sort = sort.descending();
		} else {
			if (orderBy.equalsIgnoreCase("asc")) {
				sort = sort.ascending();
			} else {
				sort = sort.descending();
			}
		}
		if (read == null) {
			read = false;
		}
		Pageable pageable = PageRequest.of(page, pageSize, sort);
		Optional<List<Notification>> optionalNotifications = notificationRepository.findUnreadNotifications(pageable,
				companyId, recipientId, read, "notifications");
		if (optionalNotifications.isPresent()) {
			return optionalNotifications.get();
		}
		return null;
	}
}