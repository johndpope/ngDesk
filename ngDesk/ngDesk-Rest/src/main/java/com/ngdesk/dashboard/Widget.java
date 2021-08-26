package com.ngdesk.dashboard;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Widget {

	
	@JsonProperty("WIDGET_ID")
	public String widgetId;
	
	@JsonProperty("POSITION")
	public Position position;
	
	@JsonProperty("TITLE")
	public String title;
	
	@JsonProperty("MODULE")
	private String moduleId;
	
	@JsonProperty("TYPE")
	public String type;
	
	@JsonProperty("LIST_LAYOUT")
	public String listLayout;
	
	@JsonProperty("CATEGORISED_BY")
	public String categorisedBy;
	
	@JsonProperty("REPRESENTED_IN")
	public String representedIn;
	
	@JsonProperty("DATA")
	public HashMap<String, Integer> data;
	
	public Widget() {
	}

	public Widget(String widgetId, Position position, String title, String moduleId, String type, String listLayout,
			String categorisedBy, String representedIn, HashMap<String, Integer> data) {
		super();
		this.widgetId = widgetId;
		this.position = position;
		this.title = title;
		this.moduleId = moduleId;
		this.type = type;
		this.listLayout = listLayout;
		this.categorisedBy = categorisedBy;
		this.representedIn = representedIn;
		this.data = data;
	}

	public String getWidgetId() {
		return widgetId;
	}

	public void setWidgetId(String widgetId) {
		this.widgetId = widgetId;
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getListLayout() {
		return listLayout;
	}

	public void setListLayout(String listLayout) {
		this.listLayout = listLayout;
	}

	public String getCategorisedBy() {
		return categorisedBy;
	}

	public void setCategorisedBy(String categorisedBy) {
		this.categorisedBy = categorisedBy;
	}

	public String getRepresentedIn() {
		return representedIn;
	}

	public void setRepresentedIn(String representedIn) {
		this.representedIn = representedIn;
	}

	public HashMap<String, Integer> getData() {
		return data;
	}

	public void setData(HashMap<String, Integer> data) {
		this.data = data;
	}

	
}
