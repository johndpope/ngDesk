package com.ngdesk.companies;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Menu {
	@JsonProperty("ROLE")
	@NotNull(message = "ROLE_NOT_NULL")
	private String role;

	@JsonProperty("MENU_ITEMS")
	@NotNull(message = "MENU_ITEMS_NOT_NULL")
	@Size(min = 1, message = "MENU_ITEMS_NOT_EMPTY")
	@Valid
	private List<MenuItem> menuItems;

	public Menu() {
		super();
	}

	public Menu(@NotNull(message = "ROLE_NOT_NULL") String role,
			@NotNull(message = "MENU_ITEMS_NOT_NULL") @Size(min = 1, message = "MENU_ITEMS_NOT_EMPTY") @Valid List<MenuItem> menuItems) {
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
