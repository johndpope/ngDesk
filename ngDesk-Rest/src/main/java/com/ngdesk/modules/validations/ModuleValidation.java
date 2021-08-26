package com.ngdesk.modules.validations;

import java.util.Date;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ModuleValidation {

	@JsonProperty("VALIDATION_ID")
	private String validationId;

	@JsonProperty("TYPE")
	@NotNull(message = "VALIDATION_TYPE_NOT_NULL")
	@Size(min = 1, message = "VALIDATION_TYPE_NOT_EMPTY")
	@Pattern(regexp = "CREATE|UPDATE|CREATE_OR_UPDATE", message = "INVALID_VALIDATION_TYPE")
	private String type;

	@JsonProperty("NAME")
	@NotNull(message = "VALIDATION_NAME_NOT_NULL")
	@Size(min = 1, message = "VALIDATION_NAME_NOT_EMPTY")
	private String name;

	@JsonProperty("DESCRIPTION")
	@NotNull(message = "VALIDATION_DESCRIPTION_NOT_NULL")
	private String description;

	@JsonProperty("ROLES")
	@NotEmpty(message = "VALIDATION_ROLES_NOT_EMPTY")
	private List<String> roles;

	@JsonProperty("VALIDATIONS")
	@NotEmpty(message = "VALIDATIONS_NOT_NULL")
	@Valid
	private List<Validation> validations;

	@JsonProperty("DATE_CREATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Date dateCreated;

	@JsonProperty("DATE_UPDATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Date dateUpdated;

	@JsonProperty("LAST_UPDATED_BY")
	private String lastUpdatedBy;

	@JsonProperty("CREATED_BY")
	private String createdBy;

	public ModuleValidation() {

	}

	public ModuleValidation(String validationId,
			@NotNull(message = "VALIDATION_TYPE_NOT_NULL") @Size(min = 1, message = "VALIDATION_TYPE_NOT_EMPTY") @Pattern(regexp = "CREATE|UPDATE|CREATE_OR_UPDATE", message = "INVALID_VALIDATION_TYPE") String type,
			@NotNull(message = "VALIDATION_NAME_NOT_NULL") @Size(min = 1, message = "VALIDATION_NAME_NOT_EMPTY") String name,
			@NotNull(message = "VALIDATION_DESCRIPTION_NOT_NULL") String description,
			@NotEmpty(message = "VALIDATION_ROLES_NOT_EMPTY") List<String> roles,
			@NotEmpty(message = "VALIDATIONS_NOT_NULL") @Valid List<Validation> validations, Date dateCreated,
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
