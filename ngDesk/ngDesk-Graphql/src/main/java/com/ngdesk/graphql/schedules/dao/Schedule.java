package com.ngdesk.graphql.schedules.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;

public class Schedule {

	@Id
	private String id;

	private String name;

	private String description;

	private String timezone;

	private String userOncall;

	private List<Layer> layers;

	private String scheduleId;

	private Date dateCreated;

	private Date dateUpdated;

	private String createdBy;

	private String lastUpdatedBy;

	public Schedule() {

	}

	public Schedule(String id, String name, String description, String timezone, String userOncall, List<Layer> layers,
			String scheduleId, Date dateCreated, Date dateUpdated, String createdBy, String lastUpdatedBy) {
		super();
		this.id = id;
		this.name = name;
		this.description = description;
		this.timezone = timezone;
		this.userOncall = userOncall;
		this.layers = layers;
		this.scheduleId = scheduleId;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.createdBy = createdBy;
		this.lastUpdatedBy = lastUpdatedBy;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public String getUserOncall() {
		return userOncall;
	}

	public void setUserOncall(String userOncall) {
		this.userOncall = userOncall;
	}

	public List<Layer> getLayers() {
		return layers;
	}

	public void setLayers(List<Layer> layers) {
		this.layers = layers;
	}

	public String getScheduleId() {
		return scheduleId;
	}

	public void setScheduleId(String scheduleId) {
		this.scheduleId = scheduleId;
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

}
