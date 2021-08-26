package com.ngdesk.graphql.schedules.dao;

import java.util.Date;
import java.util.List;

public class Layer {

	private List<String> users;

	private String rotationType;

	private String startTime;

	private Date startDate;

	private boolean hasRestrictions;

	private String restrictionType;

	private List<Restriction> restrictions;

	public Layer() {

	}

	public Layer(List<String> users, String rotationType, String startTime, Date startDate, boolean hasRestrictions,
			String restrictionType, List<Restriction> restrictions) {
		super();
		this.users = users;
		this.rotationType = rotationType;
		this.startTime = startTime;
		this.startDate = startDate;
		this.hasRestrictions = hasRestrictions;
		this.restrictionType = restrictionType;
		this.restrictions = restrictions;
	}

	public List<String> getUsers() {
		return users;
	}

	public void setUsers(List<String> users) {
		this.users = users;
	}

	public String getRotationType() {
		return rotationType;
	}

	public void setRotationType(String rotationType) {
		this.rotationType = rotationType;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
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

	public List<Restriction> getRestrictions() {
		return restrictions;
	}

	public void setRestrictions(List<Restriction> restrictions) {
		this.restrictions = restrictions;
	}

}
