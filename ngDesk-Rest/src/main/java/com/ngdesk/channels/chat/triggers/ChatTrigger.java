package com.ngdesk.channels.chat.triggers;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.annotations.ChatTriggerTypeValid;

public class ChatTrigger {

	@JsonProperty("NAME")
	@NotEmpty(message = "NAME_REQUIRED")
	private String name;

	@JsonProperty("DESCRIPTION")
	@NotNull(message = "DESCRIPTION_NOT_NULL")
	private String description;

	@JsonProperty("TYPE")
	@NotEmpty(message = "TYPE_REQUIRED")
	@ChatTriggerTypeValid
	private String type;

	@JsonProperty("CONDITIONS")
	@NotNull(message = "CONDITIONS_NOT_NULL")
	@Size(min = 1, message = "CONDITIONS_REQUIRED")
	@Valid
	private List<Condition> conditions = new ArrayList<Condition>();

	@JsonProperty("ACTIONS")
	@NotNull(message = "ACTIONS_NOT_NULL")
	@Size(min = 1, message = "ACTION_REQUIRED")
	@Valid
	private List<Action> actions = new ArrayList<Action>();

	@JsonProperty("TRIGGER_ID")
	private String triggerId;

	@JsonProperty("DATE_CREATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Timestamp dateCreated;

	@JsonProperty("DATE_UPDATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Timestamp dateUpdated;

	@JsonProperty("LAST_UPDATED_BY")
	private String lastUpdated;

	public ChatTrigger() {

	}

	public ChatTrigger(@NotEmpty(message = "NAME_REQUIRED") String name,
			@NotNull(message = "DESCRIPTION_NOT_NULL") String description,
			@NotEmpty(message = "TYPE_REQUIRED") String type,
			@NotNull(message = "CONDITIONS_NOT_NULL") @Size(min = 1, message = "CONDITIONS_REQUIRED") @Valid List<Condition> conditions,
			@NotNull(message = "ACTIONS_NOT_NULL") @Size(min = 1, message = "ACTION_REQUIRED") @Valid List<Action> actions,
			String triggerId, Timestamp dateCreated, Timestamp dateUpdated, String lastUpdated) {
		super();
		this.name = name;
		this.description = description;
		this.type = type;
		this.conditions = conditions;
		this.actions = actions;
		this.triggerId = triggerId;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.lastUpdated = lastUpdated;
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<Condition> getConditions() {
		return conditions;
	}

	public void setConditions(List<Condition> conditions) {
		this.conditions = conditions;
	}

	public List<Action> getActions() {
		return actions;
	}

	public void setActions(List<Action> actions) {
		this.actions = actions;
	}

	public String getTriggerId() {
		return triggerId;
	}

	public void setTriggerId(String triggerId) {
		this.triggerId = triggerId;
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

	public String getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(String lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

}
