package com.ngdesk.workflow.dao;

import java.util.List;

import javax.validation.Valid;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateEntryNode extends Node {

	@JsonProperty("MODULE")
	@Field("MODULE")
	private String module;

	@JsonProperty("FIELDS")
	@Field("FIELDS")
	List<NodeField> fields;

	@JsonProperty("ENTRY_ID")
	@Field("ENTRY_ID")
	private String entryId;
	
	@JsonProperty("REPLACE")
	@Field("REPLACE")
	private Boolean replace;

	public UpdateEntryNode() {
	}

	public UpdateEntryNode(String module, @Valid List<NodeField> fields, String entryId, Boolean replace) {
		this.module = module;
		this.fields = fields;
		this.entryId = entryId;
		this.replace=replace;
	}

	

	public Boolean getReplace() {
		return replace;
	}

	public void setReplace(Boolean replace) {
		this.replace = replace;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public List<NodeField> getFields() {
		return fields;
	}

	public void setFields(List<NodeField> fields) {
		this.fields = fields;
	}

	public String getEntryId() {
		return entryId;
	}

	public void setEntryId(String entryId) {
		this.entryId = entryId;
	}

}