package com.ngdesk.company.plugin.dao;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.commons.annotations.CustomNotEmpty;
import com.ngdesk.commons.annotations.CustomNotNull;

import io.swagger.v3.oas.annotations.media.Schema;

public class TierModule {

	@Schema(description = "Name of the module", required = true, example = "Tickets")
	@Field("NAME")
	@JsonProperty("NAME")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "MODULE_NAME" })
	@CustomNotNull(message = "NOT_NULL", values = { "MODULE_NAME" })
	private String name;

	@Field("CHILD_MODULE")
	@JsonProperty("CHILD_MODULE")
	private List<String> childModule;

	public TierModule() {
		super();
	}

	public TierModule(String name, List<String> childModule) {
		super();
		this.name = name;
		this.childModule = childModule;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getChildModule() {
		return childModule;
	}

	public void setChildModule(List<String> childModule) {
		this.childModule = childModule;
	}

}
