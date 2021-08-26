package com.ngdesk.nodes;

import java.util.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.ngdesk.Global;
import com.ngdesk.services.NotificationMessage;

@Component
public class Say extends Node {
	private static final Logger logger = LoggerFactory.getLogger(Say.class);

	@Autowired
	private SimpMessagingTemplate template;

	@Autowired
	Global global;

	@Override
	public Map<String, Object> executeNode(Document node, Map<String, Object> inputMessage) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		logger.trace("Enter Say.executeNode()");
		try {

			Document values = (Document) node.get("VALUES");
			String message = (String) values.get("MESSAGE");
			ArrayList<Document> connections = (ArrayList<Document>) node.get("CONNECTIONS_TO");

			NotificationMessage notificationMessage = new NotificationMessage();
			notificationMessage.setMessageType("Say");

			String messageToBeSent = global.getValue(message, inputMessage);

			notificationMessage.setDateCreated(new Timestamp(new Date().getTime()));
			notificationMessage.setMessage(messageToBeSent);

			String sessionUUID = (String) inputMessage.get("SESSION_UUID");
			String sessionPassword = (String) inputMessage.get("SESSION_PASSWORD");
			String topic = "topic/" + sessionUUID + "/" + sessionPassword;
			this.template.convertAndSend(topic, notificationMessage);

			if (connections.size() == 1) {
				Document connection = connections.get(0);
				resultMap.put("NODE_ID", connection.getString("TO_NODE"));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		logger.trace("Exit Say.executeNode()");
		return resultMap;
	}

}
