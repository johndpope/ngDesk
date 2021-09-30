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
import com.ngdesk.websocket.modules.dao.ModuleService;
import com.ngdesk.websocket.notification.dao.NotificationService;
import com.ngdesk.websocket.notification.dao.NotificationOfAgentDetails;

@Component
public class NotificationOfAgentDetailsSubscriber implements MessageListener {

	@Autowired
	CompaniesRepository companiesRepository;

	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	NotificationService notificationService;

	@Autowired
	ModuleService moduleService;

	@Override
	public void onMessage(Message message, byte[] pattern) {
		try {
			NotificationOfAgentDetails notifyAgent = new ObjectMapper().readValue(message.toString(), NotificationOfAgentDetails.class);
			Optional<Company> optionalCompany = companiesRepository.findById(notifyAgent.getCompanyId(), "companies");
			if (optionalCompany.isPresent()) {
				Company company = optionalCompany.get();

				notificationService.publishAgentNotification(company, notifyAgent);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
