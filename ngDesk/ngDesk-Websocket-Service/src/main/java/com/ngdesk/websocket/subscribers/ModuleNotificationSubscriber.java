package com.ngdesk.websocket.subscribers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.repositories.CompaniesRepository;
import com.ngdesk.repositories.ModulesRepository;
import com.ngdesk.websocket.companies.dao.Company;
import com.ngdesk.websocket.dao.NotificationMessage;
import com.ngdesk.websocket.dao.WebSocketService;
import com.ngdesk.websocket.modules.dao.Module;
import com.ngdesk.websocket.modules.dao.ModuleService;

@Component
public class ModuleNotificationSubscriber implements MessageListener {
	
	@Autowired
	CompaniesRepository companiesRepository;
	
	@Autowired
	ModulesRepository modulesRepository;
	
	@Autowired
	WebSocketService webSocketService;
	
	@Autowired
	ModuleService moduleService;
	
	@Override
	public void onMessage(Message message, byte[] pattern) {
		try {
			NotificationMessage notification = new ObjectMapper().readValue(message.toString(), NotificationMessage.class);
			Optional<Company> optionalCompany = companiesRepository.findById(notification.getCompanyId(),
					"companies");
			if (optionalCompany.isPresent()) {
				Company company = optionalCompany.get();

				Optional<Module> optionalModule = modulesRepository.findById(notification.getModuleId(),
						"modules_" + company.getId());

				if (optionalModule.isPresent()) {
					webSocketService.notifyUsersToUpdateInModule(company, notification);
				}
			}
			
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}


}
