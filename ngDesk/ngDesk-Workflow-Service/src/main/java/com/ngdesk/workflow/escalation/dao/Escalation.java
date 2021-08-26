package com.ngdesk.workflow.escalation.dao;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Escalation {

	@Id
	@JsonProperty("ESCALATION_ID")
	private String id;

	@Field("NAME")
	@JsonProperty("NAME")
	private String name;

	@Field("RULES")
	@JsonProperty("RULES")
	private List<EscalationRule> rules;

	public Escalation() {

	}

	public Escalation(String id, String name, List<EscalationRule> rules) {
		this.id = id;
		this.name = name;
		this.rules = rules;
	}

	public List<EscalationRule> getRules() {
		return rules;
	}

	public void setRules(List<EscalationRule> rules) {
		this.rules = rules;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
