package com.ngdesk.websocket.roles.dao;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ModuleLevelPermission {

	@JsonProperty("ACCESS")
	@Field("ACCESS")
	private String access;

	@JsonProperty("ACCESS_TYPE")
	@Field("ACCESS_TYPE")
	private String accessType;

	@JsonProperty("EDIT")
	@Field("EDIT")
	private String edit;

	@JsonProperty("VIEW")
	@Field("VIEW")
	private String view;

	@JsonProperty("DELETE")
	@Field("DELETE")
	private String delete;

	public ModuleLevelPermission() {

	}

	
	public ModuleLevelPermission(String access, String accessType, String edit, String view, String delete) {
		super();
		this.access = access;
		this.accessType = accessType;
		this.edit = edit;
		this.view = view;
		this.delete = delete;
	}


	public String getAccess() {
		return access;
	}

	public void setAccess(String access) {
		this.access = access;
	}

	public String getAccessType() {
		return accessType;
	}

	public void setAccessType(String accessType) {
		this.accessType = accessType;
	}

	public String getEdit() {
		return edit;
	}

	public void setEdit(String edit) {
		this.edit = edit;
	}

	public String getView() {
		return view;
	}

	public void setView(String view) {
		this.view = view;
	}

	public String getDelete() {
		return delete;
	}

	public void setDelete(String delete) {
		this.delete = delete;
	}

}
