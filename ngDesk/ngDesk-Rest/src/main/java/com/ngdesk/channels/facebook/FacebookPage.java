package com.ngdesk.channels.facebook;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FacebookPage {

	@JsonProperty("PAGE_ID")
	private String pageId;

	@JsonProperty("PAGE_NAME")
	@NotNull(message = "PAGE_NAME_NOT_NULL")
	private String name;

	@JsonProperty("IS_ACTIVE")
	private boolean active;

	@JsonProperty("SUBSCRIBED")
	private boolean subscribed;

	@JsonProperty("FACEBOOK_USER_ID")
	@NotNull(message = "FACEBOOK_ID_NOT_NULL")
	private String facebookUserId;

	public FacebookPage() {
	}

	public FacebookPage(String pageId, @NotNull(message = "PAGE_NAME_NOT_NULL") String name, boolean active,
			boolean subscribed, @NotNull(message = "FACEBOOK_ID_NOT_NULL") String facebookUserId) {
		super();
		this.pageId = pageId;
		this.name = name;
		this.active = active;
		this.subscribed = subscribed;
		this.facebookUserId = facebookUserId;
	}

	public String getPageId() {
		return pageId;
	}

	public void setPageId(String pageId) {
		this.pageId = pageId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isSubscribed() {
		return subscribed;
	}

	public void setSubscribed(boolean subscribed) {
		this.subscribed = subscribed;
	}

	public String getFacebookUserId() {
		return facebookUserId;
	}

	public void setFacebookUserId(String facebookUserId) {
		this.facebookUserId = facebookUserId;
	}

}
