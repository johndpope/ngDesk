
package com.ngdesk.websocket.channels.chat.dao;

public class CloseChat {

	private String sessionUUID;

	private String subdomain;

	private Boolean isSendChatTranscript;

	private Boolean isAgentCloseChat;

	public CloseChat() {
		super();
	}

	public CloseChat(String sessionUUID, String subdomain, Boolean isSendChatTranscript, Boolean isAgentCloseChat) {
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

	public Boolean getIsSendChatTranscript() {
		return isSendChatTranscript;
	}

	public void setIsSendChatTranscript(Boolean isSendChatTranscript) {
		this.isSendChatTranscript = isSendChatTranscript;
	}

	public Boolean getIsAgentCloseChat() {
		return isAgentCloseChat;
	}

	public void setIsAgentCloseChat(Boolean isAgentCloseChat) {
		this.isAgentCloseChat = isAgentCloseChat;
	}

}
