package com.ngdesk.discussion;

import java.util.List;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
public class DiscussionMessage {

	@JsonProperty("MESSAGE")
	@NotEmpty(message = "Message is required")
	private String message;

	@JsonProperty("EMAIL_ADDRESS")
	private String emailAddress;

	@JsonProperty("SENDER")
	private Sender sender;

	@JsonProperty("MESSAGE_ID")
	private String messageId;

	@JsonProperty("MODULE_ID")
	private String moduleId;

	@JsonProperty("ENTRY_ID")
	private String entryId;

	@JsonProperty("ATTACHMENTS")
	private List<DiscussionAttachment> attachments;

	@JsonProperty("COMPANY_SUBDOMAIN")
	@NotEmpty(message = "Company Subdomain Required")
	private String subdomain;

	@JsonProperty("WIDGET_ID")
	private String channelId;

	@JsonProperty("SESSION_UUID")
	private String sessionUuid;

	@JsonProperty("MESSAGE_TYPE")
	private String messageType;

	@JsonProperty("TRIGGER_WORKFLOW")
	private boolean triggerWorkflow;

	public DiscussionMessage() {

	}

	public DiscussionMessage(@NotEmpty(message = "Message is required") String message, String emailAddress,
			Sender sender, String messageId, String moduleId, String entryId, List<DiscussionAttachment> attachments,
			@NotEmpty(message = "Company Subdomain Required") String subdomain, String channelId, String sessionUuid,
			String messageType, boolean triggerWorkflow) {
		super();
		this.message = message;
		this.emailAddress = emailAddress;
		this.sender = sender;
		this.messageId = messageId;
		this.moduleId = moduleId;
		this.entryId = entryId;
		this.attachments = attachments;
		this.subdomain = subdomain;
		this.channelId = channelId;
		this.sessionUuid = sessionUuid;
		this.messageType = messageType;
		this.triggerWorkflow = triggerWorkflow;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public Sender getSender() {
		return sender;
	}

	public void setSender(Sender sender) {
		this.sender = sender;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

	public String getEntryId() {
		return entryId;
	}

	public void setEntryId(String entryId) {
		this.entryId = entryId;
	}

	public List<DiscussionAttachment> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<DiscussionAttachment> attachments) {
		this.attachments = attachments;
	}

	public String getSubdomain() {
		return subdomain;
	}

	public void setSubdomain(String subdomain) {
		this.subdomain = subdomain;
	}

	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public String getSessionUuid() {
		return sessionUuid;
	}

	public void setSessionUuid(String sessionUuid) {
		this.sessionUuid = sessionUuid;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public boolean isTriggerWorkflow() {
		return triggerWorkflow;
	}

	public void setTriggerWorkflow(boolean triggerWorkflow) {
		this.triggerWorkflow = triggerWorkflow;
	}

}
