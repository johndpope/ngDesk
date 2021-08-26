package com.ngdesk.module.validations.dao;

import java.util.Date;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.commons.annotations.CustomNotEmpty;

public class ModuleValidation {

	// TODO: ADD ALL THE KEYS TO TRANSLATION FILE

	@JsonProperty("VALIDATION_ID")
	private String validationId;

	@JsonProperty("TYPE")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = "TYPE")
	@Pattern(regexp = "CREATE|UPDATE|CREATE_OR_UPDATE", message = "INVALID_VALIDATION_TYPE")
	private String type;

	@JsonProperty("NAME")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = "VALIDATION_NAME")
	private String name;

	@JsonProperty("DESCRIPTION")
	private String description;

	@JsonProperty("ROLES")
	@NotEmpty(message = "VALIDATION_ROLES_NOT_EMPTY")
	private List<String> roles;

	@JsonProperty("VALIDATIONS")
	@NotEmpty(message = "VALIDATIONS_NOT_EMPTY")
	@Valid
	private List<Validation> validations;

	@JsonProperty("DATE_CREATED")
	private Date dateCreated;

	@JsonProperty("DATE_UPDATED")
	private Date dateUpdated;

	@JsonProperty("LAST_UPDATED_BY")
	private String lastUpdatedBy;

	@JsonProperty("CREATED_BY")
	private String createdBy;

	public ModuleValidation() {

	}

	public ModuleValidation(String validationId,
			@Pattern(regexp = "CREATE|UPDATE|CREATE_OR_UPDATE", message = "INVALID_VALIDATION_TYPE") String type,
			String name, String description, @NotEmpty(message = "VALIDATION_ROLES_NOT_EMPTY") List<String> roles,
			@NotEmpty(message = "VALIDATIONS_NOT_EMPTY") @Valid List<Validation> validations, Date dateCreated,
			Date dateUpdated, String lastUpdatedBy, String createdBy) {
		super();
		this.validationId = validationId;
		this.type = type;
		this.name = name;
		this.description = description;
		this.roles = roles;
		this.validations = validations;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
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

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Date getDateUpdated() {
		return dateUpdated;
	}

	public void setDateUpdated(Date dateUpdated) {
		this.dateUpdated = dateUpdated;
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
