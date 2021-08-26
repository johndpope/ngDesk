package com.ngdesk.workflow.sam.dao;

import java.util.Date;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.commons.annotations.CustomNotNull;

public class SubApp {
	@JsonProperty("NAME")
	@Field("NAME")
	@CustomNotNull(message = "NOT_NULL", values = { "SUB_APP_NAME" })
	private String name;

	@JsonProperty("STATUS")
	@Field("STATUS")
	@CustomNotNull(message = "NOT_NULL", values = { "SUB_APP_STATUS" })
	private String status;

	@JsonProperty("LAST_SEEN")
	@Field("LAST_SEEN")
	private Date lastSeen;

	public SubApp() {
	}

	public SubApp(String name, String status, Date lastSeen) {
		super();
		this.name = name;
		this.status = status;
		this.lastSeen = lastSeen;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getLastSeen() {
		return lastSeen;
	}

	public void setLastSeen(Date lastSeen) {
		this.lastSeen = lastSeen;
	}
}
