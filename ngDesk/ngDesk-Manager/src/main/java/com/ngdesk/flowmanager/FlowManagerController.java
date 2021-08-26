package com.ngdesk.flowmanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.nodes.ParentNode;

@Component
@Controller
public class FlowManagerController {
	private static final Logger logger = LoggerFactory.getLogger(FlowManagerController.class);

	private ParentNode parentNode = new ParentNode();

	@MessageMapping("/flow")
	public void entry(InputMessage message) throws Exception {
		logger.trace("Enter FlowManagerController.entry()");

		logger.info("/flow entry, message : " + message);
		logger.info("/flow entry, message : "
				+ new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(message));

		parentNode.execute(message);

		logger.trace("Exit FlowManagerController.entry()");
	}

}
