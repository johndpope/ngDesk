package com.ngdesk.sam.controllers;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class SubApp {

	@JsonProperty("NAME")
	private String name;

	@JsonProperty("ID")
	private String id;

	@JsonProperty("STATUS")
	private String status;

	@JsonProperty("LAST_SEEN")
	@JsonDeserialize(using = MongoDateConverter.class)
	private String lastSeen;

	public SubApp() {
	}

	public SubApp(String name, String id, String status, String lastSeen) {
		super();
		this.name = name;
		this.id = id;
		this.status = status;
		this.lastSeen = lastSeen;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getLastSeen() {
		return lastSeen;
	}

	public void setLastSeen(String lastSeen) {
		this.lastSeen = lastSeen;
	}

}
