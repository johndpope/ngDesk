package com.ngdesk.websocket.dao;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.data.dao.PublishDiscussionMessage;

@Component
@RabbitListener(queues = "publish-discussion", concurrency = "5")
public class DiscussionListener {

	@Autowired
	WebSocketService webSocketService;

	@RabbitHandler
	public void onMessage(PublishDiscussionMessage publishMessage) {
		webSocketService.addDiscussionToEntry(publishMessage.getMessage(), publishMessage.getSubdomain(),
				publishMessage.getUserId(), publishMessage.isTrigger());

	}

}
