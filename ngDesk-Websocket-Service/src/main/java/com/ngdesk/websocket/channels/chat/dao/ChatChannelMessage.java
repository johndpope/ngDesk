package com.ngdesk.websocket.channels.chat.dao;

public class ChatChannelMessage {

	private String companyId;

	private String sessionUUId;

	private ChatChannel chatChannel;

	private String type;

	public ChatChannelMessage() {
		super();
	}

	public ChatChannelMessage(String companyId, String sessionUUId, ChatChannel chatChannel, String type) {
		super();
		this.companyId = companyId;
		this.sessionUUId = sessionUUId;
		this.chatChannel = chatChannel;
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

}
