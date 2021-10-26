package com.ngdesk.escalation.dao;

import java.util.Date;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import org.springframework.data.annotation.Id;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.commons.annotations.CustomNotEmpty;
import com.ngdesk.commons.annotations.CustomNotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;

public class Escalation {

	@Schema(required = false, accessMode = AccessMode.READ_ONLY, description = "Autogenerated Id")
	@Id
	@JsonProperty("ESCALATION_ID")
	private String id;

	@Schema(description = "Name of the escalation", required = true, example = "ngDesk customers")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "ESCALATION_NAME" })
	private String name;

	@Schema(description = "Description of the escalation", required = false, example = "Escalation to handle ngDesk customer requests")
	@CustomNotNull(message = "NOT_NULL", values = { "ESCALATION_DESCRIPTION" })
	private String description;

	@Schema(description = "Escalation Rules", required = true)
	@CustomNotNull(message = "NOT_NULL", values = { "ESCALATION_RULES" })
	@Size(min = 1, message = "RULES_REQUIRED")
	@Size(max = 99, message = "RULES_LIMIT_REACHED")
	@Valid
	private List<EscalationRule> rules;

	@Schema(description = "Date Created", required = false, accessMode = AccessMode.READ_ONLY)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Date dateCreated;

	@Schema(description = "Date Updated", required = false, accessMode = AccessMode.READ_ONLY)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Date dateUpdated;

	@Schema(description = "Last Updated By", required = false, accessMode = AccessMode.READ_ONLY)
	private String lastUpdated;

	@Schema(description = "Created By", required = false, accessMode = AccessMode.READ_ONLY)
	private String createdBy;

	public Escalation() {

	}

	public Escalation(String id, @NotEmpty(message = "ESCALATION_REQUIRED") String name, String description,
			@Size(min = 1, message = "RULES_NOT_EMPTY") @Valid List<EscalationRule> rules, Date dateCreated,
			Date dateUpdated, String lastUpdated, String createdBy) {
		super();
		this.id = id;
		this.name = name;
		this.description = description;
		this.rules = rules;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.lastUpdated = lastUpdated;
		this.createdBy = createdBy;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public List<EscalationRule> getRules() {
		return rules;
	}

	public void setRules(List<EscalationRule> rules) {
		this.rules = rules;
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

	public String getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(String lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

}
