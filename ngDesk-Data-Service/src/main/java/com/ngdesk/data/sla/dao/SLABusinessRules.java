package com.ngdesk.data.sla.dao;

import java.util.List;

public class SLABusinessRules {

	private String restrictionType;

	private List<SLARestriction> restrictions;

	public SLABusinessRules() {

	}

	public SLABusinessRules(String restrictionType, List<SLARestriction> restrictions) {
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
