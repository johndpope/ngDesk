package com.ngdesk.company.dao;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CustomLogin {

	@Field("HEADER")
	@JsonProperty("HEADER")
	private String header;

	@Field("COMPANY_ID")
	@JsonProperty("COMPANY_ID")
	private String companyId;

	@Field("LOGIN_ICON")
	@JsonProperty("LOGIN_ICON")
	private LoginIcon loginIcon;

	public CustomLogin() {
		super();
	}

	public CustomLogin(String header, String companyId, LoginIcon loginIcon) {
		super();
		this.header = header;
		this.companyId = companyId;
		this.loginIcon = loginIcon;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public LoginIcon getLoginIcon() {
		return loginIcon;
	}

	public void setLoginIcon(LoginIcon loginIcon) {
		this.loginIcon = loginIcon;
	}

}
