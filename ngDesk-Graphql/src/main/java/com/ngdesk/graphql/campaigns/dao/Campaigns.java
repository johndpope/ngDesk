package com.ngdesk.graphql.campaigns.dao;

import java.sql.Timestamp;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.springframework.data.annotation.Id;

public class Campaigns {

	@Id
	private String campaignId;

	private String name;

	private String description;

	private String sendOption;

	private String subject;

	private String body;

	private List<Row> rows;

	private String campaignType;

	private Timestamp sendDate;

	private List<String> recipientUsers;

	private List<String> recipientLists;

	private String previewUser;

	private String status;

	private List<CampaignTracking> tracking;

	private List<ButtonClick> buttonClicks;

	private Footer footer;

	private Timestamp dateCreated;

	private Timestamp dateUpdated;

	private String lastUpdatedBy;

	private String createdBy;

	public Campaigns() {
	}

	public Campaigns(String campaignId,
			@NotNull(message = "CAMPAIGN_NAME_NOT_NULL") @Size(min = 1, message = "CAMPAIGN_NAME_NOT_EMPTY") String name,
			String description,
			@NotNull(message = "SEND_OPTION_NOT_NULL") @Size(min = 1, message = "SEND_OPTION_NOT_EMPTY") @Pattern(regexp = "Send now|Send later", message = "SEND_OPTION_INVALID") String sendOption,
			@NotNull(message = "SUBJECT_NOT_NULL") @Size(min = 1, message = "SUBJECT_NOT_EMPTY") String subject,
			String body, @NotNull(message = "ROWS_NOT_NULL") @Size(min = 1, message = "ROWS_NOT_EMPTY") List<Row> rows,
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
