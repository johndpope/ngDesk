package com.ngdesk.company.module.dao;


import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;


import com.fasterxml.jackson.annotation.JsonProperty;

public class ModuleValidation {

	@JsonProperty("VALIDATION_ID")
	@Field("VALIDATION_ID")
	private String validationId;

	@JsonProperty("TYPE")
	@Field("TYPE")
	private String type;

	@JsonProperty("NAME")
	@Field("NAME")
	private String name;

	@JsonProperty("DESCRIPTION")
	@Field("DESCRIPTION")
	private String description;

	@JsonProperty("ROLES")
	@Field("ROLES")
	private List<String> roles;

	@JsonProperty("VALIDATIONS")
	@Field("VALIDATIONS")
	private List<Validation> validations;

	@JsonProperty("LAST_UPDATED_BY")
	@Field("LAST_UPDATED_BY")
	private String lastUpdatedBy;

	@JsonProperty("CREATED_BY")
	@Field("CREATED_BY")
	private String createdBy;

	public ModuleValidation() {

	}

	public ModuleValidation(String validationId, String type, String name, String description, List<String> roles,
			List<Validation> validations, String lastUpdatedBy, String createdBy) {
		super();
		this.validationId = validationId;
		this.type = type;
		this.name = name;
		this.description = description;
		this.roles = roles;
		this.validations = validations;
		this.lastUpdatedBy = lastUpdatedBy;
		this.createdBy = createdBy;
	}

	public String getValidationId() {
		return validationId;
	}

	public void setValidationId(String validationId) {
		this.validationId = validationId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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

	public List<String> getRoles() {
		return roles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}

	public List<Validation> getValidations() {
		return validations;
	}

	public void setValidations(List<Validation> validations) {
		this.validations = validations;
	}

	public String getLastUpdatedBy() {
		return lastUpdatedBy;
	}

	public void setLastUpdatedBy(String lastUpdatedBy) {
		this.lastUpdatedBy = lastUpdatedBy;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

}
