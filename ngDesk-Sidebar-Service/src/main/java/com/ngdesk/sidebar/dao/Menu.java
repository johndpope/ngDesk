package com.ngdesk.sidebar.dao;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.commons.annotations.CustomNotEmpty;

import io.swagger.v3.oas.annotations.media.Schema;

public class Menu {
	
	@Schema(description = "Role of the menu", required = true)
	@Field("ROLE")
	@JsonProperty("ROLE")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "SIDEBAR_MENU_ROLE" })
	private String role;
	
	@Schema(description = "Menu items of menu", required = true)
	@JsonProperty("MENU_ITEMS")
	@Field("MENU_ITEMS")
	@Size(min = 1, message = "MENU_ITEMS_REQUIRED")
	@Valid
	private List<MenuItem> menuItems;

	public Menu() {
		
	}

	public Menu(@NotEmpty(message = "ROLE_REQUIRED") String role, 
			@Size(min = 1, message = "MENU_ITEMS_REQUIRED") @Valid List<MenuItem> menuItems) {
		super();
		this.role = role;
		this.menuItems = menuItems;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public List<MenuItem> getMenuItems() {
		return menuItems;
	}

	public void setMenuItems(List<MenuItem> menuItems) {
		this.menuItems = menuItems;
	}

}
