package com.ngdesk.websocket.channels.chat.dao;

import java.util.List;

public class ChatVisitedPagesNotification {

	public List<String> visitedPages;

	public String sessionUUID;

	public String userId;

	public String type;

	private String companyId;

	public ChatVisitedPagesNotification() {
		super();
	}

	public ChatVisitedPagesNotification(List<String> visitedPages, String sessionUUID, String userId, String type,
			String companyId) {
		super();
		this.visitedPages = visitedPages;
		this.sessionUUID = sessionUUID;
		this.userId = userId;
		this.type = type;
		this.companyId = companyId;
	}

	public List<String> getVisitedPages() {
		return visitedPages;
	}

	public void setVisitedPages(List<String> visitedPages) {
		this.visitedPages = visitedPages;
	}

	public String getSessionUUID() {
		return sessionUUID;
	}

	public void setSessionUUID(String sessionUUID) {
		this.sessionUUID = sessionUUID;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

}
