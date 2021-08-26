package com.ngdesk.auth.jobs;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HealthResponse {
	
	private String status;
	
	private List<String> groups;
	
	public HealthResponse() {
		
	}

	public HealthResponse(String status, List<String> groups) {
		this.status = status;
		this.groups = groups;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<String> getGroups() {
		return groups;
	}

	public void setGroups(List<String> groups) {
		this.groups = groups;
	}
	
	
}
