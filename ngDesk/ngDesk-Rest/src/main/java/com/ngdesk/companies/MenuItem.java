package com.ngdesk.companies;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.annotations.ValidMenuItem;

@ValidMenuItem
public class MenuItem {
	@JsonProperty("ROUTE")
	@NotNull(message = "ROUTE_NOT_NULL")
	private String route;

	@JsonProperty("ORDER")
	@NotNull(message = "ORDER_NOT_NULL")
	private int order;

	@JsonProperty("MODULE")
	@NotNull(message = "MODULE_NAME_NOT_NULL")
	private String module;

	@JsonProperty("IS_MODULE")
	@NotNull(message = "IS_MODULE_NOT_NULL")
	private boolean ismodule;

	@JsonProperty("PATH_PARAMETER")
	@NotNull(message = "PATH_PARAMETER_NOT_NULL")
	private String pathParameter;

	@JsonProperty("ICON")
	@NotNull(message = "ICON_NOT_NULL")
	private String icon;

	@JsonProperty("EDITABLE")
	@NotNull(message = "EDITABLE_NOT_NULL")
	private boolean editable;

	@JsonProperty("NAME")
	@NotNull(message = "NAME_NOT_NULL")
	private String name;

	public MenuItem() {
		super();
	}

	public MenuItem(@NotNull(message = "ROUTE_NOT_NULL") String route, @NotNull(message = "ORDER_NOT_NULL") int order,
			@NotNull(message = "MODULE_NAME_NOT_NULL") String module,
			@NotNull(message = "IS_MODULE_NOT_NULL") boolean ismodule,
			@NotNull(message = "PATH_PARAMETER_NOT_NULL") String pathParameter,
			@NotNull(message = "ICON_NOT_NULL") String icon, @NotNull(message = "EDITABLE_NOT_NULL") boolean editable,
			@NotNull(message = "NAME_NOT_NULL") String name) {
		super();
		this.route = route;
		this.order = order;
		this.module = module;
		this.ismodule = ismodule;
		this.pathParameter = pathParameter;
		this.icon = icon;
		this.editable = editable;
		this.name = name;
	}

	public String getRoute() {
		return route;
	}

	public void setRoute(String route) {
		this.route = route;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public boolean isIsmodule() {
		return ismodule;
	}

	public void setIsmodule(boolean ismodule) {
		this.ismodule = ismodule;
	}

	public String getPathParameter() {
		return pathParameter;
	}

	public void setPathParameter(String pathParameter) {
		this.pathParameter = pathParameter;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
