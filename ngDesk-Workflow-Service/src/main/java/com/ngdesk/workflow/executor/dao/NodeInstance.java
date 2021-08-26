package com.ngdesk.workflow.executor.dao;

import java.util.Date;
import java.util.Map;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NodeInstance {
	
	@JsonProperty("NODE_ID")
	@Field("NODE_ID")
	private String nodeId;
	
	@JsonProperty("STAGE_ID")
	@Field("STAGE_ID")
	private String stageId;
	
	@JsonProperty("WORKFLOW_ID")
	@Field("WORKFLOW_ID")
	private String workflowId;
	
	@JsonProperty("MODULE_ID")
	@Field("MODULE_ID")
	private String moduleId;
	
	@JsonProperty("COMPANY_ID")
	@Field("COMPANY_ID")
	private String companyId;
	
	@JsonProperty("KICKED_OFF_BY")
	@Field("KICKED_OFF_BY")
	private String kickedOffBy;
	
	@JsonProperty("DATE_CREATED")
	@Field("DATE_CREATED")
	private Date dateCreated;
	
	@JsonProperty("ENTRY")
	@Field("ENTRY")
	private Map<String, Object> entry;
	
	@JsonProperty("OLD_COPY")
	@Field("OLD_COPY")
	private Map<String, Object> oldCopy;
	
	@JsonProperty("WORKFLOW_INSTANCE_ID")
	@Field("WORKFLOW_INSTANCE_ID")
	private String workflowInstanceId;

	public NodeInstance() {

	}

	public NodeInstance(String nodeId, String stageId, String workflowId, String moduleId, String companyId,
			String kickedOffBy, Date dateCreated, Map<String, Object> entry, Map<String, Object> oldCopy,
			String workflowInstanceId) {
		super();
		this.nodeId = nodeId;
		this.stageId = stageId;
		this.workflowId = workflowId;
		this.moduleId = moduleId;
		this.companyId = companyId;
		this.kickedOffBy = kickedOffBy;
		this.dateCreated = dateCreated;
		this.entry = entry;
		this.oldCopy = oldCopy;
		this.workflowInstanceId = workflowInstanceId;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public String getStageId() {
		return stageId;
	}

	public void setStageId(String stageId) {
		this.stageId = stageId;
	}

	public String getWorkflowId() {
		return workflowId;
	}

	public void setWorkflowId(String workflowId) {
		this.workflowId = workflowId;
	}

	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getKickedOffBy() {
		return kickedOffBy;
	}

	public void setKickedOffBy(String kickedOffBy) {
		this.kickedOffBy = kickedOffBy;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
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

	public String getWorkflowInstanceId() {
		return workflowInstanceId;
	}

	public void setWorkflowInstanceId(String workflowInstanceId) {
		this.workflowInstanceId = workflowInstanceId;
	}

}
