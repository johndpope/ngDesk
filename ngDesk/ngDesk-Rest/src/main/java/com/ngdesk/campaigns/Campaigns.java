package com.ngdesk.campaigns;

import java.sql.Timestamp;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.tracking.button.ButtonClick;

public class Campaigns {

	@JsonProperty("CAMPAIGN_ID")
	private String campaignId;

	@JsonProperty("NAME")
	@NotNull(message = "CAMPAIGN_NAME_NOT_NULL")
	@Size(min = 1, message = "CAMPAIGN_NAME_NOT_EMPTY")
	private String name;
	
	@JsonProperty("DESCRIPTION")
	private String description;
	
	@JsonProperty("SEND_OPTION")
	@NotNull(message = "SEND_OPTION_NOT_NULL")
	@Size(min = 1, message = "SEND_OPTION_NOT_EMPTY")
	@Pattern(regexp = "Send now|Send later", message = "SEND_OPTION_INVALID")
	private String sendOption;
	
	@JsonProperty("SUBJECT")
	@NotNull(message = "SUBJECT_NOT_NULL")
	@Size(min = 1, message = "SUBJECT_NOT_EMPTY")
	private String subject;

	@JsonProperty("BODY")
	private String body;
	
	@JsonProperty("ROWS")
	@NotNull(message = "ROWS_NOT_NULL")
	@Size(min = 1, message = "ROWS_NOT_EMPTY")
	private List<Row> rows;

	@JsonProperty("CAMPAIGN_TYPE")
	@NotNull(message = "CAMPAIGN_TYPE_NOT_NULL")
	@Size(min = 1, message = "CAMPAIGN_TYPE_NOT_EMPTY")
	@Pattern(regexp = "Plain|Simple|Welcome", message = "CAMPAIGN_TYPE_INVALID")
	private String campaignType;
	
	@JsonProperty("SEND_DATE")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Timestamp sendDate;
	
	@JsonProperty("RECIPIENT_USERS")
	private List<String> recipientUsers;
	
	@JsonProperty("RECIPIENT_LISTS")
	private List<String> recipientLists;
	
	@JsonProperty("PREVIEW_USER")
	private String previewUser;
	
	@JsonProperty("STATUS")
	@Pattern(regexp = "Draft|Processing|Sent|Scheduled", message = "CAMPAIGN_STATUS_INVALID")
	private String status;
	
	@JsonProperty("TRACKING")
	private List<CampaignTracking> tracking;
	
	@JsonProperty("BUTTON_CLICKS")
	private List<ButtonClick> buttonClicks;
	
	@JsonProperty("FOOTER")
	@NotNull(message = "FOOTER_NOT_NULL")
	private Footer footer;
	
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

	public Campaigns() {
	}

	public Campaigns(String campaignId,
			@NotNull(message = "CAMPAIGN_NAME_NOT_NULL") @Size(min = 1, message = "CAMPAIGN_NAME_NOT_EMPTY") String name,
			String description,
			@NotNull(message = "SEND_OPTION_NOT_NULL") @Size(min = 1, message = "SEND_OPTION_NOT_EMPTY") @Pattern(regexp = "Send now|Send later", message = "SEND_OPTION_INVALID") String sendOption,
			@NotNull(message = "SUBJECT_NOT_NULL") @Size(min = 1, message = "SUBJECT_NOT_EMPTY") String subject, String body,
			@NotNull(message = "ROWS_NOT_NULL") @Size(min = 1, message = "ROWS_NOT_EMPTY") List<Row> rows,
			@NotNull(message = "CAMPAIGN_TYPE_NOT_NULL") @Size(min = 1, message = "CAMPAIGN_TYPE_NOT_EMPTY") @Pattern(regexp = "Plain|Simple|Welcome", message = "CAMPAIGN_TYPE_INVALID") String campaignType,
			Timestamp sendDate, List<String> recipientUsers, List<String> recipientLists, String previewUser,
			@Pattern(regexp = "Draft|Processing|Sent|Scheduled", message = "CAMPAIGN_STATUS_INVALID") String status,
			List<CampaignTracking> tracking, List<ButtonClick> buttonClicks,
			@NotNull(message = "FOOTER_NOT_NULL") Footer footer, Timestamp dateCreated, Timestamp dateUpdated,
			String lastUpdatedBy, String createdBy) {
		super();
		this.campaignId = campaignId;
		this.name = name;
		this.description = description;
		this.sendOption = sendOption;
		this.subject = subject;
		this.body = body;
		this.rows = rows;
		this.campaignType = campaignType;
		this.sendDate = sendDate;
		this.recipientUsers = recipientUsers;
		this.recipientLists = recipientLists;
		this.previewUser = previewUser;
		this.status = status;
		this.tracking = tracking;
		this.buttonClicks = buttonClicks;
		this.footer = footer;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.lastUpdatedBy = lastUpdatedBy;
		this.createdBy = createdBy;
	}

	public String getCampaignId() {
		return campaignId;
	}

	public void setCampaignId(String campaignId) {
		this.campaignId = campaignId;
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

	public String getSendOption() {
		return sendOption;
	}

	public void setSendOption(String sendOption) {
		this.sendOption = sendOption;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public List<Row> getRows() {
		return rows;
	}

	public void setRows(List<Row> rows) {
		this.rows = rows;
	}

	public String getCampaignType() {
		return campaignType;
	}

	public void setCampaignType(String campaignType) {
		this.campaignType = campaignType;
	}

	public Timestamp getSendDate() {
		return sendDate;
	}

	public void setSendDate(Timestamp sendDate) {
		this.sendDate = sendDate;
	}

	public List<String> getRecipientUsers() {
		return recipientUsers;
	}

	public void setRecipientUsers(List<String> recipientUsers) {
		this.recipientUsers = recipientUsers;
	}

	public List<String> getRecipientLists() {
		return recipientLists;
	}

	public void setRecipientLists(List<String> recipientLists) {
		this.recipientLists = recipientLists;
	}

	public String getPreviewUser() {
		return previewUser;
	}

	public void setPreviewUser(String previewUser) {
		this.previewUser = previewUser;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<CampaignTracking> getTracking() {
		return tracking;
	}

	public void setTracking(List<CampaignTracking> tracking) {
		this.tracking = tracking;
	}

	public List<ButtonClick> getButtonClicks() {
		return buttonClicks;
	}

	public void setButtonClicks(List<ButtonClick> buttonClicks) {
		this.buttonClicks = buttonClicks;
	}

	public Footer getFooter() {
		return footer;
	}

	public void setFooter(Footer footer) {
		this.footer = footer;
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