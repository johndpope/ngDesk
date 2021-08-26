package com.ngdesk.graphql.workflow;

import java.util.Date;

import org.springframework.data.mongodb.core.mapping.Field;

public class NodeExecutionInfo {
	@Field("FIRST_EXECUTION_DATE")
	private Date firstExecutionDate;

	@Field("CURRENT_TIMESTAMP")
	private Date currentTimeStamp;

	@Field("NUMBER_OF_EXECUTIONS")
	private int numberOfExecutions;

	public NodeExecutionInfo(Date firstExecutionDate, Date currentTimeStamp, int numberOfExecutions) {
		this.firstExecutionDate = firstExecutionDate;
		this.currentTimeStamp = currentTimeStamp;
		this.numberOfExecutions = numberOfExecutions;
	}

	public Date getFirstExecutionDate() {
		return firstExecutionDate;
	}

	public void setFirstExecutionDate(Date firstExecutionDate) {
		this.firstExecutionDate = firstExecutionDate;
	}

	public Date getCurrentTimeStamp() {
		return currentTimeStamp;
	}

	public void setCurrentTimeStamp(Date currentTimeStamp) {
		this.currentTimeStamp = currentTimeStamp;
	}

	public int getNumberOfExecutions() {
		return numberOfExecutions;
	}

	public void setNumberOfExecutions(int numberOfExecutions) {
		this.numberOfExecutions = numberOfExecutions;
	}

	public NodeExecutionInfo() {
	}

}
