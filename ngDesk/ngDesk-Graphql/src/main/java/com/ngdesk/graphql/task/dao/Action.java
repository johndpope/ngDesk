package com.ngdesk.graphql.task.dao;

import java.util.List;

public class Action {

	private String type;
	private String moduleId;
	private List<TaskFields> fields;

	public Action() {

	}

	public Action(String type, String moduleId, List<TaskFields> fields) {
		super();
		this.type = type;
		this.moduleId = moduleId;
		this.fields = fields;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

	public List<TaskFields> getFields() {
		return fields;
	}

	public void setFields(List<TaskFields> fields) {
		this.fields = fields;
	}

}