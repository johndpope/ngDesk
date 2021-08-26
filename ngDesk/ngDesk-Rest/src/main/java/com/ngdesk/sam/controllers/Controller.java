package com.ngdesk.sam.controllers;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class Controller {

	@JsonProperty("CONTROLLER_ID")
	private String id;

	@JsonProperty("HOST_NAME")
	private String hostName;

	@JsonProperty("SUB_APPS")
	private List<SubApp> subAppList;

	@JsonProperty("STATUS")
	private String status;

	@JsonProperty("UPDATER_STATUS")
	private String updaterStatus;

	@JsonProperty("UPDATER_LAST_SEEN")
	@JsonDeserialize(using = MongoDateConverter.class)
	private String updaterLastSeen;

	@JsonProperty("LAST_SEEN")
	@JsonDeserialize(using = MongoDateConverter.class)
	private String lastSeen;

	@JsonProperty("LOGS")
	private List<ControllerLog> logs;

	@JsonProperty("COMPANY_ID")
	private String companyId;

	@JsonProperty("INSTRUCTIONS")
	private List<Instruction> instructions;

	public Controller() {
		super();
	}

	public Controller(String id, String hostName, List<SubApp> subAppList, String status, String updaterStatus,
			String updaterLastSeen, String lastSeen, List<ControllerLog> logs, String companyId,
			List<Instruction> instructions) {
		super();
		this.id = id;
		this.hostName = hostName;
		this.subAppList = subAppList;
		this.status = status;
		this.updaterStatus = updaterStatus;
		this.updaterLastSeen = updaterLastSeen;
		this.lastSeen = lastSeen;
		this.logs = logs;
		this.companyId = companyId;
		this.instructions = instructions;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public List<SubApp> getSubAppList() {
		return subAppList;
	}

	public void setSubAppList(List<SubApp> subAppList) {
		this.subAppList = subAppList;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getUpdaterStatus() {
		return updaterStatus;
	}

	public void setUpdaterStatus(String updaterStatus) {
		this.updaterStatus = updaterStatus;
	}

	public String getUpdaterLastSeen() {
		return updaterLastSeen;
	}

	public void setUpdaterLastSeen(String updaterLastSeen) {
		this.updaterLastSeen = updaterLastSeen;
	}

	public String getLastSeen() {
		return lastSeen;
	}

	public void setLastSeen(String lastSeen) {
		this.lastSeen = lastSeen;
	}

	public List<ControllerLog> getLogs() {
		return logs;
	}

	public void setLogs(List<ControllerLog> logs) {
		this.logs = logs;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public List<Instruction> getInstructions() {
		return instructions;
	}

	public void setInstructions(List<Instruction> instructions) {
		this.instructions = instructions;
	}

}