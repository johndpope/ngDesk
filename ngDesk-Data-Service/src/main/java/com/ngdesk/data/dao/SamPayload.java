package com.ngdesk.data.dao;

import java.util.Map;

public class SamPayload {

	private String companyId;

	private String moduleId;

	private Map<String, Object> entry;

	private String userId;

	private String roleId;

	String userUuid;

	public SamPayload() {

	}

	public SamPayload(String companyId, String moduleId, Map<String, Object> entry, String userId, String roleId,
			String userUuid) {
		super();
		this.companyId = companyId;
		this.moduleId = moduleId;
		this.entry = entry;
		this.userId = userId;
		this.roleId = roleId;
		this.userUuid = userUuid;
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

	public Map<String, Object> getEntry() {
		return entry;
	}

	public void setEntry(Map<String, Object> entry) {
		this.entry = entry;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	public String getUserUuid() {
		return userUuid;
	}

	public void setUserUuid(String userUuid) {
		this.userUuid = userUuid;
	}

}
