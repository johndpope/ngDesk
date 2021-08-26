package com.ngdesk.companies;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ColorPicker {

	@JsonProperty("FAVICON")
	@NotNull(message = "FAVICON_REQUIRED")
	LogoTemplate favicon;

	@JsonProperty("SIDEBAR")
	@NotNull(message = "SIDEBAR_LOGO_REQUIRED")
	LogoTemplate sidebarLogo;

	@JsonProperty("LOGIN_PAGE")
	@NotNull(message = "LOGIN_PAGE_LOGO_REQUIRED")
	LogoTemplate loginPageLogo;

	@JsonProperty("SIGNUP_PAGE")
	@NotNull(message = "SIGNUP_PAGE_LOGO_REQUIRED")
	LogoTemplate signupPageLogo;

	@JsonProperty("PRIMARY_COLOR")
	@NotEmpty(message = "PRIMARY_COLOR_REQUIRED")
	private String primaryColor;

	@JsonProperty("SECONDARY_COLOR")
	@NotEmpty(message = "SECONDARY_COLOR_REQUIRED")
	private String secondaryColor;

	public ColorPicker() {

	}

	public ColorPicker(@NotNull(message = "FAVICON_REQUIRED") LogoTemplate favicon,
			@NotNull(message = "SIDEBAR_LOGO_REQUIRED") LogoTemplate sidebarLogo,
			@NotNull(message = "LOGIN_PAGE_LOGO_REQUIRED") LogoTemplate loginPageLogo,
			@NotNull(message = "SIGNUP_PAGE_LOGO_REQUIRED") LogoTemplate signupPageLogo,
			@NotEmpty(message = "PRIMARY_COLOR_REQUIRED") String primaryColor,
			@NotEmpty(message = "SECONDARY_COLOR_REQUIRED") String secondaryColor) {
		super();
		this.favicon = favicon;
		this.sidebarLogo = sidebarLogo;
		this.loginPageLogo = loginPageLogo;
		this.signupPageLogo = signupPageLogo;
		this.primaryColor = primaryColor;
		this.secondaryColor = secondaryColor;
	}

	public LogoTemplate getFavicon() {
		return favicon;
	}

	public void setFavicon(LogoTemplate favicon) {
		this.favicon = favicon;
	}

	public LogoTemplate getSidebarLogo() {
		return sidebarLogo;
	}

	public void setSidebarLogo(LogoTemplate sidebarLogo) {
		this.sidebarLogo = sidebarLogo;
	}

	public LogoTemplate getLoginPageLogo() {
		return loginPageLogo;
	}

	public void setLoginPageLogo(LogoTemplate loginPageLogo) {
		this.loginPageLogo = loginPageLogo;
	}

	public LogoTemplate getSignupPageLogo() {
		return signupPageLogo;
	}

	public void setSignupPageLogo(LogoTemplate signupPageLogo) {
		this.signupPageLogo = signupPageLogo;
	}

	public String getPrimaryColor() {
		return primaryColor;
	}

	public void setPrimaryColor(String primaryColor) {
		this.primaryColor = primaryColor;
	}

	public String getSecondaryColor() {
		return secondaryColor;
	}

	public void setSecondaryColor(String secondaryColor) {
		this.secondaryColor = secondaryColor;
	}

}
