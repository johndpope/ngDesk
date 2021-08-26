package com.ngdesk.tracking;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Step {

	@JsonProperty("STEP")
	@NotNull(message = "STEP_NOT_NULL")
	private int step;

	@JsonProperty("INVITED_USER")
	@NotNull(message = "INVITED_USER_NOT_NULL")
	private boolean invitedUser;

	@JsonProperty("CREATED_FIRST_TICKET")
	@NotNull(message = "CREATED_FIRST_TICKET_NOT_NULL")
	private boolean createdFirstTicket;

	public Step() {

	}

	public Step(@NotNull(message = "STEP_NOT_NULL") int step,
			@NotNull(message = "INVITED_USER_NOT_NULL") boolean invitedUser,
			@NotNull(message = "CREATED_FIRST_TICKET_NOT_NULL") boolean createdFirstTicket) {
		super();
		this.step = step;
		this.invitedUser = invitedUser;
		this.createdFirstTicket = createdFirstTicket;
	}

	public int getStep() {
		return step;
	}

	public void setStep(int step) {
		this.step = step;
	}

	public boolean isInvitedUser() {
		return invitedUser;
	}

	public void setInvitedUser(boolean invitedUser) {
		this.invitedUser = invitedUser;
	}

	public boolean isCreatedFirstTicket() {
		return createdFirstTicket;
	}

	public void setCreatedFirstTicket(boolean createdFirstTicket) {
		this.createdFirstTicket = createdFirstTicket;
	}

}
