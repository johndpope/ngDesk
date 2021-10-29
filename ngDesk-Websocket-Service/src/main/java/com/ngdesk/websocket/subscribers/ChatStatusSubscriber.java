package com.ngdesk.websocket.subscribers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.repositories.CompaniesRepository;
import com.ngdesk.websocket.channels.chat.dao.ChatStatusMessage;
import com.ngdesk.websocket.companies.dao.Company;
import com.ngdesk.websocket.dao.WebSocketService;

@Component
public class ChatStatusSubscriber implements MessageListener {

	@Autowired
	WebSocketService webSocketService;

	@Autowired
	CompaniesRepository companiesRepository;

	@Override
	public void onMessage(Message message, byte[] pattern) {
		try {
			ChatStatusMessage chatStatusMessage = new ObjectMapper().readValue(message.toString(),
					ChatStatusMessage.class);
			Optional<Company> optionalCompany = companiesRepository.findById(chatStatusMessage.getCompanyId(),
					"companies");
			if (optionalCompany.isPresent()) {
				webSocketService.publishChatStatus(optionalCompany.get(), chatStatusMessage);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
