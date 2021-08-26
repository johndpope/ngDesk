package com.ngdesk.workflow.dao;

import java.util.List;

import javax.validation.constraints.Pattern;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.commons.annotations.CustomNotEmpty;

public class ApprovalNode extends Node {

	@JsonProperty("APPROVERS")
	@Field("APPROVERS")
	private List<String> approvers;

	@JsonProperty("TEAMS")
	@Field("TEAMS")
	private List<String> teams;

	@JsonProperty("NUMBER_OF_APPROVALS_REQUIRED")
	@Field("NUMBER_OF_APPROVALS_REQUIRED")
	private Integer numberOfApprovalsRequired;

	@JsonProperty("APPROVAL_CONDITION")
	@Field("APPROVAL_CONDITION")
	@Pattern(regexp = "Any Approver|All Approvers|Minimum No. of Approvals")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "APPROVAL_CONDITION" })
	private String approvalCondition;

	@JsonProperty("NOTIFY_USERS_FOR_APPROVAL")
	@Field("NOTIFY_USERS_FOR_APPROVAL")
	private boolean notifyUsersForApproval;

	@JsonProperty("NOTIFY_USERS_AFTER_APPROVAL")
	@Field("NOTIFY_USERS_AFTER_APPROVAL")
	private boolean notifyUsersAfterApproval;

	@JsonProperty("DISABLE_ENTRY")
	@Field("DISABLE_ENTRY")
	private boolean disableEntry;

	public ApprovalNode() {
	}

	public ApprovalNode(List<String> approvers, List<String> teams, Integer numberOfApprovalsRequired,
			@Pattern(regexp = "Any Approver|All Approvers|Minimum No. of Approvals") String approvalCondition,
			boolean notifyUsersForApproval, boolean notifyUsersAfterApproval, boolean disableEntry) {
		super();
		this.approvers = approvers;
		this.teams = teams;
		this.numberOfApprovalsRequired = numberOfApprovalsRequired;
		this.approvalCondition = approvalCondition;
		this.notifyUsersForApproval = notifyUsersForApproval;
		this.notifyUsersAfterApproval = notifyUsersAfterApproval;
		this.disableEntry = disableEntry;
	}

	public List<String> getApprovers() {
		return approvers;
	}

	public List<String> getTeams() {
		return teams;
	}

	public Integer getNumberOfApprovalsRequired() {
		return numberOfApprovalsRequired;
	}

	public String getApprovalCondition() {
		return approvalCondition;
	}

	public boolean isNotifyUsersForApproval() {
		return notifyUsersForApproval;
	}

	public boolean isNotifyUsersAfterApproval() {
		return notifyUsersAfterApproval;
	}

	public void setApprovers(List<String> approvers) {
		this.approvers = approvers;
	}

	public void setTeams(List<String> teams) {
		this.teams = teams;
	}

	public void setNumberOfApprovalsRequired(Integer numberOfApprovalsRequired) {
		this.numberOfApprovalsRequired = numberOfApprovalsRequired;
	}

	public void setApprovalCondition(String approvalCondition) {
		this.approvalCondition = approvalCondition;
	}

	public void setNotifyUsersForApproval(boolean notifyUsersForApproval) {
		this.notifyUsersForApproval = notifyUsersForApproval;
	}

	public void setNotifyUsersAfterApproval(boolean notifyUsersAfterApproval) {
		this.notifyUsersAfterApproval = notifyUsersAfterApproval;
	}

	public boolean isDisableEntry() {
		return disableEntry;
	}

	public void setDisableEntry(boolean disableEntry) {
		this.disableEntry = disableEntry;
	}

}
