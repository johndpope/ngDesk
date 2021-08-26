package com.ngdesk.channels.chat;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChatTransferParameters {

	@JsonProperty("SUBDOMAIN")
	private String subdomain;
	@JsonProperty("MODULE_ID")
	private String moduleId;
	@JsonProperty("DATA_ID")
	private String dataId;

	public ChatTransferParameters() {

	}

	public ChatTransferParameters(String subdomain, String moduleId, String dataId) {
		super();
		this.subdomain = subdomain;
		this.moduleId = moduleId;
		this.dataId = dataId;
	}

	public String getSubdomain() {
		return subdomain;
	}

	public void setSubdomain(String subdomain) {
		this.subdomain = subdomain;
	}

	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

	public String getDataId() {
		return dataId;
	}

	public void setDataId(String dataId) {
		this.dataId = dataId;
	}
	
	

}
