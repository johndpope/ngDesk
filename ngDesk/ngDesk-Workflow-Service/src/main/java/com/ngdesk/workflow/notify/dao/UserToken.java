package com.ngdesk.workflow.notify.dao;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserToken {

	@Id
	@JsonProperty("TOKEN_ID")
	private String id;

	@JsonProperty("IOS")
	@Field("IOS")
	private List<Token> iosTokens;

	@JsonProperty("ANDROID")
	@Field("ANDROID")
	private List<Token> androidTokens;

	@JsonProperty("WEB")
	@Field("WEB")
	private List<Token> webTokens;

	@JsonProperty("USER_UUID")
	@Field("USER_UUID")
	private String userUuid;

	public UserToken() {

	}

	public UserToken(String id, List<Token> iosTokens, List<Token> androidTokens, List<Token> webTokens,
			String userUuid) {
		super();
		this.id = id;
		this.iosTokens = iosTokens;
		this.androidTokens = androidTokens;
		this.webTokens = webTokens;
		this.userUuid = userUuid;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<Token> getIosTokens() {
		return iosTokens;
	}

	public void setIosTokens(List<Token> iosTokens) {
		this.iosTokens = iosTokens;
	}

	public List<Token> getAndroidTokens() {
		return androidTokens;
	}

	public void setAndroidTokens(List<Token> androidTokens) {
		this.androidTokens = androidTokens;
	}

	public List<Token> getWebTokens() {
		return webTokens;
	}

	public void setWebTokens(List<Token> webTokens) {
		this.webTokens = webTokens;
	}

	public String getUserUuid() {
		return userUuid;
	}

	public void setUserUuid(String userUuid) {
		this.userUuid = userUuid;
	}

}
