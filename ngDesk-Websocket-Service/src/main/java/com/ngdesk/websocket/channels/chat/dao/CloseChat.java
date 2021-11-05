
package com.ngdesk.websocket.channels.chat.dao;

public class CloseChat {

	private String sessionUUID;

	private String subdomain;

	private boolean isSendChatTranscript;

	private boolean isAgentCloseChat;

	public CloseChat() {
		super();
	}

	public CloseChat(String sessionUUID, String subdomain, boolean isSendChatTranscript, boolean isAgentCloseChat) {
		super();
		this.sessionUUID = sessionUUID;
		this.subdomain = subdomain;
		this.isSendChatTranscript = isSendChatTranscript;
		this.isAgentCloseChat = isAgentCloseChat;
	}

	public String getSessionUUID() {
		return sessionUUID;
	}

	public void setSessionUUID(String sessionUUID) {
		this.sessionUUID = sessionUUID;
	}

	public String getSubdomain() {
		return subdomain;
	}

	public void setSubdomain(String subdomain) {
		this.subdomain = subdomain;
	}

	public boolean isSendChatTranscript() {
		return isSendChatTranscript;
	}

	public void setSendChatTranscript(boolean isSendChatTranscript) {
		this.isSendChatTranscript = isSendChatTranscript;
	}

	public boolean isAgentCloseChat() {
		return isAgentCloseChat;
	}

	public void setAgentCloseChat(boolean isAgentCloseChat) {
		this.isAgentCloseChat = isAgentCloseChat;
	}

}
