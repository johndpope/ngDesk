package com.ngdesk.auth.dao;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SubscriptionStatus {

	@JsonProperty("SUBSCRIPTION")
	private String subscription;

	public SubscriptionStatus() {

	}

	public SubscriptionStatus(String subscription) {
		super();
		this.subscription = subscription;
	}

	public String getSubscription() {
		return subscription;
	}

	public void setSubscription(String subscription) {
		this.subscription = subscription;
	}

}
