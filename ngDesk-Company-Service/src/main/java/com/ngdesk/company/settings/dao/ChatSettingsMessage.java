package com.ngdesk.company.settings.dao;

public class ChatSettingsMessage {
	private String companyId;

	private String type;

	private ChatSettings chatSettings;

	public ChatSettingsMessage() {

	}

	public ChatSettingsMessage(String companyId, String type, ChatSettings chatSettings) {
		super();
		this.companyId = companyId;
		this.type = type;
		this.chatSettings = chatSettings;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public ChatSettings getChatSettings() {
		return chatSettings;
	}

	public void setChatSettings(ChatSettings chatSettings) {
		this.chatSettings = chatSettings;
	}

}
