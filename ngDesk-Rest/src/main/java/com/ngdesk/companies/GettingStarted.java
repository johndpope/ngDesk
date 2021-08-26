package com.ngdesk.companies;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GettingStarted {

	@JsonProperty("COMPANY_ID")
	private String companyId;

	@JsonProperty("STEP_NAME")
	private String stepName;

	@JsonProperty("STEP_ID")
	private String stepId;

	@JsonProperty("COMPLETED")
	private boolean completed;

	public GettingStarted() {
	}

	public GettingStarted(String companyId, String stepName, String stepId, boolean completed) {
		super();
		this.companyId = companyId;
		this.stepName = stepName;
		this.stepId = stepId;
		this.completed = completed;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getStepName() {
		return stepName;
	}

	public void setStepName(String stepName) {
		this.stepName = stepName;
	}

	public String getStepId() {
		return stepId;
	}

	public void setStepId(String stepId) {
		this.stepId = stepId;
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

}
