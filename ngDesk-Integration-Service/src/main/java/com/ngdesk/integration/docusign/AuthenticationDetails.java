package com.ngdesk.integration.docusign;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthenticationDetails {

	@JsonProperty("access_token")
	@Field("ACESS_TOKEN")
	private String accessToken;

	@JsonProperty("refresh_token")
	@Field("REFRESH_TOKEN")
	private String refreshToken;

	@JsonProperty("token_type")
	@Field("TOKEN_TYPE")
	private String tokenType;

	@JsonProperty("expires_in")
	@Field("EXPIRES_IN")
	private int expiresIn;

	public AuthenticationDetails() {

	}

	public AuthenticationDetails(String accessToken, String refreshToken, String tokenType, int expiresIn) {
		super();
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
		this.tokenType = tokenType;
		this.expiresIn = expiresIn;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public String getTokenType() {
		return tokenType;
	}

	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	}

	public int getExpiresIn() {
		return expiresIn;
	}

	public void setExpiresIn(int expiresIn) {
		this.expiresIn = expiresIn;
	}

}
