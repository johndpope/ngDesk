package com.ngdesk.module.slas.dao;

import java.util.Date;

public class SLAInstance {
	
	private String slaId;

	private String moduleId;

	private String companyId;

	private String dataId;

	private Date slaTimeInfo;

	private Integer numberOfExecutions;

	public SLAInstance() {
		super();
	}

	public SLAInstance(String slaId, String moduleId, String companyId, String dataId, Date slaTimeInfo,
			Integer numberOfExecutions) {
		super();
		this.slaId = slaId;
		this.moduleId = moduleId;
		this.companyId = companyId;
		this.dataId = dataId;
		this.slaTimeInfo = slaTimeInfo;
		this.numberOfExecutions = numberOfExecutions;
	}

	public String getSlaId() {
		return slaId;
	}

	public void setSlaId(String slaId) {
		this.slaId = slaId;
	}

	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getDataId() {
		return dataId;
	}

	public void setDataId(String dataId) {
		this.dataId = dataId;
	}

	public Date getSlaTimeInfo() {
		return slaTimeInfo;
	}

	public void setSlaTimeInfo(Date slaTimeInfo) {
		this.slaTimeInfo = slaTimeInfo;
	}

	public Integer getNumberOfExecutions() {
		return numberOfExecutions;
	}

	public void setNumberOfExecutions(Integer numberOfExecutions) {
		this.numberOfExecutions = numberOfExecutions;
	}

}
