package com.ngdesk.websocket.subscribers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.repositories.CompaniesRepository;
import com.ngdesk.repositories.ModulesRepository;
import com.ngdesk.websocket.companies.dao.Company;
import com.ngdesk.websocket.modules.dao.Module;
import com.ngdesk.websocket.notification.dao.Notification;
import com.ngdesk.websocket.notification.dao.NotificationService;

@Component
public class NotificationSubscriber implements MessageListener {

	@Autowired
	CompaniesRepository companiesRepository;

	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	NotificationService notificationService;

	@Override
	public void onMessage(Message message, byte[] pattern) {
		try {
			Notification notification = new ObjectMapper().readValue(message.toString(), Notification.class);

			Optional<Company> optionalCompany = companiesRepository.findById(notification.getCompanyId(), "companies");
			if (optionalCompany.isPresent()) {
				Company company = optionalCompany.get();

				Optional<Module> optionalModule = modulesRepository.findById(notification.getModuleId(),
						"modules_" + company.getId());

				if (optionalModule.isPresent()) {
					notificationService.addNotification(notification);
					notificationService.publishNotification(company, notification);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
