package com.ngdesk.data.dao;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GlobalSearchTemplate {

	@JsonProperty("input")
	private String input;

	@JsonProperty("FIELD_NAME")
	private String fieldName;

	@JsonProperty("TEAMS")
	private List<String> teams;

	@JsonProperty("MODULE_ID")
	private String moduleId;

	@JsonProperty("ENTRY_ID")
	private String entryId;

	@JsonProperty("COMPANY_ID")
	private String companyId;

	public GlobalSearchTemplate() {

	}

	public GlobalSearchTemplate(String input, String fieldName, List<String> teams, String moduleId, String entryId,
			String companyId) {
		super();
		this.input = input;
		this.fieldName = fieldName;
		this.teams = teams;
		this.moduleId = moduleId;
		this.entryId = entryId;
		this.companyId = companyId;
	}

	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public List<String> getTeams() {
		return teams;
	}

	public void setTeams(List<String> teams) {
		this.teams = teams;
	}

	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

	public String getEntryId() {
		return entryId;
	}

	public void setEntryId(String entryId) {
		this.entryId = entryId;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

}
