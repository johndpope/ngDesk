package com.ngdesk.graphql.escalation.dao;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EscalationRule {

	@JsonProperty("MINS_AFTER")
	@Field("MINS_AFTER")
	private Integer minsAfter;

	@JsonProperty("ORDER")
	@Field("ORDER")
	private Integer order;

	@JsonProperty("ESCALATE_TO")
	@Field("ESCALATE_TO")
	private EscalateTo escalateTo;

	public EscalationRule() {

	}

	public EscalationRule(Integer minsAfter, Integer order, EscalateTo escalateTo) {
		super();
		this.minsAfter = minsAfter;
		this.order = order;
		this.escalateTo = escalateTo;
	}

	public Integer getMinsAfter() {
		return minsAfter;
	}

	public void setMinsAfter(Integer minsAfter) {
		this.minsAfter = minsAfter;
	}

	public Integer getOrder() {
		return order;
	}

	public void setOrder(Integer order) {
		this.order = order;
	}

	public EscalateTo getEscalateTo() {
		return escalateTo;
	}

	public void setEscalateTo(EscalateTo escalateTo) {
		this.escalateTo = escalateTo;
	}

}
