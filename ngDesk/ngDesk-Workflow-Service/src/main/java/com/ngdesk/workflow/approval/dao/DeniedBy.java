package com.ngdesk.workflow.approval.dao;

public class DeniedBy {

	private String deniedUser;

	private String deniedComments;

	public DeniedBy(String deniedUser, String deniedComments) {
		super();
		this.deniedUser = deniedUser;
		this.deniedComments = deniedComments;
	}

	public DeniedBy() {
		super();
	}

	public String getDeniedUser() {
		return deniedUser;
	}

	public void setDeniedUser(String deniedUser) {
		this.deniedUser = deniedUser;
	}

	public String getDeniedComments() {
		return deniedComments;
	}

	public void setDeniedComments(String deniedComments) {
		this.deniedComments = deniedComments;
	}

}
