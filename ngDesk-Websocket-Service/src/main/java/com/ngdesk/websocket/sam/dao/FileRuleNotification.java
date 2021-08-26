package com.ngdesk.websocket.sam.dao;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FileRuleNotification {

	@JsonProperty("CONTROLLER_ID")
	private String controllerId;

	@JsonProperty("RULE_ID")
	private String ruleId;
	FileRuleNotification(){
		
	}
	public FileRuleNotification(String controllerId, String ruleId) {
		super();
		this.controllerId = controllerId;
		this.ruleId = ruleId;
	}

	public String getControllerId() {
		return controllerId;
	}

	public void setControllerId(String controllerId) {
		this.controllerId = controllerId;
	}

	public String getRuleId() {
		return ruleId;
	}

	public void setRuleId(String ruleId) {
		this.ruleId = ruleId;
	}

}
