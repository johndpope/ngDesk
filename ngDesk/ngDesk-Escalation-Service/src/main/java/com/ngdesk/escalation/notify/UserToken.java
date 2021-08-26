package com.ngdesk.escalation.notify;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserToken {

	@Id
	private String id;

	@Field("IOS")
	private List<Token> iosTokens;

	@Field("ANDROID")
	private List<Token> androidTokens;

	@Field("USER_UUID")
	private String userUuid;

	public UserToken() {

	}

	public UserToken(String id, List<Token> iosTokens, List<Token> androidTokens, String userUuid) {
		this.id = id;
		this.iosTokens = iosTokens;
		this.androidTokens = androidTokens;
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

	public String getUserUuid() {
		return userUuid;
	}

	public void setUserUuid(String userUuid) {
		this.userUuid = userUuid;
	}

}
