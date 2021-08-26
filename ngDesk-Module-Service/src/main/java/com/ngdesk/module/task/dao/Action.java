package com.ngdesk.module.task.dao;

import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.ngdesk.commons.annotations.CustomNotEmpty;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", visible = true)
@JsonSubTypes({ @JsonSubTypes.Type(value = CreateEntry.class, name = "CreateEntry") })
public abstract class Action {

	@Schema(required = true, description = "type of the node", example = "CreateEntry")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "NODE_TYPE" })
	@Pattern(regexp = "CreateEntry", message = "INVALID_NODE_TYPE")
	private String type;

	public Action() {

	}

	public Action(@Pattern(regexp = "CreateEntry", message = "INVALID_NODE_TYPE") String type) {
		super();
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}