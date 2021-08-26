package com.ngdesk.data.sla.dao;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SLARedisPayload {

	@JsonProperty("SLA_ID")
	private String slaId;

	@JsonProperty("DATA_ID")
	private String dataId;

	public SLARedisPayload() {

	}

	public SLARedisPayload(String slaId, String dataId) {
		super();
		this.slaId = slaId;
		this.dataId = dataId;
	}

	public String getSlaId() {
		return slaId;
	}

	public void setSlaId(String slaId) {
		this.slaId = slaId;
	}

	public String getDataId() {
		return dataId;
	}

	public void setDataId(String dataId) {
		this.dataId = dataId;
	}

}
