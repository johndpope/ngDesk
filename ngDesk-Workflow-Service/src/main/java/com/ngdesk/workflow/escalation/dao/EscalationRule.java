package com.ngdesk.workflow.escalation.dao;

import javax.validation.Valid;
import javax.validation.constraints.Min;

import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.commons.annotations.CustomNotNull;

import io.swagger.v3.oas.annotations.media.Schema;

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

	@Transient
	@JsonProperty("ESCALATION_ID")
	private String escalationId;

	@Transient
	@JsonProperty("ENTRY_ID")
	private String entryId;

	@Transient
	@JsonProperty("COMPANY_ID")
	private String companyId;

	public EscalationRule() {

	}

	public EscalationRule(Integer minsAfter, Integer order, EscalateTo escalateTo, String escalationId, String entryId,
			String companyId) {
		this.minsAfter = minsAfter;
		this.order = order;
		this.escalateTo = escalateTo;
		this.escalationId = escalationId;
		this.entryId = entryId;
		this.companyId = companyId;
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

	public String getEscalationId() {
		return escalationId;
	}

	public void setEscalationId(String escalationId) {
		this.escalationId = escalationId;
	}

	public String getEntryId() {
		return entryId;
	}

	public void setEntryId(String entryId) {
		this.entryId = entryId;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

}
