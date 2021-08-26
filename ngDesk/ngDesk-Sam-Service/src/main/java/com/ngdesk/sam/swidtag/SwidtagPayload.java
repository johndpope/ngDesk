package com.ngdesk.sam.swidtag;

import com.ngdesk.commons.models.User;

public class SwidtagPayload {

	private User user;

	private Swidtag swidtag;

	public SwidtagPayload() {

	}

	public SwidtagPayload(User user, Swidtag swidtag) {
		super();
		this.user = user;
		this.swidtag = swidtag;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Swidtag getSwidtag() {
		return swidtag;
	}

	public void setSwidtag(Swidtag swidtag) {
		this.swidtag = swidtag;
	}

}
