package com.ngdesk.graphql.approval.dao;

import java.util.List;

import org.springframework.data.annotation.Id;

public class Approval {

	@Id
	private String approvalId;

	private String status;

	private List<DeniedBy> deniedBy;

	private List<String> approvedBy;

	private List<String> approvers;

	private List<String> teamsWhoCanApprove;

	private String companyId;

	private String dataId;

	private String moduleId;

	private String workflowId;

	private Boolean displayButton;

	public Approval() {
		super();
	}

	public Approval(String approvalId, String status, List<DeniedBy> deniedBy, List<String> approvedBy,
			List<String> approvers, List<String> teamsWhoCanApprove, String companyId, String dataId, String moduleId,
			String workflowId, Boolean displayButton) {
		super();
		this.approvalId = approvalId;
		this.status = status;
		this.deniedBy = deniedBy;
		this.approvedBy = approvedBy;
		this.approvers = approvers;
		this.teamsWhoCanApprove = teamsWhoCanApprove;
		this.companyId = companyId;
		this.dataId = dataId;
		this.moduleId = moduleId;
		this.workflowId = workflowId;
		this.displayButton = displayButton;
	}

	public String getApprovalId() {
		return approvalId;
	}

	public void setApprovalId(String approvalId) {
		this.approvalId = approvalId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<DeniedBy> getDeniedBy() {
		return deniedBy;
	}

	public void setDeniedBy(List<DeniedBy> deniedBy) {
		this.deniedBy = deniedBy;
	}

	public List<String> getApprovedBy() {
		return approvedBy;
	}

	public void setApprovedBy(List<String> approvedBy) {
		this.approvedBy = approvedBy;
	}

	public List<String> getApprovers() {
		return approvers;
	}

	public void setApprovers(List<String> approvers) {
		this.approvers = approvers;
	}

	public List<String> getTeamsWhoCanApprove() {
		return teamsWhoCanApprove;
	}

	public void setTeamsWhoCanApprove(List<String> teamsWhoCanApprove) {
		this.teamsWhoCanApprove = teamsWhoCanApprove;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
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

	public String getWorkflowId() {
		return workflowId;
	}

	public void setWorkflowId(String workflowId) {
		this.workflowId = workflowId;
	}

	public Boolean getDisplayButton() {
		return displayButton;
	}

	public void setDisplayButton(Boolean displayButton) {
		this.displayButton = displayButton;
	}

}
