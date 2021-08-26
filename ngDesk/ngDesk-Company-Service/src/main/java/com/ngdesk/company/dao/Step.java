package com.ngdesk.company.dao;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Step {

	@JsonProperty("INVITED_USER")
	@Field("INVITED_USER")
	private Boolean invitedUser = false;

	@JsonProperty("CREATED_FIRST_TICKET")
	@Field("CREATED_FIRST_TICKET")
	private Boolean createdFirstTicket = false;

	public Step() {
		super();
	}

	public Step(Boolean invitedUser, Boolean createdFirstTicket) {
		super();
		this.invitedUser = invitedUser;
		this.createdFirstTicket = createdFirstTicket;
	}

	public Boolean getInvitedUser() {
		return invitedUser;
	}

	public void setInvitedUser(Boolean invitedUser) {
		this.invitedUser = invitedUser;
	}

	public Boolean getCreatedFirstTicket() {
		return createdFirstTicket;
	}

	public void setCreatedFirstTicket(Boolean createdFirstTicket) {
		this.createdFirstTicket = createdFirstTicket;
	}

}
