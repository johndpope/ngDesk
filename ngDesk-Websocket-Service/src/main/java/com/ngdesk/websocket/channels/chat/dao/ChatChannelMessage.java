package com.ngdesk.websocket.channels.chat.dao;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChatChannelMessage {

	
	private String companyId;

	
	private String sessionUUId;
	

	private ChatChannel chatChannel;


	private String messageType;

	public ChatChannelMessage() {
		super();
	}

	public ChatChannelMessage(String companyId, String sessionUUId, ChatChannel chatChannel, String messageType) {
		super();
		this.companyId = companyId;
		this.sessionUUId = sessionUUId;
		this.chatChannel = chatChannel;
		this.messageType = messageType;
	}

	public String getSessionUUId() {
		return sessionUUId;
	}

	public void setSessionUUId(String sessionUUId) {
		this.sessionUUId = sessionUUId;
	}

	public ChatChannel getChatChannel() {
		return chatChannel;
	}

	public void setChatChannel(ChatChannel chatChannel) {
		this.chatChannel = chatChannel;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

}
