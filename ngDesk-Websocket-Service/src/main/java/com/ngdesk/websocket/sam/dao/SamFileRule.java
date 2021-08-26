package com.ngdesk.websocket.sam.dao;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.commons.annotations.CustomNotEmpty;

public class SamFileRule {
	@Id
	@JsonProperty("RULE_ID")
	private String id;

	@JsonProperty("FILE_NAME")
	@Field("FILE_NAME")
	private String fileName;

	@JsonProperty("FILE_PATH")
	@Field("FILE_PATH")
	private String filePath;

	@JsonProperty("APPLICATION")
	@Field("APPLICATION")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "APPLICATION" })
	private String application;

	@JsonProperty("RULE_CONDITION")
	@Field("RULE_CONDITION")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "RULE_CONDITION" })
	private String ruleCondition;

	@JsonProperty("HASH")
	@Field("HASH")
	private String hash;

	@JsonProperty("COMPANY_ID")
	@Field("COMPANY_ID")
	private String companyId;
 
	SamFileRule() {

	}

	public SamFileRule(String id, String fileName, String filePath, String application, String ruleCondition,
			String hash, String companyId) {
		super();
		this.id = id;
		this.fileName = fileName;
		this.filePath = filePath;
		this.application = application;
		this.ruleCondition = ruleCondition;
		this.hash = hash;
		this.companyId = companyId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public String getRuleCondition() {
		return ruleCondition;
	}

	public void setRuleCondition(String ruleCondition) {
		this.ruleCondition = ruleCondition;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

}
