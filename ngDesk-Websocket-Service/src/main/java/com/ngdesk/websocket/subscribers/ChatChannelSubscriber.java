package com.ngdesk.websocket.subscribers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.repositories.CompaniesRepository;
import com.ngdesk.websocket.channels.chat.dao.ChatChannelMessage;
import com.ngdesk.websocket.companies.dao.Company;
import com.ngdesk.websocket.dao.WebSocketService;

@Component
public class ChatChannelSubscriber implements MessageListener {

	@Autowired
	WebSocketService webSocketService;

	@Autowired
	CompaniesRepository companiesRepository;

	@Override
	public void onMessage(Message message, byte[] pattern) {

		try {
			ChatChannelMessage notification = new ObjectMapper().readValue(message.toString(),
					ChatChannelMessage.class);
			Optional<Company> optionalCompany = companiesRepository.findById(notification.getCompanyId(), "companies");
			if (optionalCompany.isPresent()) {
				webSocketService.publishChatChannel(optionalCompany.get(), notification);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
