package com.ngdesk.global.picklist;

import java.sql.Timestamp;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Picklist {

	@JsonProperty("NAME")
	@NotNull(message = "PICKLIST_NAME_NOT_NULL")
	@Size(min = 1, message = "PICKLIST_NAME_NOT_EMPTY")
	private String name;

	@JsonProperty("DESCRIPTION")
	@NotNull(message = "PICKLIST_DESCRIPTION_NOT_NULL")
	private String description;

	@JsonProperty("VALUES")
	@NotNull(message = "PICKLIST_VALUES_NOT_NULL")
	@Size(min = 1, message = "PICKLIST_VALUES_NOT_EMPTY")
	private List<String> values;

	@JsonProperty("INSERTION_ORDER")
	private boolean insertionOrder;

	@JsonProperty("DATE_CREATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Timestamp dateCreated;

	@JsonProperty("DATE_UPDATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Timestamp dateUpdated;

	@JsonProperty("LAST_UPDATED_BY")
	private String lastUpdated;

	@JsonProperty("CREATED_BY")
	private String createdBy;

	@JsonProperty("PICKLIST_ID")
	private String picklistId;

	public Picklist() {

	}

	public Picklist(
			@NotNull(message = "PICKLIST_NAME_NOT_NULL") @Size(min = 1, message = "PICKLIST_NAME_NOT_EMPTY") String name,
			@NotNull(message = "PICKLIST_DESCRIPTION_NOT_NULL") String description,
			@NotNull(message = "PICKLIST_VALUES_NOT_NULL") @Size(min = 1, message = "PICKLIST_VALUES_NOT_EMPTY") List<String> values,
			boolean insertionOrder, Timestamp dateCreated, Timestamp dateUpdated, String lastUpdated, String createdBy,
			String picklistId) {
		super();
		this.name = name;
		this.description = description;
		this.values = values;
		this.insertionOrder = insertionOrder;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.lastUpdated = lastUpdated;
		this.createdBy = createdBy;
		this.picklistId = picklistId;
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

	public List<String> getValues() {
		return values;
	}

	public void setValues(List<String> values) {
		this.values = values;
	}

	public boolean isInsertionOrder() {
		return insertionOrder;
	}

	public void setInsertionOrder(boolean insertionOrder) {
		this.insertionOrder = insertionOrder;
	}

	public Timestamp getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Timestamp dateCreated) {
		this.dateCreated = dateCreated;
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

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getPicklistId() {
		return picklistId;
	}

	public void setPicklistId(String picklistId) {
		this.picklistId = picklistId;
	}

}
