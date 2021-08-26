package com.ngdesk;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import org.springframework.web.socket.sockjs.frame.Jackson2SockJsMessageCodec;

public class ManagerWebSocket {

	private final static WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
	private static Logger log = LoggerFactory.getLogger(ManagerWebSocket.class);

	public ListenableFuture<StompSession> connect(String url) {
		try {
			log.trace("Enter ManagerWebSocket.connect() : url " + url);

			Transport webSocketTransport = new WebSocketTransport(new StandardWebSocketClient());
			List<Transport> transports = Collections.singletonList(webSocketTransport);

			SockJsClient sockJsClient = new SockJsClient(transports);
			sockJsClient.setMessageCodec(new Jackson2SockJsMessageCodec());
			

			WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);			
			log.trace("Exit ManagerWebSocket.connect() : url " + url);

			return stompClient.connect(url, headers, new CustomStompSessionHandler());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
