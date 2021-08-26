package com.ngdesk.workflow;

import java.sql.Timestamp;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.annotations.ValidWorkflow;

@Component
public class Workflow {

	@JsonProperty("NODES")
	@NotNull(message = "NODES_NOT_NULL")
	@Size(min = 2, message = "ACTION_REQUIRED")
	@Valid
	@ValidWorkflow
	private List<Node> nodes;

	@JsonProperty("DATE_UPDATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Timestamp dateUpdated;

	@JsonProperty("LAST_UPDATED_BY")
	private String lastUpdated;

	public Workflow() {

	}
    public Workflow(
            @NotNull(message = "NODES_NOT_NULL") @Size(min = 2, message = "ACTION_REQUIRED") @Valid List<Node> nodes,
            Timestamp dateUpdated, String lastUpdated) {
        super();
        this.nodes = nodes;
        this.dateUpdated = dateUpdated;
        this.lastUpdated = lastUpdated;
    }

    public List<Node> getNodes() {
		return nodes;
	}

	public void setNodes(List<Node> nodes) {
		this.nodes = nodes;
	}

	public Timestamp getDateUpdated() {
		return dateUpdated;
	}

	public void setDateUpdated(Timestamp dateUpdated) {
		this.dateUpdated = dateUpdated;
	}

	public String getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(String lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

}
