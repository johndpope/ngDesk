package com.ngdesk.modules;

import java.sql.Timestamp;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PremadeResponse {

	@JsonProperty("PREMADE_RESPONSE_ID")
	private String premadeResponseId;

	@JsonProperty("NAME")
	@NotNull(message = "RESPONSE_NAME_NOT_NULL")
	@Size(min = 1, message = "RESPONSE_NAME_NOT_EMPTY")
	private String name;

	@JsonProperty("DESCRIPTION")
	private String description;

	@JsonProperty("MESSAGE")
	@NotNull(message = "RESPONSE_MESSAGE_NOT_NULL")
	@Size(min = 1, message = "RESPONSE_MESSAGE_NOT_EMPTY")
	private String message;

	@JsonProperty("TEAMS")
	@NotNull(message = "TEAM_IDS_NOT_NULL")
	private List<String> teams;

	@JsonProperty("MODULE")
	@NotNull(message = "MODULE_NOT_NULL")
	private String module;

	@JsonProperty("DATE_CREATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Timestamp dateCreated;

	@JsonProperty("DATE_UPDATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Timestamp dateUpdated;

	@JsonProperty("LAST_UPDATED_BY")
	private String lastUpdatedBy;

	@JsonProperty("CREATED_BY")
	private String createdBy;

	PremadeResponse() {
	}

	public PremadeResponse(String premadeResponseId,
			@NotNull(message = "RESPONSE_NAME_NOT_NULL") @Size(min = 1, message = "RESPONSE_NAME_NOT_EMPTY") String name,
			String description,
			@NotNull(message = "RESPONSE_MESSAGE_NOT_NULL") @Size(min = 1, message = "RESPONSE_MESSAGE_NOT_EMPTY") String message,
			@NotNull(message = "TEAM_IDS_NOT_NULL") List<String> teams,
			@NotNull(message = "MODULE_NOT_NULL") String module, Timestamp dateCreated, Timestamp dateUpdated,
			String lastUpdatedBy, String createdBy) {
		super();
		this.premadeResponseId = premadeResponseId;
		this.name = name;
		this.description = description;
		this.message = message;
		this.teams = teams;
		this.module = module;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.lastUpdatedBy = lastUpdatedBy;
		this.createdBy = createdBy;
	}

	public String getPremadeResponseId() {
		return premadeResponseId;
	}

	public void setPremadeResponseId(String premadeResponseId) {
		this.premadeResponseId = premadeResponseId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public List<String> getTeams() {
		return teams;
	}

	public void setTeams(List<String> teams) {
		this.teams = teams;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public Timestamp getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Timestamp dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Timestamp getDateUpdated() {
		return dateUpdated;
	}

	public void setDateUpdated(Timestamp dateUpdated) {
		this.dateUpdated = dateUpdated;
	}

	public String getLastUpdatedBy() {
		return lastUpdatedBy;
	}

	public void setLastUpdatedBy(String lastUpdatedBy) {
		this.lastUpdatedBy = lastUpdatedBy;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

}
