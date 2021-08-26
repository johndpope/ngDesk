package com.ngdesk.workflow.approval.dao;

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
	
	private String nodeId;

	private String moduleId;

	private String workflowId;

	public Approval() {
		super();
	}

	public Approval(String approvalId, String status, List<DeniedBy> deniedBy, List<String> approvedBy,
			List<String> approvers, List<String> teamsWhoCanApprove, String companyId, String dataId,String nodeId, String moduleId,
			String workflowId) {
		super();
		this.approvalId = approvalId;
		this.status = status;
		this.deniedBy = deniedBy;
		this.approvedBy = approvedBy;
		this.approvers = approvers;
		this.teamsWhoCanApprove = teamsWhoCanApprove;
		this.companyId = companyId;
		this.dataId = dataId;
		this.nodeId=nodeId;
		this.moduleId = moduleId;
		this.workflowId = workflowId;
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

	public List<String> getteamsWhoCanApprove() {
		return teamsWhoCanApprove;
	}

	public void setteamsWhoCanApprove(List<String> teamsWhoCanApprove) {
		this.teamsWhoCanApprove = teamsWhoCanApprove;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	
	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
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

	public List<String> getApprovers() {
		return approvers;
	}

	public void setApprovers(List<String> approvers) {
		this.approvers = approvers;
	}

}
