package com.ngdesk.workflow.executor.dao;

import java.util.Date;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.workflow.dao.NodeExecutionInfo;

public class WorkflowInstance {

	@JsonProperty("INSTANCE_ID")
	@Id
	private String instanceId;

	@JsonProperty("COMPANY_ID")
	@Field("COMPANY_ID")
	private String companyId;

	@JsonProperty("WORKFLOW_ID")
	@Field("WORKFLOW_ID")
	private String workflowId;

	@JsonProperty("STAGE_ID")
	@Field("STAGE_ID")
	private String stageId;

	@JsonProperty("DATA_ID")
	@Field("DATA_ID")
	private String dataId;

	@JsonProperty("NODE_ID")
	@Field("NODE_ID")
	private String nodeId;

	@JsonProperty("MODULE_ID")
	@Field("MODULE_ID")
	private String moduleId;

	@JsonProperty("DATE_CREATED")
	@Field("DATE_CREATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Date dateCreated;

	@JsonProperty("DATE_UPDATED")
	@Field("DATE_UPDATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Date dateUpdated;

	@JsonProperty("KICKED_OFF_BY")
	@Field("KICKED_OFF_BY")
	private String workflowKickedOffBy;

	@JsonProperty("NODES_EXECUTED")
	@Field("NODES_EXECUTED")
	private Map<String, NodeExecutionInfo> nodesExecuted;

	@JsonProperty("STATUS")
	@Field("STATUS")
	private String status = "WORKFLOW_IN_EXECUTION";

	public WorkflowInstance() {

	}

	public WorkflowInstance(String instanceId, String companyId, String workflowId, String stageId, String dataId,
			String nodeId, String moduleId, Date dateCreated, Date dateUpdated, String workflowKickedOffBy,
			Map<String, NodeExecutionInfo> nodesExecuted, String status) {
		super();
		this.instanceId = instanceId;
		this.companyId = companyId;
		this.workflowId = workflowId;
		this.stageId = stageId;
		this.dataId = dataId;
		this.nodeId = nodeId;
		this.moduleId = moduleId;
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

	public String getWorkflowId() {
		return workflowId;
	}

	public void setWorkflowId(String workflowId) {
		this.workflowId = workflowId;
	}

	public String getStageId() {
		return stageId;
	}

	public void setStageId(String stageId) {
		this.stageId = stageId;
	}

	public String getDataId() {
		return dataId;
	}

	public void setDataId(String dataId) {
		this.dataId = dataId;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
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
