package com.ngdesk.graphql.task.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;

import com.ngdesk.commons.annotations.CustomNotEmpty;

import io.swagger.v3.oas.annotations.media.Schema;

public class Task {

	@Id
	private String id;

	private List<Condition> conditions;

	private String companyId;

	private String moduleId;

	private boolean recurrence;

	private Interval intervals;

	private Date stopDate;

	private List<Action> actions;

	private Date dateCreated;

	private Date dateUpdated;

	private Date lastExecuted;

	private String createdBy;

	private String lastUpdatedBy;

	private String taskName;

	private String taskDescription;

	private Date startDate;

	private String timezone;

	public Task() {
		super();
	}

	public Task(String id, List<Condition> conditions, String companyId, String moduleId, boolean recurrence,
			Interval intervals, Date stopDate, List<Action> actions, Date dateCreated, Date dateUpdated,
			Date lastExecuted, String createdBy, String lastUpdatedBy, String taskName, String taskDescription,
			Date startDate, String timezone) {

		super();
		this.id = id;
		this.conditions = conditions;
		this.companyId = companyId;
		this.moduleId = moduleId;
		this.recurrence = recurrence;
		this.intervals = intervals;
		this.stopDate = stopDate;
		this.actions = actions;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.lastExecuted = lastExecuted;
		this.createdBy = createdBy;
		this.lastUpdatedBy = lastUpdatedBy;
		this.taskName = taskName;
		this.taskDescription = taskDescription;
		this.startDate = startDate;
		this.timezone = timezone;
	}

	public Date getLastExecuted() {
		return lastExecuted;
	}

	public void setLastExecuted(Date lastExecuted) {
		this.lastExecuted = lastExecuted;
	}

	public String getTaskId() {
		return id;
	}

	public void setTaskId(String taskId) {
		this.id = taskId;
	}

	public List<Condition> getConditions() {
		return conditions;
	}

	public void setConditions(List<Condition> conditions) {
		this.conditions = conditions;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

	public boolean isRecurrence() {
		return recurrence;
	}

	public void setRecurrence(boolean recurrence) {
		this.recurrence = recurrence;
	}

	public Interval getIntervals() {
		return intervals;
	}

	public void setIntervals(Interval intervals) {
		this.intervals = intervals;
	}

	public Date getStopDate() {
		return stopDate;
	}

	public void setStopDate(Date stopDate) {
		this.stopDate = stopDate;
	}

	public List<Action> getActions() {
		return actions;
	}

	public void setActions(List<Action> actions) {
		this.actions = actions;
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

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getLastUpdatedBy() {
		return lastUpdatedBy;
	}

	public void setLastUpdatedBy(String lastUpdatedBy) {
		this.lastUpdatedBy = lastUpdatedBy;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public String getTaskDescription() {
		return taskDescription;
	}

	public void setTaskDescription(String taskDescription) {
		this.taskDescription = taskDescription;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

}
