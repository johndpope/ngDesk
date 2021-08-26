package com.ngdesk.graphql.form.dao;

import java.util.List;

import javax.validation.Valid;

import com.ngdesk.commons.annotations.CustomNotEmpty;

public class FormPanel {

	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "GRIDS" })
	@Valid
	private List<List<Grid>> grids;

	private boolean collapse;

	private String panelDisplayName;

	private String ID;

	public FormPanel() {

	}

	public FormPanel(@Valid List<List<Grid>> grids, boolean collapse, String panelDisplayName, String iD) {
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

	public boolean isCollapse() {
		return collapse;
	}

	public void setCollapse(boolean collapse) {
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
