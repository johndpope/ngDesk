package com.ngdesk.nodes;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.Global;

@Component
public class SayAndGather extends Node {
	private static final Logger logger = LoggerFactory.getLogger(SayAndGather.class);

	@Autowired
	private SimpMessagingTemplate template;

	@Override
	public Map<String, Object> executeNode(Document node, Map<String, Object> inputMessage) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			logger.trace("Enter SayAndGather.executeNode()");

			String message = null;
			if (inputMessage.containsKey("ANSWER")) {
				String answer = (String) inputMessage.get("ANSWER");

				Document values = (Document) node.get("VALUES");
				ArrayList<Document> options = (ArrayList<Document>) values.get("OPTIONS");

				for (Document option : options) {
					String opt = option.getString("OPTION");
					String node_id = option.getString("TO_NODE");
					if (answer.equals(opt)) {
						resultMap.put("NODE_ID", node_id);
						break;
					}
				}
				message = answer;
			} else
				message = "no answer";

			Map<String, Object> nodeMessage = new HashMap<String, Object>();
			nodeMessage.put("MESSAGE", message);
			inputMessage.put((String) inputMessage.get("NODE_NAME"), nodeMessage);
			resultMap.put("INPUT_MESSAGE", inputMessage);

			String sessionUUID = (String) inputMessage.get("SESSION_UUID");
			String sessionPassword = (String) inputMessage.get("SESSION_PASSWORD");
			String topic = "topic/" + sessionUUID + "/" + sessionPassword;
			this.template.convertAndSend(topic, inputMessage);

		} catch (Exception e) {
			e.printStackTrace();
		}

		logger.trace("Exit SayAndGather.executeNode()");
		return resultMap;
	}

}
