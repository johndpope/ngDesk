package com.ngdesk.companies;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UsageType {

	@JsonProperty("TICKETS")
	private boolean tickets;

	@JsonProperty("PAGER")
	private boolean pager;

	@JsonProperty("CHAT")
	private boolean chat;

	public UsageType() {
	}

	public UsageType(boolean tickets, boolean pager, boolean chat) {
		super();
		this.tickets = tickets;
		this.pager = pager;
		this.chat = chat;
	}

	public boolean isTickets() {
		return tickets;
	}

	public void setTickets(boolean tickets) {
		this.tickets = tickets;
	}

	public boolean isPager() {
		return pager;
	}

	public void setPager(boolean pager) {
		this.pager = pager;
	}

	public boolean isChat() {
		return chat;
	}

	public void setChat(boolean chat) {
		this.chat = chat;
	}

}
