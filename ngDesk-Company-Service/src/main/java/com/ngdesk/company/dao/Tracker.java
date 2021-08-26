package com.ngdesk.company.dao;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Tracker {

	@JsonProperty("STEPS")
	@Field("STEPS")
	private Step steps;

	public Tracker() {
	}

	public Tracker(Step steps) {
		this.steps = steps;
	}

	public Step getSteps() {
		return steps;
	}

	public void setSteps(Step steps) {
		this.steps = steps;
	}

}
