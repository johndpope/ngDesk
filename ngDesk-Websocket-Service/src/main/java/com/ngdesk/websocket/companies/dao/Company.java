package com.ngdesk.websocket.companies.dao;

import java.util.ArrayList;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Company {

	@JsonProperty("COMPANY_ID")
	@Id
	private String id;

	@JsonProperty("COMPANY_SUBDOMAIN")
	@Field("COMPANY_SUBDOMAIN")
	private String companySubdomain;

	@JsonProperty("PHONE")
	private Phone phone;

	@JsonProperty("MAX_CHATS_PER_AGENT")
	@Field("MAX_CHATS_PER_AGENT")
	private int maxChatPerAgent = 5;

	@JsonProperty("CHAT_SETTINGS")
	@Field("CHAT_SETTINGS")
	private ChatSettings chatSettings;

	public Company() {

	}

	public Company(String id, String companySubdomain, Phone phone, int maxChatPerAgent, ChatSettings chatSettings) {
		super();
		this.id = id;
		this.companySubdomain = companySubdomain;
		this.phone = phone;
		this.maxChatPerAgent = maxChatPerAgent;
		this.chatSettings = chatSettings;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCompanySubdomain() {
		return companySubdomain;
	}

	public void setCompanySubdomain(String companySubdomain) {
		this.companySubdomain = companySubdomain;
	}

	public Phone getPhone() {
		return phone;
	}

	public void setPhone(Phone phone) {
		this.phone = phone;
	}

	public int getMaxChatPerAgent() {
		return maxChatPerAgent;
	}

	public void setMaxChatPerAgent(int maxChatPerAgent) {
		this.maxChatPerAgent = maxChatPerAgent;
	}

	public ChatSettings getChatSettings() {
		return chatSettings;
	}

	public void setChatSettings(ChatSettings chatSettings) {
		this.chatSettings = chatSettings;
	}

}
