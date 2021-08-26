package com.ngdesk.graphql.chat.channel.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

public class ChatChannelWorkflow {

	@Field("TYPE")
	private String type;

	@Field("NAME")
	private String name;

	@Field("DESCRIPTION")
	private String description;

	@Field("CONDITIONS")
	List<Condition> conditions;

	@Field("STAGES")
	private List<ChatChannelStage> stages;

	@Field("ORDER")
	private Integer order;

	public ChatChannelWorkflow() {

	}

	public ChatChannelWorkflow(String type, String name, String description, List<Condition> conditions,
			List<ChatChannelStage> stages, Integer order) {
		super();
		this.type = type;
		this.name = name;
		this.description = description;
		this.conditions = conditions;
		this.stages = stages;
		this.order = order;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<Condition> getConditions() {
		return conditions;
	}

	public void setConditions(List<Condition> conditions) {
		this.conditions = conditions;
	}

	public List<ChatChannelStage> getStages() {
		return stages;
	}

	public void setStages(List<ChatChannelStage> stages) {
		this.stages = stages;
	}

	public Integer getOrder() {
		return order;
	}

	public void setOrder(Integer order) {
		this.order = order;
	}

}
