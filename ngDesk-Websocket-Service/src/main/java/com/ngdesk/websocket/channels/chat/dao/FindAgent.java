package com.ngdesk.websocket.channels.chat.dao;

public class FindAgent {

	public String sessionUUID;

	public String companySubdomain;

	public String type;

	public Boolean isAgentAvailable;

	public FindAgent() {
		super();
	}

	public FindAgent(String sessionUUID, String companySubdomain, String type, Boolean isAgentAvailable) {
		super();
		this.sessionUUID = sessionUUID;
		this.companySubdomain = companySubdomain;
		this.type = type;
		this.isAgentAvailable = isAgentAvailable;
	}

	public String getSessionUUID() {
		return sessionUUID;
	}

	public void setSessionUUID(String sessionUUID) {
		this.sessionUUID = sessionUUID;
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

	public Boolean getIsAgentAvailable() {
		return isAgentAvailable;
	}

	public void setIsAgentAvailable(Boolean isAgentAvailable) {
		this.isAgentAvailable = isAgentAvailable;
	}

}
