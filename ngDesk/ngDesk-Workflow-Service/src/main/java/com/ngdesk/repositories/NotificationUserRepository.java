package com.ngdesk.repositories;

import org.springframework.stereotype.Repository;

import com.ngdesk.workflow.notification.dao.NotificationUser;

@Repository
public interface NotificationUserRepository extends CustomNgdeskRepository<NotificationUser, String> {

}
