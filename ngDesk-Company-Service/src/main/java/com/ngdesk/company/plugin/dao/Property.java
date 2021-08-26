package com.ngdesk.company.plugin.dao;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.commons.annotations.CustomNotEmpty;
import com.ngdesk.commons.annotations.CustomNotNull;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Property {
	@Schema(description = "Max count of the feature", required = true, example = "50")
	@Field("MAX_COUNT")
	@JsonProperty("MAX_COUNT")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "MAX_COUNT"})
	@CustomNotNull(message = "NOT_NULL", values = { "MAX_COUNT"})
	private Integer maxCount;
	
	@Schema(description = "The feature is enabled or disabled", required = false, example = "true")
	@Field("ENABLED")
	@JsonProperty("ENABLED")
	private Boolean enabled;

	public Property() {
		super();
	}

	public Property(Integer maxCount, Boolean enabled) {
		super();
		this.maxCount = maxCount;
		this.enabled = enabled;
	}

	public Integer getMaxCount() {
		return maxCount;
	}

	public void setMaxCount(Integer maxCount) {
		this.maxCount = maxCount;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}
}
