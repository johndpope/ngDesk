package com.ngdesk.integration.amazom.aws.dao;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Message {
	@JsonProperty("AlarmName")
	public String alarmName;

	@JsonProperty("AlarmDescription")
	public String alarmDescription;

	@JsonProperty("AWSAccountId")
	public String awsAccountId;

	@JsonProperty("NewStateValue")
	public String newStateValue;

	@JsonProperty("NewStateReason")
	public String newStateReason;

	@JsonProperty("StateChangeTime")
	public String stateChangeTime;

	@JsonProperty("Region")
	public String region;

	@JsonProperty("AlarmArn")
	public String alarmArn;

	@JsonProperty("OldStateValue")
	public String oldStateValue;

	@JsonProperty("Trigger")
	public Map<String, Object> Trigger;

	public Message() {
		super();
	}

	public Message(String alarmName, String alarmDescription, String awsAccountId, String newStateValue,
			String newStateReason, String stateChangeTime, String region, String alarmArn, String oldStateValue,
			Map<String, Object> trigger) {
		super();
		this.alarmName = alarmName;
		this.alarmDescription = alarmDescription;
		this.awsAccountId = awsAccountId;
		this.newStateValue = newStateValue;
		this.newStateReason = newStateReason;
		this.stateChangeTime = stateChangeTime;
		this.region = region;
		this.alarmArn = alarmArn;
		this.oldStateValue = oldStateValue;
		Trigger = trigger;
	}

	public String getAlarmName() {
		return alarmName;
	}

	public void setAlarmName(String alarmName) {
		this.alarmName = alarmName;
	}

	public String getAlarmDescription() {
		return alarmDescription;
	}

	public void setAlarmDescription(String alarmDescription) {
		this.alarmDescription = alarmDescription;
	}

	public String getAwsAccountId() {
		return awsAccountId;
	}

	public void setAwsAccountId(String awsAccountId) {
		this.awsAccountId = awsAccountId;
	}

	public String getNewStateValue() {
		return newStateValue;
	}

	public void setNewStateValue(String newStateValue) {
		this.newStateValue = newStateValue;
	}

	public String getNewStateReason() {
		return newStateReason;
	}

	public void setNewStateReason(String newStateReason) {
		this.newStateReason = newStateReason;
	}

	public String getStateChangeTime() {
		return stateChangeTime;
	}

	public void setStateChangeTime(String stateChangeTime) {
		this.stateChangeTime = stateChangeTime;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getAlarmArn() {
		return alarmArn;
	}

	public void setAlarmArn(String alarmArn) {
		this.alarmArn = alarmArn;
	}

	public String getOldStateValue() {
		return oldStateValue;
	}

	public void setOldStateValue(String oldStateValue) {
		this.oldStateValue = oldStateValue;
	}

	public Map<String, Object> getTrigger() {
		return Trigger;
	}

	public void setTrigger(Map<String, Object> trigger) {
		Trigger = trigger;
	}

}
