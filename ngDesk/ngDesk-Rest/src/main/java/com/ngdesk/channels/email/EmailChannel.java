package com.ngdesk.channels.email;

import java.sql.Timestamp;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Pattern.Flag;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.ngdesk.workflow.Workflow;

public class EmailChannel {
	@JsonProperty("NAME")
	@NotNull(message = "CHANNEL_NAME_NOT_NULL")
	@NotEmpty(message = "CHANNEL_NAME_NOT_NULL")
	private String name;

	@JsonProperty("DESCRIPTION")
	@NotNull(message = "CHANNEL_DESCRIPTION_NOT_NULL")
	private String description;

	@JsonProperty("MODULE")
	@NotEmpty(message = "MODULE_MISSING")
	private String module;

	@JsonProperty("SOURCE_TYPE")
	@NotNull(message = "CHANNEL_SOURCE_TYPE_NOT_NULL")
	@Pattern(regexp = "email", message = "INVALID_SOURCE_TYPE")
	private String sourceType;

	@JsonProperty("WORKFLOW")
	@Valid
	private Workflow workflow;

	@JsonProperty("CHANNEL_ID")
	private String channelId;

	@JsonProperty("EMAIL_ADDRESS")
	@Email(flags = Flag.CASE_INSENSITIVE, message = "EMAIL_INVALID")
	@NotBlank(message = "EMAIL_INVALID")
	private String emailAddress;

	@JsonProperty("TYPE")
	@NotEmpty(message = "TYPE_MISSING")
	@Pattern(regexp = "Internal|External", message = "INVALID_EMAIL_TYPE")
	private String type;

	@JsonProperty("IS_VERIFIED")
	private boolean verified;

	@JsonProperty("DATE_CREATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Timestamp dateCreated;

	@JsonProperty("DATE_UPDATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Timestamp dateUpdated;

	@JsonProperty("LAST_UPDATED_BY")
	private String lastUpdated;

	@JsonProperty("CREATE_MAPPING")
	private EmailMapping createMapping;

	@JsonProperty("UPDATE_MAPPING")
	private EmailMapping updateMapping;

	public EmailChannel() {

	}

	public EmailChannel(
			@NotNull(message = "CHANNEL_NAME_NOT_NULL") @NotEmpty(message = "CHANNEL_NAME_NOT_NULL") String name,
			@NotNull(message = "CHANNEL_DESCRIPTION_NOT_NULL") String description,
			@NotEmpty(message = "MODULE_MISSING") String module,
			@NotNull(message = "CHANNEL_SOURCE_TYPE_NOT_NULL") @Pattern(regexp = "email", message = "INVALID_SOURCE_TYPE") String sourceType,
			@Valid Workflow workflow, String channelId,
			@Email(flags = Flag.CASE_INSENSITIVE, message = "EMAIL_INVALID") @NotBlank(message = "EMAIL_INVALID") String emailAddress,
			@NotEmpty(message = "TYPE_MISSING") @Pattern(regexp = "Internal|External", message = "INVALID_EMAIL_TYPE") String type,
			boolean verified, Timestamp dateCreated, Timestamp dateUpdated, String lastUpdated,
			EmailMapping createMapping, EmailMapping updateMapping) {
		super();
		this.name = name;
		this.description = description;
		this.module = module;
		this.sourceType = sourceType;
		this.workflow = workflow;
		this.channelId = channelId;
		this.emailAddress = emailAddress;
		this.type = type;
		this.verified = verified;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.lastUpdated = lastUpdated;
		this.createMapping = createMapping;
		this.updateMapping = updateMapping;
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

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public String getSourceType() {
		return sourceType;
	}

	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}

	public Workflow getWorkflow() {
		return workflow;
	}

	public void setWorkflow(Workflow workflow) {
		this.workflow = workflow;
	}

	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isVerified() {
		return verified;
	}

	public void setVerified(boolean verified) {
		this.verified = verified;
	}

	public Timestamp getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Timestamp dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Timestamp getDateUpdated() {
		return dateUpdated;
	}

	public void setDateUpdated(Timestamp dateUpdated) {
		this.dateUpdated = dateUpdated;
	}

	public String getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(String lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public EmailMapping getCreateMapping() {
		return createMapping;
	}

	public void setCreateMapping(EmailMapping createMapping) {
		this.createMapping = createMapping;
	}

	public EmailMapping getUpdateMapping() {
		return updateMapping;
	}

	public void setUpdateMapping(EmailMapping updateMapping) {
		this.updateMapping = updateMapping;
	}


}
