package com.ngdesk.data.dao;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkflowPayloadForChat {

	@JsonProperty("PAGE_LOAD")
	private Map<String, Object> pageLoad;

	@JsonProperty("CHAT_NAME")
	private String channelName;

	@JsonProperty("USER_ID")
	private String userId;

	@JsonProperty("MODULE")
	private String moduleId;

	@JsonProperty("COMPANY_ID")
	private String companyId;

	@JsonProperty("DATA_ID")
	private String dataId;

	@JsonProperty("TYPE")
	private String requestType;

	public WorkflowPayloadForChat() {

	}

	public WorkflowPayloadForChat(Map<String, Object> pageLoad, String channelName, String userId, String moduleId,
			String companyId, String dataId, String requestType) {
		super();
		this.pageLoad = pageLoad;
		this.channelName = channelName;
		this.userId = userId;
		this.moduleId = moduleId;
		this.companyId = companyId;
		this.dataId = dataId;
		this.requestType = requestType;
	}

	public Map<String, Object> getPageLoad() {
		return pageLoad;
	}

	public void setPageLoad(Map<String, Object> pageLoad) {
		this.pageLoad = pageLoad;
	}

	public String getChannelName() {
		return channelName;
	}

	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getDataId() {
		return dataId;
	}

	public void setDataId(String dataId) {
		this.dataId = dataId;
	}

	public String getRequestType() {
		return requestType;
	}

	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}

}
