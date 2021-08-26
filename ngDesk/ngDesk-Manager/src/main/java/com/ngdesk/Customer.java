package com.ngdesk;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Customer {

	@JsonProperty("LAST_SEEN")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Timestamp lastSeen;

	@JsonProperty("STATUS")
	private String status;

	@JsonProperty("CUSTOMER_ID")
	private String customerId;

	@JsonProperty("CUSTOMER_UUID")
	private String customerUUID;

	@JsonProperty("EMAIL_ADDRESS")
	private String emailAddress;

	@JsonProperty("FIRST_NAME")
	private String firstName;

	@JsonProperty("DISABLED")
	private boolean disabled;

	public Timestamp getLastSeen() {
		return lastSeen;
	}

	public void setLastSeen(Timestamp lastSeen) {
		this.lastSeen = lastSeen;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public String getCustomerUUID() {
		return customerUUID;
	}

	public void setCustomerUUID(String customerUUID) {
		this.customerUUID = customerUUID;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

}
