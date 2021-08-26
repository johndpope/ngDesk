package com.ngdesk.modules.list.mobile.layouts;

import java.util.Date;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.modules.list.layouts.OrderBy;
import com.ngdesk.modules.rules.Condition;

public class ListMobileLayout {

	@JsonProperty("LAYOUT_ID")
	private String listLayoutId;

	@JsonProperty("NAME")
	@NotNull(message = "LISTLAYOUT_NAME_NOT_NULL")
	@Size(min = 1, message = "LISTLAYOUT_NAME_EMPTY")
	private String name;

	@JsonProperty("DESCRIPTION")
	@NotNull(message = "DESCRIPTION_NOT_NULL")
	private String description;

	@JsonProperty("ID")
	private String id;

	@JsonProperty("ROLE")
	@NotNull(message = "ROLE_NOT_NULL")
	private String role;

	@JsonProperty("ORDER_BY")
	@NotNull(message = "ORDER_BY_NOT_NULL")
	@Valid
	private OrderBy orderBy;

	@JsonProperty("FIELDS")
	@NotNull(message = "COLUMN_SHOW_NOT_NULL")
	@Valid
	private List<String> fields;

	@JsonProperty("CONDITIONS")
	@Valid
	private List<Condition> conditions;

	@JsonProperty("IS_DEFAULT")
	private boolean isdefault;

	@JsonProperty("DATE_CREATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Date dateCreated;

	@JsonProperty("DATE_UPDATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Date dateUpdated;

	@JsonProperty("LAST_UPDATED_BY")
	private String lastUpdatedBy;

	@JsonProperty("CREATED_BY")
	private String createdBy;

	public ListMobileLayout() {

	}

	public ListMobileLayout(String listLayoutId,
			@NotNull(message = "LISTLAYOUT_NAME_NOT_NULL") @Size(min = 1, message = "LISTLAYOUT_NAME_EMPTY") String name,
			@NotNull(message = "DESCRIPTION_NOT_NULL") String description, String id,
			@NotNull(message = "ROLE_NOT_NULL") String role,
			@NotNull(message = "ORDER_BY_NOT_NULL") @Valid OrderBy orderBy,
			@NotNull(message = "COLUMN_SHOW_NOT_NULL") @Valid List<String> fields, @Valid List<Condition> conditions,
			boolean isdefault, Date dateCreated, Date dateUpdated, String lastUpdatedBy, String createdBy) {
		super();
		this.listLayoutId = listLayoutId;
		this.name = name;
		this.description = description;
		this.id = id;
		this.role = role;
		this.orderBy = orderBy;
		this.fields = fields;
		this.conditions = conditions;
		this.isdefault = isdefault;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.lastUpdatedBy = lastUpdatedBy;
		this.createdBy = createdBy;
	}

	public String getListLayoutId() {
		return listLayoutId;
	}

	public void setListLayoutId(String listLayoutId) {
		this.listLayoutId = listLayoutId;
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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public OrderBy getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(OrderBy orderBy) {
		this.orderBy = orderBy;
	}

	public List<String> getFields() {
		return fields;
	}

	public void setFields(List<String> fields) {
		this.fields = fields;
	}

	public List<Condition> getConditions() {
		return conditions;
	}

	public void setConditions(List<Condition> conditions) {
		this.conditions = conditions;
	}

	public boolean isIsdefault() {
		return isdefault;
	}

	public void setIsdefault(boolean isdefault) {
		this.isdefault = isdefault;
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

	public String getLastUpdatedBy() {
		return lastUpdatedBy;
	}

	public void setLastUpdatedBy(String lastUpdatedBy) {
		this.lastUpdatedBy = lastUpdatedBy;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

}