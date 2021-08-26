package com.ngdesk.data.dao;

import java.util.List;
import java.util.Map;

import com.ngdesk.workflow.company.dao.Company;
import com.ngdesk.workflow.dao.Workflow;
import com.ngdesk.workflow.module.dao.Module;

public class WorkflowExecutionInstance {

	private Company company;

	private Workflow workflow;

	private String stageId;

	private Map<String, Object> entry;

	private Map<String, Object> oldCopy;

	private String nodeId;

	private Module module;

	private String workflowInstanceId;

	private String userId;

	private boolean onError;

	private String status;

	private Map<String, List<String>> emailSentOut;

	public WorkflowExecutionInstance() {

	}

	public WorkflowExecutionInstance(Company company, Workflow workflow, String stageId, Map<String, Object> entry,
			Map<String, Object> oldCopy, String nodeId, Module module, String workflowInstanceId, String userId,
			boolean onError, String status, Map<String, List<String>> emailSentOut) {
		super();
		this.company = company;
		this.workflow = workflow;
		this.stageId = stageId;
		this.entry = entry;
		this.oldCopy = oldCopy;
		this.nodeId = nodeId;
		this.module = module;
		this.workflowInstanceId = workflowInstanceId;
		this.userId = userId;
		this.onError = onError;
		this.status = status;
		this.emailSentOut = emailSentOut;
	}

	public Company getCompany() {
		return company;
	}

	public void setCompany(Company company) {
		this.company = company;
	}

	public Workflow getWorkflow() {
		return workflow;
	}

	public void setWorkflow(Workflow workflow) {
		this.workflow = workflow;
	}

	public String getStageId() {
		return stageId;
	}

	public void setStageId(String stageId) {
		this.stageId = stageId;
	}

	public Map<String, Object> getEntry() {
		return entry;
	}

	public void setEntry(Map<String, Object> entry) {
		this.entry = entry;
	}

	public Map<String, Object> getOldCopy() {
		return oldCopy;
	}

	public void setOldCopy(Map<String, Object> oldCopy) {
		this.oldCopy = oldCopy;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public Module getModule() {
		return module;
	}

	public void setModule(Module module) {
		this.module = module;
	}

	public String getWorkflowInstanceId() {
		return workflowInstanceId;
	}

	public void setWorkflowInstanceId(String workflowInstanceId) {
		this.workflowInstanceId = workflowInstanceId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public boolean isOnError() {
		return onError;
	}

	public void setOnError(boolean onError) {
		this.onError = onError;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Map<String, List<String>> getEmailSentOut() {
		return emailSentOut;
	}

	public void setEmailSentOut(Map<String, List<String>> emailSentOut) {
		this.emailSentOut = emailSentOut;
	}

}