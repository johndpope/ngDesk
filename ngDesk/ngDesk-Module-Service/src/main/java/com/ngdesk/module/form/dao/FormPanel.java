package com.ngdesk.module.form.dao;

import java.util.List;

import javax.validation.Valid;

import com.ngdesk.commons.annotations.CustomNotEmpty;
import com.ngdesk.commons.annotations.CustomNotNull;

import io.swagger.v3.oas.annotations.media.Schema;

public class FormPanel {

	@Valid
	@Schema(description = "Grids")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "GRIDS" })
	private List<List<Grid>> grids;

	@Schema(description = "Is panel collapsable")
	private Boolean collapse;

	@Schema(description = "Panel Name", required = true)
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "PANEL_NAME" })
	private String panelDisplayName;

	@Schema(description = "Panel ID")
	private String ID;

	public FormPanel() {

	}

	public FormPanel(@Valid List<List<Grid>> grids, Boolean collapse, String panelDisplayName, String iD) {
		super();
		this.grids = grids;
		this.collapse = collapse;
		this.panelDisplayName = panelDisplayName;
		ID = iD;
	}

	public List<List<Grid>> getGrids() {
		return grids;
	}

	public void setGrids(List<List<Grid>> grids) {
		this.grids = grids;
	}

	public Boolean getCollapse() {
		return collapse;
	}

	public void setCollapse(Boolean collapse) {
		this.collapse = collapse;
	}

	public String getPanelDisplayName() {
		return panelDisplayName;
	}

	public void setPanelDisplayName(String panelDisplayName) {
		this.panelDisplayName = panelDisplayName;
	}

	public String getID() {
		return ID;
	}

	public void setID(String iD) {
		ID = iD;
	}

}
