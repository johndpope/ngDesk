package com.ngdesk.graphql.campaigns.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Campaigns {

	@Id
	private String campaignId;
	@Field("NAME")
	private String name;
	@Field("DESCRIPTION")
	private String description;
	@Field("SEND_OPTION")
	private String sendOption;
	@Field("SUBJECT")
	private String subject;
	@Field("BODY")
	private String body;
	@Field("ROWS")
	private List<Row> rows;
	@Field("CAMPAIGN_TYPE")
	private String campaignType;
	@Field("SEND_DATE")
	private Date sendDate;
	@Field("RECIPIENT_USERS")
	private List<String> recipientUsers;
	@Field("RECIPIENT_LIST")
	private List<String> recipientLists;
	@Field("PREVIEW_USER")
	private String previewUser;
	@Field("STATUS")
	private String status;
	@Field("TRACKING")
	private List<CampaignTracking> tracking;
	@Field("BUTTON_CLICKS")
	private List<ButtonClick> buttonClicks;
	@Field("FOOTER")
	private Footer footer;
	@Field("DATE_CREATED")
	private Date dateCreated;
	@Field("DATE_UPDATED")
	private Date dateUpdated;
	@Field("LAST_UPDATED_BY")
	private String lastUpdatedBy;
	@Field("CREATED_BY")
	private String createdBy;

	public Campaigns() {
	}

	public Campaigns(String campaignId, String name, String description, String sendOption, String subject, String body,
			List<Row> rows, String campaignType, Date sendDate, List<String> recipientUsers,
			List<String> recipientLists, String previewUser, String status, List<CampaignTracking> tracking,
			List<ButtonClick> buttonClicks, Footer footer, Date dateCreated, Date dateUpdated, String lastUpdatedBy,
			String createdBy) {
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

	public Date getSendDate() {
		return sendDate;
	}

	public void setSendDate(Date sendDate) {
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
