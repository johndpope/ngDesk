package com.ngdesk.companies;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginLogo {

	@JsonProperty("ICON")
	@NotEmpty(message = "ICON_REQUIRED")
	private String icon;

	@JsonProperty("NAME")
	@NotEmpty(message = "FILE_NAME_REQUIRED")
	private String name;

	private LoginLogo() {

	}

	public LoginLogo(@NotEmpty(message = "ICON_REQUIRED") String icon,
			@NotEmpty(message = "FILE_NAME_REQUIRED") String name) {
		super();
		this.icon = icon;
		this.name = name;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
