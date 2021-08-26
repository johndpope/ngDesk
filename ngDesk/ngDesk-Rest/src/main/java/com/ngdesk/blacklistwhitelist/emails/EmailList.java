package com.ngdesk.blacklistwhitelist.emails;

import java.util.List;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EmailList {

	@JsonProperty("BLACK_LIST_INCOMING")
	@NotNull(message = "BLACK_LIST_INCOMING_NOT_NULL")
	private List<String> blacklistIncoming;

	@JsonProperty("BLACK_LIST_OUTGOING")
	@NotNull(message = "BLACK_LIST_OUTGOING_NOT_NULL")
	private List<String> blacklistOutgoing;

	@JsonProperty("WHITE_LIST_INCOMING")
	@NotNull(message = "WHITE_LIST_INCOMING_NOT_NULL")
	private List<String> whitelistIncoming;

	@JsonProperty("WHITE_LIST_OUTGOING")
	@NotNull(message = "WHITE_LIST_OUTGOING_NOT_NULL")
	private List<String> whitelistOutgoing;

	public EmailList() {
	}

	public EmailList(@NotNull(message = "BLACK_LIST_INCOMING_NOT_NULL") List<String> blacklistIncoming,
			@NotNull(message = "BLACK_LIST_OUTGOING_NOT_NULL") List<String> blacklistOutgoing,
			@NotNull(message = "WHITE_LIST_INCOMING_NOT_NULL") List<String> whitelistIncoming,
			@NotNull(message = "WHITE_LIST_OUTGOING_NOT_NULL") List<String> whitelistOutgoing) {
		super();
		this.blacklistIncoming = blacklistIncoming;
		this.blacklistOutgoing = blacklistOutgoing;
		this.whitelistIncoming = whitelistIncoming;
		this.whitelistOutgoing = whitelistOutgoing;
	}

	public List<String> getBlacklistIncoming() {
		return blacklistIncoming;
	}

	public void setBlacklistIncoming(List<String> blacklistIncoming) {
		this.blacklistIncoming = blacklistIncoming;
	}

	public List<String> getBlacklistOutgoing() {
		return blacklistOutgoing;
	}

	public void setBlacklistOutgoing(List<String> blacklistOutgoing) {
		this.blacklistOutgoing = blacklistOutgoing;
	}

	public List<String> getWhitelistIncoming() {
		return whitelistIncoming;
	}

	public void setWhitelistIncoming(List<String> whitelistIncoming) {
		this.whitelistIncoming = whitelistIncoming;
	}

	public List<String> getWhitelistOutgoing() {
		return whitelistOutgoing;
	}

	public void setWhitelistOutgoing(List<String> whitelistOutgoing) {
		this.whitelistOutgoing = whitelistOutgoing;
	}

}
