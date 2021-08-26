package com.ngdesk.graphql.workflow;

import java.util.Date;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

public class WorkflowInstance {

	@Id
	private String instanceId;

	@Field("COMPANY_ID")
	private String companyId;

	@Field("WORKFLOW_ID")
	private String workflow;

	@Field("STAGE_ID")
	private String stage;

	@Field("DATA_ID")
	private String dataId;

	@Field("NODE_ID")
	private String node;

	@Field("MODULE_ID")
	private String module;

	@Field("DATE_CREATED")
	private Date dateCreated;

	@Field("DATE_UPDATED")
	private Date dateUpdated;

	@Field("KICKED_OFF_BY")
	private String workflowKickedOffBy;

	@Field("NODES_EXECUTED")
	private Map<String, NodeExecutionInfo> nodesExecuted;

	@Field("STATUS")
	private String status = "WORKFLOW_IN_EXECUTION";

	public WorkflowInstance() {

	}

	public WorkflowInstance(String instanceId, String companyId, String workflow, String stage, String dataId,
			String node, String module, Date dateCreated, Date dateUpdated, String workflowKickedOffBy,
			Map<String, NodeExecutionInfo> nodesExecuted, String status) {
		super();
		this.instanceId = instanceId;
		this.companyId = companyId;
		this.workflow = workflow;
		this.stage = stage;
		this.dataId = dataId;
		this.node = node;
		this.module = module;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.workflowKickedOffBy = workflowKickedOffBy;
		this.nodesExecuted = nodesExecuted;
		this.status = status;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getWorkflow() {
		return workflow;
	}

	public void setWorkflow(String workflow) {
		this.workflow = workflow;
	}

	public String getStage() {
		return stage;
	}

	public void setStage(String stage) {
		this.stage = stage;
	}

	public String getDataId() {
		return dataId;
	}

	public void setDataId(String dataId) {
		this.dataId = dataId;
	}

	public String getNode() {
		return node;
	}

	public void setNode(String node) {
		this.node = node;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Date getDateUpdated() {
		return dateUpdated;
	}

	public void setDateUpdated(Date dateUpdated) {
		this.dateUpdated = dateUpdated;
	}

	public String getWorkflowKickedOffBy() {
		return workflowKickedOffBy;
	}

	public void setWorkflowKickedOffBy(String workflowKickedOffBy) {
		this.workflowKickedOffBy = workflowKickedOffBy;
	}

	public Map<String, NodeExecutionInfo> getNodesExecuted() {
		return nodesExecuted;
	}

	public void setNodesExecuted(Map<String, NodeExecutionInfo> nodesExecuted) {
		this.nodesExecuted = nodesExecuted;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
