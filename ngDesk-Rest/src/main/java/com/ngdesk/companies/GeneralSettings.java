package com.ngdesk.companies;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.annotations.Timezone;

public class GeneralSettings {

	@JsonProperty("LANGUAGE")
	@NotNull(message = "LANGUAGE_NOT_NULL")
	@Size(max = 2, message = "LANGUAGE_MUST_BE_2_CHAR")
	@Pattern(regexp = "^[a-z]{2}$", message = "LANGUAGE_MUST_BE_CHAR")
	private String language;

	@JsonProperty("TIMEZONE")
	@NotNull(message = "TIMEZONE_NOT_NULL")
	@Timezone(message = "TIMEZONE_INVALID")
	private String timezone;
	
	@JsonProperty("LOCALE")
	@NotNull(message = "LOCALE_NOT_NULL")
	@Pattern(regexp = "^[a-zA-Z-]{2,12}$", message = "LOCALE_MUST_BE_CHAR")
	private String locale;

	public GeneralSettings() {

	}

	public GeneralSettings(
			@NotNull(message = "LANGUAGE_NOT_NULL") @Size(max = 2, message = "LANGUAGE_MUST_BE_2_CHAR") @Pattern(regexp = "^[a-z]{2}$", message = "LANGUAGE_MUST_BE_CHAR") String language,
			@NotNull(message = "TIMEZONE_NOT_NULL") String timezone,
			@NotNull(message = "LOCALE_NOT_NULL") @Pattern(regexp = "^[a-zA-Z-]{2,12}$", message = "LOCALE_MUST_BE_CHAR") String locale) {
		super();
		this.language = language;
		this.timezone = timezone;
		this.locale = locale;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	
}
