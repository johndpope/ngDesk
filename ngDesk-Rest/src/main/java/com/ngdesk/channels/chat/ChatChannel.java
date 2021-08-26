package com.ngdesk.channels.chat;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.channels.chat.triggers.ChatTrigger;
import com.ngdesk.workflow.Workflow;

public class ChatChannel {

	@JsonProperty("NAME")
	@NotNull(message = "CHANNEL_NAME_NOT_NULL")
	@NotBlank(message = "CHANNEL_NAME_NOT_NULL")
	private String name;

	@JsonProperty("DESCRIPTION")
	@NotNull(message = "CHANNEL_DESCRIPTION_NOT_NULL")
	private String description;

	@JsonProperty("SOURCE_TYPE")
	@NotNull(message = "CHANNEL_SOURCE_TYPE_NOT_NULL")
	@Pattern(regexp = "chat", message = "INVALID_SOURCE_TYPE")
	private String sourceType;

	@JsonProperty("TITLE")
	@Size(min = 1, message = "TITLE_REQUIRED")
	private String title;

	@JsonProperty("SUBTITLE")
	@Size(min = 1, message = "SUBTITLE_REQUIRED")
	private String subTitle;

	@JsonProperty("FILE")
	@Size(min = 1, message = "FILE_REQUIRED")
	private String file;

	@JsonProperty("HEADER_COLOR")
	@Size(min = 1, message = "COLOR_REQUIRED")
	private String color;

	@JsonProperty("HEADER_TEXT_COLOR")
	@Size(min = 1, message = "COLOR_REQUIRED")
	private String textColor;
	
	@JsonProperty("SENDER_BUBBLE_COLOR")
	@Size(min = 1, message = "COLOR_REQUIRED")
	private String senderBubbleColor;
	
	@JsonProperty("RECEIVER_BUBBLE_COLOR")
	@Size(min = 1, message = "COLOR_REQUIRED")
	private String receiverBubbleColor;
	
	@JsonProperty("SENDER_TEXT_COLOR")
	@Size(min = 1, message = "COLOR_REQUIRED")
	private String senderTextColor;
	
	@JsonProperty("RECEIVER_TEXT_COLOR")
	@Size(min = 1, message = "COLOR_REQUIRED")
	private String receiverTextColor;
	
	@JsonProperty("CHAT_TRIGGERS")
	@Valid
	private List<ChatTrigger> triggers = new ArrayList<ChatTrigger>();

	@JsonProperty("WORKFLOW")
	@Valid
	private Workflow workflow;

	@JsonProperty("SETTINGS")
	@Valid
	private ChatChannelSettings settings;

	@JsonProperty("CHANNEL_ID")
	private String channelId;

	@JsonProperty("DATE_CREATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Timestamp dateCreated;

	@JsonProperty("DATE_UPDATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Timestamp dateUpdated;

	@JsonProperty("LAST_UPDATED_BY")
	private String lastUpdated;

	@JsonProperty("MODULE")
	private String moduleId;

	@JsonProperty("CHAT_PROMPTS")
	@NotNull(message = "CHAT_PROMPT_NOT_NULL")
	private List<ChatPrompt> chatPrompt = new ArrayList<ChatPrompt>();

	public ChatChannel() {

	}

	public ChatChannel(
			@NotNull(message = "CHANNEL_NAME_NOT_NULL") @NotBlank(message = "CHANNEL_NAME_NOT_NULL") String name,
			@NotNull(message = "CHANNEL_DESCRIPTION_NOT_NULL") String description,
			@NotNull(message = "CHANNEL_SOURCE_TYPE_NOT_NULL") @Pattern(regexp = "chat", message = "INVALID_SOURCE_TYPE") String sourceType,
			@Size(min = 1, message = "TITLE_REQUIRED") String title,
			@Size(min = 1, message = "SUBTITLE_REQUIRED") String subTitle,
			@Size(min = 1, message = "FILE_REQUIRED") String file,
			@Size(min = 1, message = "COLOR_REQUIRED") String color,
			@Size(min = 1, message = "COLOR_REQUIRED") String textColor,
			@Size(min = 1, message = "COLOR_REQUIRED") String senderBubbleColor,
			@Size(min = 1, message = "COLOR_REQUIRED") String receiverBubbleColor,
			@Size(min = 1, message = "COLOR_REQUIRED") String senderTextColor,
			@Size(min = 1, message = "COLOR_REQUIRED") String receiverTextColor, @Valid List<ChatTrigger> triggers,
			@Valid Workflow workflow, @Valid ChatChannelSettings settings, String channelId, Timestamp dateCreated,
			Timestamp dateUpdated, String lastUpdated, String moduleId,
			@NotNull(message = "CHAT_PROMPT_NOT_NULL") List<ChatPrompt> chatPrompt) {
		super();
		this.name = name;
		this.description = description;
		this.sourceType = sourceType;
		this.title = title;
		this.subTitle = subTitle;
		this.file = file;
		this.color = color;
		this.textColor = textColor;
		this.senderBubbleColor = senderBubbleColor;
		this.receiverBubbleColor = receiverBubbleColor;
		this.senderTextColor = senderTextColor;
		this.receiverTextColor = receiverTextColor;
		this.triggers = triggers;
		this.workflow = workflow;
		this.settings = settings;
		this.channelId = channelId;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.lastUpdated = lastUpdated;
		this.moduleId = moduleId;
		this.chatPrompt = chatPrompt;
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

	public String getSourceType() {
		return sourceType;
	}

	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSubTitle() {
		return subTitle;
	}

	public void setSubTitle(String subTitle) {
		this.subTitle = subTitle;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getTextColor() {
		return textColor;
	}

	public void setTextColor(String textColor) {
		this.textColor = textColor;
	}

	public String getSenderBubbleColor() {
		return senderBubbleColor;
	}

	public void setSenderBubbleColor(String senderBubbleColor) {
		this.senderBubbleColor = senderBubbleColor;
	}

	public String getReceiverBubbleColor() {
		return receiverBubbleColor;
	}

	public void setReceiverBubbleColor(String receiverBubbleColor) {
		this.receiverBubbleColor = receiverBubbleColor;
	}

	public String getSenderTextColor() {
		return senderTextColor;
	}

	public void setSenderTextColor(String senderTextColor) {
		this.senderTextColor = senderTextColor;
	}

	public String getReceiverTextColor() {
		return receiverTextColor;
	}

	public void setReceiverTextColor(String receiverTextColor) {
		this.receiverTextColor = receiverTextColor;
	}

	public List<ChatTrigger> getTriggers() {
		return triggers;
	}

	public void setTriggers(List<ChatTrigger> triggers) {
		this.triggers = triggers;
	}

	public Workflow getWorkflow() {
		return workflow;
	}

	public void setWorkflow(Workflow workflow) {
		this.workflow = workflow;
	}

	public ChatChannelSettings getSettings() {
		return settings;
	}

	public void setSettings(ChatChannelSettings settings) {
		this.settings = settings;
	}

	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
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

	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

	public List<ChatPrompt> getChatPrompt() {
		return chatPrompt;
	}

	public void setChatPrompt(List<ChatPrompt> chatPrompt) {
		this.chatPrompt = chatPrompt;
	}

}
