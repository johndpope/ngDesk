package com.ngdesk.tracking;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ActivityTrack {

	@JsonProperty("ID")
	@NotNull(message = "ID_REQUIRED")
	private String id;

	@JsonProperty("STEPS")
	@NotNull(message = "STEPS_NOT_NULL")
	@Valid
	private Step step;

	public ActivityTrack() {

	}

	public ActivityTrack(String id, @NotNull(message = "STEPS_NOT_NULL") @Valid Step step) {
		super();
		this.id = id;
		this.step = step;
	}

	public Step getStep() {
		return step;
	}

	public void setStep(Step step) {
		this.step = step;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
