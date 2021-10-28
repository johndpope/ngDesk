package com.ngdesk.websocket.channels.chat.dao;

public class ChatVisitedPages {

	public String visitedPages;

	public String sessionUUID;

	public String companySubdomain;

	public ChatVisitedPages(String visitedPages, String sessionUUID, String companySubdomain) {
		super();
		this.visitedPages = visitedPages;
		this.sessionUUID = sessionUUID;
		this.companySubdomain = companySubdomain;
	}

	public String getVisitedPages() {
		return visitedPages;
	}

	public void setVisitedPages(String visitedPages) {
		this.visitedPages = visitedPages;
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

}
