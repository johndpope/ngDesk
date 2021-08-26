package com.ngdesk.websocket.sam.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Controller {

	@Id
	@JsonProperty("CONTROLLER_ID")
	private String id;

	@JsonProperty("STATUS")
	@Field("STATUS")
	private String status;

	@JsonProperty("LAST_SEEN")
	@Field("LAST_SEEN")
	private Date lastSeen;

	@JsonProperty("SUB_APPS")
	@Field("SUB_APPS")
	private List<SubApp> subApps;

	public Controller() {

	}

	public Controller(String id, String status, Date lastSeen, List<SubApp> subApps) {
		super();
		this.id = id;
		this.status = status;
		this.lastSeen = lastSeen;
		this.subApps = subApps;
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

	public Date getLastSeen() {
		return lastSeen;
	}

	public void setLastSeen(Date lastSeen) {
		this.lastSeen = lastSeen;
	}

	public List<SubApp> getSubApps() {
		return subApps;
	}

	public void setSubApps(List<SubApp> subApps) {
		this.subApps = subApps;
	}

}
