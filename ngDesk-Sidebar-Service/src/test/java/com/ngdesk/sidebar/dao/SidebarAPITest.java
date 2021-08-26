package com.ngdesk.sidebar.dao;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.managers.AuthProxy;
import com.ngdesk.sidebar.ApplicationTest;

import brave.Tracer;

@ExtendWith(SpringExtension.class)
@WebMvcTest(SidebarAPI.class)
@ContextConfiguration(classes = { ApplicationTest.class })
public class SidebarAPITest {
	
	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	private SidebarAPI sidebarAPI;
	
	@MockBean
	AuthProxy authProxy;
	
	@MockBean
	Tracer tracer;
	
	private final ObjectMapper mapper = new ObjectMapper();
	
	@Test
	public void testSidebarGetSuccess() throws Exception {
		
		SubMenuItem subMenuItem = new SubMenuItem("test-sub-route", 0, "", false, "", "test", false, "Test sub menu");
		List<SubMenuItem> subMenuItems = new ArrayList<SubMenuItem>();
		subMenuItems.add(subMenuItem);
		MenuItem menuItem = new MenuItem("test-menu", 0, "", false, "", "test", false, "Test menu", subMenuItems);
		List<MenuItem> menuItems = new ArrayList<MenuItem>();
		menuItems.add(menuItem);
		Menu menu = new Menu("test role", menuItems);
		List<Menu> menus = new ArrayList<Menu>();
		menus.add(menu);
		Sidebar sidebar = new Sidebar(menus);
		
		// PREPARE STUB
		given(sidebarAPI.getSidebar()).willReturn(sidebar);
		
		// PERFORM MOCK TEST
		String sidebarString = mockMvc
				.perform(get("/companies/sidebar").contentType(APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		
		Sidebar response = mapper.readValue(sidebarString, Sidebar.class);
	}
	
	@Test
	public void testSidebarPutSuccess() throws Exception {
		SubMenuItem subMenuItem = new SubMenuItem("test-sub-route", 0, "", false, "", "test", false, "Test sub menu");
		List<SubMenuItem> subMenuItems = new ArrayList<SubMenuItem>();
		subMenuItems.add(subMenuItem);
		MenuItem menuItem = new MenuItem("test-menu", 0, "", false, "", "test", false, "Test menu", subMenuItems);
		List<MenuItem> menuItems = new ArrayList<MenuItem>();
		menuItems.add(menuItem);
		Menu menu = new Menu("test role", menuItems);
		List<Menu> menus = new ArrayList<Menu>();
		menus.add(menu);
		Sidebar sidebar = new Sidebar(menus);
		
		// PREPARE STUB
		given(sidebarAPI.putSidebar(any(Sidebar.class))).willReturn(sidebar);
		
		// PERFORM MOCK TEST
		String sidebarString = mockMvc
				.perform(put("/companies/sidebar").content(mapper.writeValueAsString(sidebar))
						.contentType(APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		
		Sidebar response = mapper.readValue(sidebarString, Sidebar.class);
	}
	
	@Test
	public void testSidebarPutRoleNotEmpty() throws Exception {
		SubMenuItem subMenuItem = new SubMenuItem("test-sub-route", 0, "", false, "", "test", false, "Test sub menu");
		List<SubMenuItem> subMenuItems = new ArrayList<SubMenuItem>();
		subMenuItems.add(subMenuItem);
		MenuItem menuItem = new MenuItem("test-menu", 0, "", false, "", "test", false, "Test menu", subMenuItems);
		List<MenuItem> menuItems = new ArrayList<MenuItem>();
		menuItems.add(menuItem);
		Menu menu = new Menu(null, menuItems);
		List<Menu> menus = new ArrayList<Menu>();
		menus.add(menu);
		Sidebar sidebar = new Sidebar(menus);
		
		// PREPARE STUB
		given(sidebarAPI.putSidebar(any(Sidebar.class))).willReturn(sidebar);
		
		// PERFORM MOCK TEST
		mockMvc.perform(
				put("/companies/sidebar").content(mapper.writeValueAsString(sidebar)).contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	@Test
	public void testSidebarPutMenuItemNameNotEmpty() throws Exception {
		SubMenuItem subMenuItem = new SubMenuItem("test-sub-route", 0, "", false, "", "test", false, "Test sub menu");
		List<SubMenuItem> subMenuItems = new ArrayList<SubMenuItem>();
		subMenuItems.add(subMenuItem);
		MenuItem menuItem = new MenuItem("test-menu", 0, "", false, "", "test", false, null, subMenuItems);
		List<MenuItem> menuItems = new ArrayList<MenuItem>();
		menuItems.add(menuItem);
		Menu menu = new Menu(null, menuItems);
		List<Menu> menus = new ArrayList<Menu>();
		menus.add(menu);
		Sidebar sidebar = new Sidebar(menus);
		
		// PREPARE STUB
		given(sidebarAPI.putSidebar(any(Sidebar.class))).willReturn(sidebar);
		
		// PERFORM MOCK TEST
		mockMvc.perform(
				put("/companies/sidebar").content(mapper.writeValueAsString(sidebar)).contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	@Test
	public void testSidebarPutMenuItemEditableNotNull() throws Exception {
		SubMenuItem subMenuItem = new SubMenuItem("test-sub-route", 0, "", false, "", "test", false, "Test sub menu");
		List<SubMenuItem> subMenuItems = new ArrayList<SubMenuItem>();
		subMenuItems.add(subMenuItem);
		MenuItem menuItem = new MenuItem("test-menu", 0, "", false, "", "test", null, "Test Menu", subMenuItems);
		List<MenuItem> menuItems = new ArrayList<MenuItem>();
		menuItems.add(menuItem);
		Menu menu = new Menu(null, menuItems);
		List<Menu> menus = new ArrayList<Menu>();
		menus.add(menu);
		Sidebar sidebar = new Sidebar(menus);
		
		// PREPARE STUB
		given(sidebarAPI.putSidebar(any(Sidebar.class))).willReturn(sidebar);
		
		// PERFORM MOCK TEST
		mockMvc.perform(
				put("/companies/sidebar").content(mapper.writeValueAsString(sidebar)).contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	@Test
	public void testSidebarPutMenuItemIconNotEmpty() throws Exception {
		SubMenuItem subMenuItem = new SubMenuItem("test-sub-route", 0, "", false, "", "test", false, "Test sub menu");
		List<SubMenuItem> subMenuItems = new ArrayList<SubMenuItem>();
		subMenuItems.add(subMenuItem);
		MenuItem menuItem = new MenuItem("test-menu", 0, "", false, "", null, false, "Test Menu", subMenuItems);
		List<MenuItem> menuItems = new ArrayList<MenuItem>();
		menuItems.add(menuItem);
		Menu menu = new Menu(null, menuItems);
		List<Menu> menus = new ArrayList<Menu>();
		menus.add(menu);
		Sidebar sidebar = new Sidebar(menus);
		
		// PREPARE STUB
		given(sidebarAPI.putSidebar(any(Sidebar.class))).willReturn(sidebar);
		
		// PERFORM MOCK TEST
		mockMvc.perform(
				put("/companies/sidebar").content(mapper.writeValueAsString(sidebar)).contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	@Test
	public void testSidebarPutMenuItemIsModuleNotNull() throws Exception {
		SubMenuItem subMenuItem = new SubMenuItem("test-sub-route", 0, "", false, "", "test", false, "Test sub menu");
		List<SubMenuItem> subMenuItems = new ArrayList<SubMenuItem>();
		subMenuItems.add(subMenuItem);
		MenuItem menuItem = new MenuItem("test-menu", 0, "", null, "", "test", false, "Test Menu", subMenuItems);
		List<MenuItem> menuItems = new ArrayList<MenuItem>();
		menuItems.add(menuItem);
		Menu menu = new Menu(null, menuItems);
		List<Menu> menus = new ArrayList<Menu>();
		menus.add(menu);
		Sidebar sidebar = new Sidebar(menus);
		
		// PREPARE STUB
		given(sidebarAPI.putSidebar(any(Sidebar.class))).willReturn(sidebar);
		
		// PERFORM MOCK TEST
		mockMvc.perform(
				put("/companies/sidebar").content(mapper.writeValueAsString(sidebar)).contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	@Test
	public void testSidebarPutMenuItemOrderNotNull() throws Exception {
		SubMenuItem subMenuItem = new SubMenuItem("test-sub-route", 0, "", false, "", "test", false, "Test sub menu");
		List<SubMenuItem> subMenuItems = new ArrayList<SubMenuItem>();
		subMenuItems.add(subMenuItem);
		MenuItem menuItem = new MenuItem("test-menu", null, "", false, "", "test", false, "Test Menu", subMenuItems);
		List<MenuItem> menuItems = new ArrayList<MenuItem>();
		menuItems.add(menuItem);
		Menu menu = new Menu(null, menuItems);
		List<Menu> menus = new ArrayList<Menu>();
		menus.add(menu);
		Sidebar sidebar = new Sidebar(menus);
		
		// PREPARE STUB
		given(sidebarAPI.putSidebar(any(Sidebar.class))).willReturn(sidebar);
		
		// PERFORM MOCK TEST
		mockMvc.perform(
				put("/companies/sidebar").content(mapper.writeValueAsString(sidebar)).contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

}
