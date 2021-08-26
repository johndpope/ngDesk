package com.ngdesk.discussion;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
public class EndChat {
	
	@JsonProperty("WIDGET_ID")
	@NotEmpty(message="Channel Id Required")
	private String channel;
	
	@JsonProperty("SESSION_UUID")
	@NotEmpty(message="Session UUID Required")
	private String sessionUuid;
	
	@JsonProperty("COMPANY_SUBDOMAIN")
	@NotEmpty(message="Company Subdomain Required")
	private String subdomain;
	
	@JsonProperty("USER_UUID")
	@NotEmpty(message="User UUID Required")
	private String userUuid;
	
	public EndChat() {
		
	}

	public EndChat(@NotEmpty(message = "Channel Id Required") String channel,
			@NotEmpty(message = "Session UUID Required") String sessionUuid,
			@NotEmpty(message = "Company Subdomain Required") String subdomain,
			@NotEmpty(message = "User UUID Required") String userUuid) {
		super();
		this.channel = channel;
		this.sessionUuid = sessionUuid;
		this.subdomain = subdomain;
		this.userUuid = userUuid;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getSessionUuid() {
		return sessionUuid;
	}

	public void setSessionUuid(String sessionUuid) {
		this.sessionUuid = sessionUuid;
	}

	public String getSubdomain() {
		return subdomain;
	}

	public void setSubdomain(String subdomain) {
		this.subdomain = subdomain;
	}

	public String getUserUuid() {
		return userUuid;
	}

	public void setUserUuid(String userUuid) {
		this.userUuid = userUuid;
	} 
	
	
}
