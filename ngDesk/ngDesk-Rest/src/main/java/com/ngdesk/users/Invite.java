package com.ngdesk.users;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Invite {

	@JsonProperty("USERS")
	@NotNull(message = "NO_USERS_TO_INVITE")
	@Size(min = 1, message = "NO_USERS_TO_INVITE")
	@Valid
	private List<InviteUser> users;

	public Invite() {

	}

	public Invite(
			@NotNull(message = "NO_USERS_TO_INVITE") @Size(min = 1, message = "NO_USERS_TO_INVITE") List<InviteUser> users) {
		super();
		this.users = users;
	}

	public List<InviteUser> getUsers() {
		return users;
	}

	public void setUsers(List<InviteUser> users) {
		this.users = users;
	}

}
