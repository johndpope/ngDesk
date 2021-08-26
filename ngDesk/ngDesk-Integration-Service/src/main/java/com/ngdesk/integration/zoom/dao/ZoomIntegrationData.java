package com.ngdesk.integration.zoom.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ZoomIntegrationData {

	@JsonProperty("ZOOM_DATA_ID")
	@Id
	private String zoomId;

	@JsonProperty("CODE")
	@Field("CODE")
	private String code;

	@JsonProperty("COMPANY_ID")
	@Field("COMPANY_ID")
	private String companyId;

	@JsonProperty("AUTHENTICATION_DETAILS")
	@Field("AUTHENTICATION_DETAILS")
	private ZoomAuthentication zoomAuthentication;

	@JsonProperty("ZOOM_USER_INFORMATION")
	@Field("ZOOM_USER_INFORMATION")
	private ZoomUserInformation zoomUserInformation;

	@JsonProperty("ZOOM_UNINSTALL")
	@Field("ZOOM_UNINSTALL")
	private Map<String, Object> uninstallZoom;

	@JsonProperty("MEETINGS_SCHEDULED")
	@Field("MEETINGS_SCHEDULED")
	private List<ZoomMeeting> meetingsScheduled;

	@JsonProperty("DATE_CREATED")
	@Field("DATE_CREATED")
	private Date dateCreated;

	@JsonProperty("TOKEN_UPDATED_DATE")
	@Field("TOKEN_UPDATED_DATE")
	private Date tokenUpdatedDate;

	public ZoomIntegrationData() {
		super();
	}

	public ZoomIntegrationData(String zoomId, String code, String companyId, ZoomAuthentication zoomAuthentication,
			ZoomUserInformation zoomUserInformation, Map<String, Object> uninstallZoom,
			List<ZoomMeeting> meetingsScheduled, Date dateCreated, Date tokenUpdatedDate) {
		super();
		this.zoomId = zoomId;
		this.code = code;
		this.companyId = companyId;
		this.zoomAuthentication = zoomAuthentication;
		this.zoomUserInformation = zoomUserInformation;
		this.uninstallZoom = uninstallZoom;
		this.meetingsScheduled = meetingsScheduled;
		this.dateCreated = dateCreated;
		this.tokenUpdatedDate = tokenUpdatedDate;
	}

	public String getZoomId() {
		return zoomId;
	}

	public void setZoomId(String zoomId) {
		this.zoomId = zoomId;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public ZoomAuthentication getZoomAuthentication() {
		return zoomAuthentication;
	}

	public void setZoomAuthentication(ZoomAuthentication zoomAuthentication) {
		this.zoomAuthentication = zoomAuthentication;
	}

	public ZoomUserInformation getZoomUserInformation() {
		return zoomUserInformation;
	}

	public void setZoomUserInformation(ZoomUserInformation zoomUserInformation) {
		this.zoomUserInformation = zoomUserInformation;
	}

	public Map<String, Object> getUninstallZoom() {
		return uninstallZoom;
	}

	public void setUninstallZoom(Map<String, Object> uninstallZoom) {
		this.uninstallZoom = uninstallZoom;
	}

	public List<ZoomMeeting> getMeetingsScheduled() {
		return meetingsScheduled;
	}

	public void setMeetingsScheduled(List<ZoomMeeting> meetingsScheduled) {
		this.meetingsScheduled = meetingsScheduled;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Date getTokenUpdatedDate() {
		return tokenUpdatedDate;
	}

	public void setTokenUpdatedDate(Date tokenUpdatedDate) {
		this.tokenUpdatedDate = tokenUpdatedDate;
	}

}
