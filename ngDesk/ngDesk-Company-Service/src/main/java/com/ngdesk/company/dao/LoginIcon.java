package com.ngdesk.company.dao;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginIcon {

	@Field("NAME")
	@JsonProperty("NAME")
	private String name;

	@Field("ICON")
	@JsonProperty("ICON")
	private String icon;

	public LoginIcon() {
		super();
	}

	public LoginIcon(String name, String icon) {
		super();
		this.name = name;
		this.icon = icon;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

}
