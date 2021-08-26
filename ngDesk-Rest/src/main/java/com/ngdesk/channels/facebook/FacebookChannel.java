package com.ngdesk.channels.facebook;

import java.sql.Timestamp;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.workflow.Workflow;

public class FacebookChannel {

	@JsonProperty("CHANNEL_ID")
	private String channelId;

	@JsonProperty("NAME")
	@NotEmpty(message = "CHANNEL_NAME_NOT_NULL")
	private String name;

	@JsonProperty("DESCRIPTION")
	@NotNull(message = "CHANNEL_DESCRIPTION_NOT_NULL")
	private String description;

	@JsonProperty("COMPANY_ID")
	private String companyId;

	@JsonProperty("PAGES")
	@Size(max = 15, message = "PAGE_MORE_THAN_FIFTEEN")
	private List<FacebookPage> pages;

	@JsonProperty("MODULE")
	@NotEmpty(message = "MODULE_MISSING")
	private String module;

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

	public FacebookChannel() {
	}

	public FacebookChannel(String channelId, @NotEmpty(message = "CHANNEL_NAME_NOT_NULL") String name,
			@NotNull(message = "CHANNEL_DESCRIPTION_NOT_NULL") String description, String companyId,
			@Size(max = 15, message = "PAGE_MORE_THAN_FIFTEEN") List<FacebookPage> pages,
			@NotEmpty(message = "MODULE_MISSING") String module, @Valid Workflow workflow, Timestamp dateCreated,
			Timestamp dateUpdated, String lastUpdatedBy, String createdBy) {
		super();
		this.channelId = channelId;
		this.name = name;
		this.description = description;
		this.companyId = companyId;
		this.pages = pages;
		this.module = module;
		this.workflow = workflow;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.lastUpdatedBy = lastUpdatedBy;
		this.createdBy = createdBy;
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

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public List<FacebookPage> getPages() {
		return pages;
	}

	public void setPages(List<FacebookPage> pages) {
		this.pages = pages;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
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

}
