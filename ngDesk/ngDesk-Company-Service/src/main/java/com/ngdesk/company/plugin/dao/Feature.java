package com.ngdesk.company.plugin.dao;
import org.springframework.data.mongodb.core.mapping.Field;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;


@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Feature {

	@Schema(description = "SLAS Feature", required = true)
	@Field("SLAS")
	@JsonProperty("SLAS")
	private Property slas;
	
	@Schema(description = "Trigger Feature", required = true)
	@Field("TRIGGERS")
	@JsonProperty("TRIGGERS")
	private Property triggers;
	
	@Schema(description = "Fields Feature", required = true)
	@Field("FIELDS")
	@JsonProperty("FIELDS")
	private Property fields;

	public Feature() {
		super();
	}

	public Feature(Property slas, Property triggers, Property fields) {
		super();
		this.slas = slas;
		this.triggers = triggers;
		this.fields = fields;
	}

	public Property getSlas() {
		return slas;
	}

	public void setSlas(Property slas) {
		this.slas = slas;
	}

	public Property getTriggers() {
		return triggers;
	}

	public void setTriggers(Property triggers) {
		this.triggers = triggers;
	}

	public Property getFields() {
		return fields;
	}

	public void setFields(Property fields) {
		this.fields = fields;
	}
}
