package com.ngdesk.createuser;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateUser {

	@JsonProperty("PAGE_LOAD")
	private Object pageLoad;

	@JsonProperty("USER")
	private Object user;

	public Object getPageLoad() {
		return pageLoad;
	}

	public void setPageLoad(Object pageLoad) {
		this.pageLoad = pageLoad;
	}

	public Object getUser() {
		return user;
	}

	public void setUser(Object user) {
		this.user = user;
	}

}
