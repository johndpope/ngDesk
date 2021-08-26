package com.ngdesk.nodes;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.Global;

@Component
public class Start extends Node {
	private static final Logger logger = LoggerFactory.getLogger(Start.class);

	@Override
	public Map<String, Object> executeNode(Document node, Map<String, Object> inputMessage) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		logger.trace("Enter Start.executeNode()");
		try {
			ArrayList<Document> connections = (ArrayList<Document>) node.get("CONNECTIONS_TO");

			if (connections.size() == 1) {
				Document connection = connections.get(0);
				resultMap.put("NODE_ID", connection.getString("TO_NODE"));
			}

			resultMap.put("INPUT_MESSAGE", inputMessage);
		} catch (Exception e) {
			e.printStackTrace();
		}

		logger.trace("Exit Start.executeNode()");
		return resultMap;
	}
}
