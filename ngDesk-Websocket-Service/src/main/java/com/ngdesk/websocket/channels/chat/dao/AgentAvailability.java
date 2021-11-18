package com.ngdesk.websocket.channels.chat.dao;

public class AgentAvailability {

	public String sessionUUID;

	public String companySubdomain;

	public String type;

	public String channelName;

	public Boolean isAgentAvailable;

	public Boolean isBussinessHoursEnabled;

	public String message;

	public AgentAvailability() {
		super();
	}

	public AgentAvailability(String sessionUUID, String companySubdomain, String type, String channelName,
			Boolean isAgentAvailable, Boolean isBussinessHoursEnabled, String message) {
		super();
		this.sessionUUID = sessionUUID;
		this.companySubdomain = companySubdomain;
		this.type = type;
		this.channelName = channelName;
		this.isAgentAvailable = isAgentAvailable;
		this.isBussinessHoursEnabled = isBussinessHoursEnabled;
		this.message = message;
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

	public String getChannelName() {
		return channelName;
	}

	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}

	public Boolean getIsAgentAvailable() {
		return isAgentAvailable;
	}

	public void setIsAgentAvailable(Boolean isAgentAvailable) {
		this.isAgentAvailable = isAgentAvailable;
	}

	public Boolean getIsBussinessHoursEnabled() {
		return isBussinessHoursEnabled;
	}

	public void setIsBussinessHoursEnabled(Boolean isBussinessHoursEnabled) {
		this.isBussinessHoursEnabled = isBussinessHoursEnabled;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
