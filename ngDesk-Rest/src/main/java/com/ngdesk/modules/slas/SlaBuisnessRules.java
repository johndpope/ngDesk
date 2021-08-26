package com.ngdesk.modules.slas;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.schedules.Restriction;

import io.swagger.annotations.ApiModelProperty;

public class SlaBuisnessRules {
	
	@ApiModelProperty(notes = "Expects a boolean that represents whether it has restrictions or not")
	@JsonProperty("HAS_RESTRICTIONS")
	@NotNull(message = "HAS_RESTRICTIONS_NOT_NULL")
	private boolean hasRestrictions;

	@ApiModelProperty(notes = "Represents whether the restriction needs to be on specific times of the day or specific times of the week")
	@JsonProperty("RESTRICTION_TYPE")
	@Pattern(regexp = "Day|Week", message = "INVALID_RESTRICTION_TYPE")
	private String restrictionType;

	@ApiModelProperty(notes = "Expects list of Restriction details having start time, end time, start day, end day")
	@JsonProperty("SLA_RESTRICTIONS")
	@NotNull(message = "RESTRICTIONS_NOT_NULL")
	@Valid
	private List<SlaRestriction> restrictions;
	
	public SlaBuisnessRules() {}
	
	public SlaBuisnessRules(@NotNull(message = "HAS_RESTRICTIONS_NOT_NULL") boolean hasRestrictions,
			@Pattern(regexp = "Day|Week", message = "INVALID_RESTRICTION_TYPE") String restrictionType,
			@NotNull(message = "RESTRICTIONS_NOT_NULL") @Valid List<SlaRestriction> restrictions) {
		super();
		this.hasRestrictions = hasRestrictions;
		this.restrictionType = restrictionType;
		this.restrictions = restrictions;
	}

	public boolean isHasRestrictions() {
		return hasRestrictions;
	}

	public void setHasRestrictions(boolean hasRestrictions) {
		this.hasRestrictions = hasRestrictions;
	}

	public String getRestrictionType() {
		return restrictionType;
	}

	public void setRestrictionType(String restrictionType) {
		this.restrictionType = restrictionType;
	}

	public List<SlaRestriction> getRestrictions() {
		return restrictions;
	}

	public void setRestrictions(List<SlaRestriction> restrictions) {
		this.restrictions = restrictions;
	}	
}
