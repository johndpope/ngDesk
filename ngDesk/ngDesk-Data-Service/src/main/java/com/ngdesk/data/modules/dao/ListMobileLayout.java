package com.ngdesk.data.modules.dao;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ListMobileLayout {

	@JsonProperty("LAYOUT_ID")
	@Field("LAYOUT_ID")
	private String listLayoutId;

	@JsonProperty("NAME")
	@Field("NAME")
	private String name;

	@JsonProperty("DESCRIPTION")
	@Field("DESCRIPTION")
	private String description;

	@JsonProperty("ROLE")
	@Field("ROLE")
	private String role;

	@JsonProperty("FIELDS")
	@Field("FIELDS")
	private List<String> fields;

	@JsonProperty("CONDITIONS")
	@Field("CONDITIONS")
	private List<Condition> conditions;

	public ListMobileLayout() {

	}

	public ListMobileLayout(String listLayoutId, String name, String description, String role, List<String> fields,
			List<Condition> conditions) {
		super();
		this.listLayoutId = listLayoutId;
		this.name = name;
		this.description = description;
		this.role = role;
		this.fields = fields;
		this.conditions = conditions;
	}

	public String getListLayoutId() {
		return listLayoutId;
	}

	public void setListLayoutId(String listLayoutId) {
		this.listLayoutId = listLayoutId;
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

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public List<String> getFields() {
		return fields;
	}

	public void setFields(List<String> fields) {
		this.fields = fields;
	}

	public List<Condition> getConditions() {
		return conditions;
	}

	public void setConditions(List<Condition> conditions) {
		this.conditions = conditions;
	}

}
