package com.ngdesk.companies;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Sidebar {
	@JsonProperty("SIDEBAR_MENU")
	@NotNull(message = "SIDEBAR_MENU_NOT_NULL")
	@Size(min = 1, message = "SIDEBAR_MENU_NOT_EMPTY")
	@Valid
	private List<Menu> sidebarMenu;

	public Sidebar(
			@NotNull(message = "SIDEBAR_MENU_NOT_NULL") @Size(min = 1, message = "SIDEBAR_MENU_NOT_EMPTY") List<Menu> sidebarMenu) {
		super();
		this.sidebarMenu = sidebarMenu;
	}

	public Sidebar() {
		super();
	}

	public List<Menu> getSidebarMenu() {
		return sidebarMenu;
	}

	public void setSidebarMenu(List<Menu> sidebarMenu) {
		this.sidebarMenu = sidebarMenu;
	}
}
