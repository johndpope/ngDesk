package com.ngdesk.websocket.channels.chat.dao;

public class AgentChattingStatusCheck {

	private String userId;

	private String companySubdomain;

	private String type;

	private String message;

	public AgentChattingStatusCheck() {
		super();
	}

	public AgentChattingStatusCheck(String userId, String companySubdomain, String type, String message) {
		super();
		this.userId = userId;
		this.companySubdomain = companySubdomain;
		this.type = type;
		this.message = message;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getCompanySubdomain() {
		return companySubdomain;
	}

	public void setCompanySubdomain(String companySubdomain) {
		this.companySubdomain = companySubdomain;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
