package com.ngdesk.sam.controllers.dao;

import java.util.Date;

import javax.validation.constraints.Pattern;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.commons.annotations.CustomNotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;

public class SubApp {

	@Schema(description = "Name of the SubApp", required = true)
	@JsonProperty("NAME")
	@Field("NAME")
	@CustomNotNull(message = "NOT_NULL", values = { "SUB_APP_NAME" })
	private String name;

	@Schema(description = "Status of the SubApp", required = true)
	@JsonProperty("STATUS")
	@Field("STATUS")
	@Pattern(regexp = "Online|Offline", message = "INVALID_SUB_APP_STATUS")
	@CustomNotNull(message = "NOT_NULL", values = { "SUB_APP_STATUS" })
	private String status;

	@Schema(description = "Last seen of a SubApp", accessMode = AccessMode.READ_ONLY)
	@JsonProperty("LAST_SEEN")
	@Field("LAST_SEEN")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Date lastSeen;

	@Schema(description = "Version of the controller")
	@JsonProperty("VERSION")
	@Field("VERSION")
	private Integer version;

	public SubApp() {
	}

	public SubApp(String name, @Pattern(regexp = "Online|Offline", message = "INVALID_SUB_APP_STATUS") String status,
			Date lastSeen, Integer version) {
		this.name = name;
		this.status = status;
		this.lastSeen = lastSeen;
		this.version = version;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getLastSeen() {
		return lastSeen;
	}

	public void setLastSeen(Date lastSeen) {
		this.lastSeen = lastSeen;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

}
