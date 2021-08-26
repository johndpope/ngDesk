package com.ngdesk.module.dao;

import java.util.List;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.commons.annotations.CustomNotEmpty;

import io.swagger.v3.oas.annotations.media.Schema;

public class CommonRequest {

	@JsonProperty("TIER")
	private String tier;

	@JsonProperty("PLUGIN_NAMES")
	private List<String> pluginNames;

	@JsonProperty("COMPANY_ID")
	private String companyId;

	@JsonProperty("COMPANY_SUBDOMAIN")
	private String companySubdomain;

	@JsonProperty("EMAIL_ADDRESS")
	private String emailAddress;

	@JsonProperty("PHONE")
	private Phone phone;

	@JsonProperty("PASSWORD")
	private String password;

	@JsonProperty("FIRST_NAME")
	private String firstName;

	@JsonProperty("LAST_NAME")
	private String lastName;

	@JsonProperty("MODULES")
	private List<String> modules;

	public CommonRequest(String tier, List<String> pluginNames, String companyId, String companySubdomain,
			String emailAddress, Phone phone, String password, String firstName, String lastName,
			List<String> modules) {
		super();
		this.tier = tier;
		this.pluginNames = pluginNames;
		this.companyId = companyId;
		this.companySubdomain = companySubdomain;
		this.emailAddress = emailAddress;
		this.phone = phone;
		this.password = password;
		this.firstName = firstName;
		this.lastName = lastName;
		this.modules = modules;
	}

	public CommonRequest() {
	}

	public String getTier() {
		return tier;
	}

	public void setTier(String tier) {
		this.tier = tier;
	}

	public List<String> getPluginNames() {
		return pluginNames;
	}

	public void setPluginNames(List<String> pluginNames) {
		this.pluginNames = pluginNames;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getCompanySubdomain() {
		return companySubdomain;
	}

	public void setCompanySubdomain(String companySubdomain) {
		this.companySubdomain = companySubdomain;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public Phone getPhone() {
		return phone;
	}

	public void setPhone(Phone phone) {
		this.phone = phone;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public List<String> getModules() {
		return modules;
	}

	public void setModules(List<String> modules) {
		this.modules = modules;
	}

}
