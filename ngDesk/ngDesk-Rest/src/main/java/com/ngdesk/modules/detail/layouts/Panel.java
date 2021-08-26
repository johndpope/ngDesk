package com.ngdesk.modules.detail.layouts;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Panel {

	@JsonProperty("SETTINGS")
	@Valid
	private Settings settings;

	@JsonProperty("ID")
	@NotNull(message = "PANEL_NAME_NOT_NULL")
	private String id;

	@JsonProperty("PANEL_NAME")
	private String panelName;

	@JsonProperty("GRIDS")
	@Valid
	private List<List<Grid>> grids;
	
	
	@JsonProperty("DISPLAY_TYPE")
	@Pattern(regexp = "^(Panel|Tab)$", message = "INVALID_DISPLAY_TYPE")
	private String displayType;
	

	public Panel() { }


	public Panel(@Valid Settings settings, @NotNull(message = "PANEL_NAME_NOT_NULL") String id, String panelName,
			@Valid List<List<Grid>> grids,
			@Pattern(regexp = "^(Panel|Tab)$", message = "INVALID_DISPLAY_TYPE") String displayType) {
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
