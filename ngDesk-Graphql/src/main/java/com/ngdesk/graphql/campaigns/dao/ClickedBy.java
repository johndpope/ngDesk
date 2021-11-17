package com.ngdesk.graphql.campaigns.dao;

public class ClickedBy {

	private String emailAddress;

	private String companyId;

	private int clicks;

	public ClickedBy() {

	}

	public ClickedBy(String emailAddress, String companyId, int clicks) {
		super();
		this.emailAddress = emailAddress;
		this.companyId = companyId;
		this.clicks = clicks;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public int getClicks() {
		return clicks;
	}

	public void setClicks(int clicks) {
		this.clicks = clicks;
	}

}
