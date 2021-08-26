package com.ngdesk.company.dao;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

public class UsageType {

	@Schema(required = false, description = "usagetype of ngdesk for ticketing")
	@JsonProperty("TICKETS")
	@Field("TICKETS")
	private boolean tickets;

	@Schema(required = false, description = "usagetype of ngdesk for paging")
	@JsonProperty("PAGER")
	@Field("PAGER")
	private boolean pager;

	@Schema(required = false, description = "usagetype of ngdesk for chat")
	@JsonProperty("CHAT")
	@Field("CHAT")
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
