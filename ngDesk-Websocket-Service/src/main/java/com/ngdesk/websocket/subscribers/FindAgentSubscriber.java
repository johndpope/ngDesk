package com.ngdesk.websocket.subscribers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.repositories.CompaniesRepository;
import com.ngdesk.websocket.channels.chat.dao.FindAgent;
import com.ngdesk.websocket.companies.dao.Company;
import com.ngdesk.websocket.dao.WebSocketService;

@Component
public class FindAgentSubscriber implements MessageListener {

	@Autowired
	WebSocketService webSocketService;

	@Autowired
	CompaniesRepository companiesRepository;

	@Override
	public void onMessage(Message message, byte[] pattern) {
		try {
			FindAgent findAgent = new ObjectMapper().readValue(message.toString(), FindAgent.class);
			Optional<Company> optionalCompany = companiesRepository
					.findCompanyBySubdomain(findAgent.getCompanySubdomain());
			if (optionalCompany.isPresent()) {
				webSocketService.publishFindAgentNotification(optionalCompany.get(), findAgent);

			}

		} catch (Exception e) {
			e.printStackTrace();

		}
	}

}
