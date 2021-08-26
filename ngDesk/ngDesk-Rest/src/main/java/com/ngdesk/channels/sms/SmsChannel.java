package com.ngdesk.channels.sms;

import java.sql.Timestamp;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.workflow.Workflow;

public class SmsChannel {
	@JsonProperty("CHANNEL_ID")
	private String channelId;

	@JsonProperty("NAME")
	@NotEmpty(message = "CHANNEL_NAME_NOT_NULL")
	private String name;

	@JsonProperty("DESCRIPTION")
	@NotNull(message = "CHANNEL_DESCRIPTION_NOT_NULL")
	private String description;

	@JsonProperty("MODULE")
	@NotEmpty(message = "MODULE_MISSING")
	private String module;

	@JsonProperty("PHONE_NUMBER")
	private String phoneNumber;

	@JsonProperty("SID")
	private String sid;

	@JsonProperty("VERIFIED")
	private boolean verified;

	@JsonProperty("WORKFLOW")
	@Valid
	private Workflow workflow;

	@JsonProperty("DATE_CREATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Timestamp dateCreated;

	@JsonProperty("DATE_UPDATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Timestamp dateUpdated;

	@JsonProperty("LAST_UPDATED_BY")
	private String lastUpdatedBy;

	@JsonProperty("CREATED_BY")
	private String createdBy;

	@JsonProperty("WHATSAPP_ENABLED")
	private boolean whatsapp;

	@JsonProperty("WHATSAPP_REQUESTED")
	private boolean request;

	public SmsChannel() {
	}

	public SmsChannel(String channelId, @NotEmpty(message = "CHANNEL_NAME_NOT_NULL") String name,
			@NotNull(message = "CHANNEL_DESCRIPTION_NOT_NULL") String description,
			@NotEmpty(message = "MODULE_MISSING") String module, String phoneNumber, String sid, boolean verified,
			@Valid Workflow workflow, Timestamp dateCreated, Timestamp dateUpdated, String lastUpdatedBy,
			String createdBy, boolean whatsapp, boolean request) {
		super();
		this.channelId = channelId;
		this.name = name;
		this.description = description;
		this.module = module;
		this.phoneNumber = phoneNumber;
		this.sid = sid;
		this.verified = verified;
		this.workflow = workflow;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.lastUpdatedBy = lastUpdatedBy;
		this.createdBy = createdBy;
		this.whatsapp = whatsapp;
		this.request = request;
	}

	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
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

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}

	public boolean isVerified() {
		return verified;
	}

	public void setVerified(boolean verified) {
		this.verified = verified;
	}

	public Workflow getWorkflow() {
		return workflow;
	}

	public void setWorkflow(Workflow workflow) {
		this.workflow = workflow;
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

	public boolean isWhatsapp() {
		return whatsapp;
	}

	public void setWhatsapp(boolean whatsapp) {
		this.whatsapp = whatsapp;
	}

	public boolean isRequest() {
		return request;
	}

	public void setRequest(boolean request) {
		this.request = request;
	}

}
