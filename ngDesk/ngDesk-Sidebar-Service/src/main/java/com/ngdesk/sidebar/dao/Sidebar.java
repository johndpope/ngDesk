package com.ngdesk.sidebar.dao;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.commons.annotations.CustomNotNull;

import io.swagger.v3.oas.annotations.media.Schema;

public class Sidebar {
	
	@Schema(description = "Sidebar Menu", required = true)
	@JsonProperty("SIDEBAR_MENU")
	@Field("SIDEBAR_MENU")
	@CustomNotNull(message = "NOT_NULL", values = { "ESCALATION_SIDEBAR_MENU" })
	@Size(min = 1, message = "SIDEBAR_MENU_REQUIRED")
	@Valid
	private List<Menu> sidebarMenu;

	public Sidebar() {

	}

	public Sidebar(@NotEmpty(message = "SIDEBAR_MENU_NOT_NULL")
			@Size(min = 1, message = "SIDEBAR_MENU_REQUIRED") @Valid List<Menu> sidebarMenu) {
		super();
		this.sidebarMenu = sidebarMenu;
	}

	public List<Menu> getSidebarMenu() {
		return sidebarMenu;
	}

	public void setSidebarMenu(List<Menu> sidebarMenu) {
		this.sidebarMenu = sidebarMenu;
	}

}
