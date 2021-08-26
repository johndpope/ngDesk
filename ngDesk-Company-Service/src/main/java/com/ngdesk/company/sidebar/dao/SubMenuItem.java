package com.ngdesk.company.sidebar.dao;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.commons.annotations.CustomNotEmpty;
import com.ngdesk.commons.annotations.CustomNotNull;

import io.swagger.v3.oas.annotations.media.Schema;

public class SubMenuItem {

	@Schema(description = "Route of the sub menu item", required = true, example = "schedules")
	@Field("ROUTE")
	@JsonProperty("ROUTE")
	@CustomNotNull(message = "NOT_NULL", values = { "SUB_MENU_ITEM_ROUTE" })
	private String route;

	@Schema(description = "Order of the sub menu item", required = true)
	@Field("ORDER")
	@JsonProperty("ORDER")
	@CustomNotNull(message = "NOT_NULL", values = { "SUB_MENU_ITEM_ORDER" })
	private Integer order;

	@Schema(description = "Module of the sub menu item", required = false, example = "Tickets")
	@Field("MODULE")
	@JsonProperty("MODULE")
	@CustomNotNull(message = "NOT_NULL", values = { "SUB_MENU_ITEM_MODULE" })
	private String module;

	@Schema(description = "Is sub item a module", required = true)
	@Field("IS_MODULE")
	@JsonProperty("IS_MODULE")
	@CustomNotNull(message = "NOT_NULL", values = { "SUB_MENU_ITEM_IS_MODULE" })
	private Boolean isModule;

	@Schema(description = "Path parameter of the sub menu item", required = false)
	@Field("PATH_PARAMETER")
	@JsonProperty("PATH_PARAMETER")
	@CustomNotNull(message = "NOT_NULL", values = { "SUB_MENU_ITEM_PATH_PARAMETER" })
	private String pathParameter;

	@Schema(description = "Icon of the sub menu item", required = false)
	@Field("ICON")
	@JsonProperty("ICON")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "SUB_MENU_ITEM_ICON" })
	private String icon;

	@Schema(description = "Is menu editable", required = true)
	@Field("EDITABLE")
	@JsonProperty("EDITABLE")
	@CustomNotNull(message = "NOT_NULL", values = { "SUB_MENU_ITEM_IS_EDITABLE" })
	private Boolean editable;

	@Schema(description = "Name of the sub menu item", required = true, example = "Schedules")
	@Field("NAME")
	@JsonProperty("NAME")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "SUB_MENU_ITEM_NAME" })
	private String name;

	public SubMenuItem() {
		super();
	}

	public SubMenuItem(@NotNull(message = "ROUTE_NOT_NULL") String route,
			@NotNull(message = "ORDER_REQUIRED") Integer order, String module,
			@NotNull(message = "IS_MODULE_REQUIRED") Boolean isModule, String pathParameter,
			@NotEmpty(message = "ICON_REQUIRED") String icon, @NotNull(message = "EDITABLE_REQUIRED") Boolean editable,
			@NotEmpty(message = "NAME_REQUIRED") String name) {
		super();
		this.route = route;
		this.order = order;
		this.module = module;
		this.isModule = isModule;
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

	public Integer getOrder() {
		return order;
	}

	public void setOrder(Integer order) {
		this.order = order;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public Boolean isModule() {
		return isModule;
	}

	public void setModule(Boolean isModule) {
		this.isModule = isModule;
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

	public Boolean getEditable() {
		return editable;
	}

	public void setEditable(Boolean editable) {
		this.editable = editable;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
