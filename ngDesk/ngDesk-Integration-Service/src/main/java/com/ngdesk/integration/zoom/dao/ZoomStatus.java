package com.ngdesk.integration.zoom.dao;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ZoomStatus {

	@JsonProperty("ZOOM_AUTHENTICATED")
	private Boolean zoomAuthenticated;

	@JsonProperty("ZOOM_URL")
	private String zoomUrl;

	@JsonProperty("ZOOM_USER_INFORMATION")
	private ZoomUserInformation zoomUserInformation;

	public ZoomStatus(Boolean zoomAuthenticated, String zoomUrl, ZoomUserInformation zoomUserInformation) {
		super();
		this.zoomAuthenticated = zoomAuthenticated;
		this.zoomUrl = zoomUrl;
		this.zoomUserInformation = zoomUserInformation;
	}

	public ZoomStatus() {
		super();
	}

	public Boolean getZoomAuthenticated() {
		return zoomAuthenticated;
	}

	public void setZoomAuthenticated(Boolean zoomAuthenticated) {
		this.zoomAuthenticated = zoomAuthenticated;
	}

	public String getZoomUrl() {
		return zoomUrl;
	}

	public void setZoomUrl(String zoomUrl) {
		this.zoomUrl = zoomUrl;
	}

	public ZoomUserInformation getZoomUserInformation() {
		return zoomUserInformation;
	}

	public void setZoomUserInformation(ZoomUserInformation zoomUserInformation) {
		this.zoomUserInformation = zoomUserInformation;
	}

}
