package com.ngdesk.workflow.sendsms.dao;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.workflow.data.dao.BasePhone;

public class ToValue {

	@JsonProperty("DATA_ID")
	private String dataId;

	@JsonProperty("FULL_NAME")
	private String fullName;

	@JsonProperty("PHONE_NUMBER")
	private BasePhone phoneNumber;

	public ToValue() {

	}

	public ToValue(String dataId, String fullName, BasePhone phoneNumber) {
		super();
		this.dataId = dataId;
		this.fullName = fullName;
		this.phoneNumber = phoneNumber;
	}

	public String getDataId() {
		return dataId;
	}

	public void setDataId(String dataId) {
		this.dataId = dataId;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public BasePhone getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(BasePhone phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

}
