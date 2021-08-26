package com.ngdesk.module.slas.dao;

import java.util.List;

import javax.validation.constraints.Pattern;

import io.swagger.v3.oas.annotations.media.Schema;

public class SLABusinessRules {

	@Schema(description = "Restriction Type")
	@Pattern(regexp = "Day|Week", message = "INVALID_RESTRICTION_TYPE")
	private String restrictionType;

	@Schema(description = "Restrictions")
	private List<SLARestriction> restrictions;

	public SLABusinessRules() {

	}

	public SLABusinessRules(@Pattern(regexp = "Day|Week", message = "INVALID_RESTRICTION_TYPE") String restrictionType,
			List<SLARestriction> restrictions) {
		super();
		this.restrictionType = restrictionType;
		this.restrictions = restrictions;
	}

	public String getRestrictionType() {
		return restrictionType;
	}

	public void setRestrictionType(String restrictionType) {
		this.restrictionType = restrictionType;
	}

	public List<SLARestriction> getRestrictions() {
		return restrictions;
	}

	public void setRestrictions(List<SLARestriction> restrictions) {
		this.restrictions = restrictions;
	}

}
