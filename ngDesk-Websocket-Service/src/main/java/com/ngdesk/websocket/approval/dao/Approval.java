package com.ngdesk.websocket.approval.dao;

public class Approval {

	private Boolean approved;

	private String dataId;

	private String moduleId;

	private String comments;

	public Approval() {
		super();
	}

	public Boolean getApproved() {
		return approved;
	}

	public void setApproved(Boolean approved) {
		this.approved = approved;
	}

	public String getDataId() {
		return dataId;
	}

	public void setDataId(String dataId) {
		this.dataId = dataId;
	}

	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public Approval(Boolean approved, String dataId, String moduleId, String comments) {
		super();
		this.approved = approved;
		this.dataId = dataId;
		this.moduleId = moduleId;
		this.comments = comments;
	}

}
