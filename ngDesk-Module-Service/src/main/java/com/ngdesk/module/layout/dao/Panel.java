package com.ngdesk.module.layout.dao;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Panel {

	@Field("SETTINGS")
	@JsonProperty("SETTINGS")
	private Settings settings;

	@Field("ID")
	@JsonProperty("ID")
	private String id;

	@Field("PANEL_NAME")
	@JsonProperty("PANEL_NAME")
	private String panelName;

	@Field("GRIDS")
	@JsonProperty("GRIDS")
	private List<List<Grid>> grids;

	@Field("DISPLAY_TYPE")
	@JsonProperty("DISPLAY_TYPE")
	private String displayType;

	public Panel() {
	}

	public Panel(Settings settings, String id, String panelName, List<List<Grid>> grids, String displayType) {
		super();
		this.settings = settings;
		this.id = id;
		this.panelName = panelName;
		this.grids = grids;
		this.displayType = displayType;
	}

	public Settings getSettings() {
		return settings;
	}

	public void setSettings(Settings settings) {
		this.settings = settings;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPanelName() {
		return panelName;
	}

	public void setPanelName(String panelName) {
		this.panelName = panelName;
	}

	public List<List<Grid>> getGrids() {
		return grids;
	}

	public void setGrids(List<List<Grid>> grids) {
		this.grids = grids;
	}

	public String getDisplayType() {
		return displayType;
	}

	public void setDisplayType(String displayType) {
		this.displayType = displayType;
	}

}
