package com.ngdesk.notifications.dao;

import java.util.Date;
import javax.validation.constraints.NotEmpty;
import com.ngdesk.commons.annotations.CustomNotEmpty;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;
import nonapi.io.github.classgraph.json.Id;

public class Notification {

	@Id
	@Schema(description = "Autogenerated Id")
	private String id;
	@Schema(description = "Company Id")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "COMPANY_ID" })
	private String companyId;

	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "MODULE_ID" })
	@Schema(description = "Module Id")
	private String moduleId;

	@Schema(description = "Data Id")
	private String dataId;

	@Schema(description = "Recipient Id", required = true)
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "RECIPIENT_ID" })
	private String recipientId;

	@Schema(description = "Date Created", required = false, accessMode = AccessMode.READ_ONLY)
	private Date dateCreated;

	@Schema(description = "Date Updated", required = false, accessMode = AccessMode.READ_ONLY)
	private Date dateUpdated;

	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "MESSAGE" })
	@Schema(description = "Notification Message", required = true)
	private String message;

	@Schema(description = "Notification Message Read", required = true)
	private Boolean read;

	public Notification() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Notification(String companyId, String moduleId, String dataId, String recipientId, Date dateCreated,
			Date dateUpdated, Boolean read, @NotEmpty(message = "DAO_VARIABLE_REQUIRED") String message) {
		super();

		this.companyId = companyId;
		this.moduleId = moduleId;
		this.dataId = dataId;
		this.recipientId = recipientId;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.read = read;
		this.message = message;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public String getDataId() {
		return dataId;
	}

	public void setDataId(String dataId) {
		this.dataId = dataId;
	}

	public String getRecipientId() {
		return recipientId;
	}

	public void setRecipientId(String recipientId) {
		this.recipientId = recipientId;
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

	public Boolean getRead() {
		return read;
	}

	public void setRead(Boolean read) {
		this.read = read;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
