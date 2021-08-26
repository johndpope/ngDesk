package com.ngdesk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

public class CustomStompSessionHandler extends StompSessionHandlerAdapter {

	private final Logger logger = LoggerFactory.getLogger(CustomStompSessionHandler.class);

	@Override
	public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
		logger.trace("CustomStompSessionHandler.afterConnect() : " + session.getSessionId());
	}

	@Override
	public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload,
			Throwable exception) {
		logger.trace("CustomStompSessionHandler.handleException() : ", exception);
	}

	@Override
	public void handleFrame(StompHeaders headers, Object payload) {
//        logger.info("Received : " + msg.getText() + " from : " + msg.getFrom());
	}

}
