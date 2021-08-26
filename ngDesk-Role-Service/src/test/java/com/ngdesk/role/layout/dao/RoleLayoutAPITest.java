package com.ngdesk.role.layout.dao;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.managers.AuthProxy;
import com.ngdesk.role.ApplicationTest;

import brave.Tracer;

@ExtendWith(SpringExtension.class)
@WebMvcTest(RoleLayoutAPI.class)
@ContextConfiguration(classes = { ApplicationTest.class })
public class RoleLayoutAPITest {
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private RoleLayoutAPI roleLayoutApi;

	@MockBean
	AuthProxy authProxy;

	@MockBean
	Tracer tracer;

	private final ObjectMapper mapper = new ObjectMapper();

	@DisplayName("Test to determine success of post")
	@Test
	public void testRoleLayoutPostSuccess() throws Exception {

		List<String> columnsShow = new ArrayList<String>();
		columnsShow.add("column1");
		columnsShow.add("Column2");
		List<Condition> conditions = new ArrayList<Condition>();
		OrderBy orderBy = new OrderBy("column1", "Asc");
		Tab layoutModule = new Tab("tab id","Module id", columnsShow, orderBy, conditions);
		List<Tab> modules = new ArrayList<Tab>();
		modules.add(layoutModule);

		RoleLayout roleLayout = new RoleLayout("Test", "company id", "Admin", "Test layout", "Test desc", false,
				modules);

		// PREPARE STUB
		given(roleLayoutApi.postRoleLayout(any(RoleLayout.class))).willReturn(roleLayout);

		// PERFORM MOCK TEST
		String roleLayoutString = mockMvc
				.perform(post("/role_layout").content(mapper.writeValueAsString(roleLayout))
						.contentType(APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

		RoleLayout response = mapper.readValue(roleLayoutString, RoleLayout.class);
	}

	@DisplayName("Test to failure of post incase of empty name")
	@Test
	public void testRoleLayoutPostNameEmptyError() throws Exception {

		List<String> columnsShow = new ArrayList<String>();
		columnsShow.add("column1");
		columnsShow.add("Column2");
		List<Condition> conditions = new ArrayList<Condition>();
		OrderBy orderBy = new OrderBy("column1", "Asc");
		Tab layoutModule = new Tab("tab id","Module id", columnsShow, orderBy, conditions);
		List<Tab> modules = new ArrayList<Tab>();
		modules.add(layoutModule);

		RoleLayout roleLayout = new RoleLayout("", "company id", "Admin", "", "Test desc", true, modules);

		// PREPARE STUB
		given(roleLayoutApi.postRoleLayout(any(RoleLayout.class))).willReturn(roleLayout);

		mockMvc.perform(
				post("/role_layout").content(mapper.writeValueAsString(roleLayout)).contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest());

	}

	@DisplayName("Test to failure of post incase of null moudles")
	@Test
	public void testRoleLayoutModuleNotNullError() throws Exception {
		RoleLayout roleLayout = new RoleLayout("Test", "company id", "Admin", "Test layout", "Test desc", true, null);

		given(roleLayoutApi.postRoleLayout(any(RoleLayout.class))).willReturn(roleLayout);

		mockMvc.perform(
				post("/role_layout").content(mapper.writeValueAsString(roleLayout)).contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@DisplayName("Test to failure of post incase of invalid order by value")
	@Test
	public void testRoleLayoutOrderByValueError() throws Exception {

		List<String> columnsShow = new ArrayList<String>();
		columnsShow.add("column1");
		columnsShow.add("Column2");
		List<Condition> conditions = new ArrayList<Condition>();
		OrderBy orderBy = new OrderBy("column1", "Test1");
		Tab layoutModule = new Tab("tab id","Module id", columnsShow, orderBy, conditions);
		List<Tab> modules = new ArrayList<Tab>();
		modules.add(layoutModule);

		RoleLayout roleLayout = new RoleLayout("", "company id", "Admin", "Test Layout", "Test desc", true, modules);

		// PREPARE STUB
		given(roleLayoutApi.postRoleLayout(any(RoleLayout.class))).willReturn(roleLayout);

		mockMvc.perform(
				post("/role_layout").content(mapper.writeValueAsString(roleLayout)).contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest());

	}

	@DisplayName("Test to failure of post incase of moduleId being empty in layout")
	@Test
	public void testRoleLayoutLayoutModuleNull() throws Exception {

		List<String> columnsShow = new ArrayList<String>();
		columnsShow.add("column1");
		columnsShow.add("Column2");
		List<Condition> conditions = new ArrayList<Condition>();
		OrderBy orderBy = new OrderBy("column1", "Test1");
		Tab layoutModule = new Tab("tab id","Module id", columnsShow, orderBy, conditions);
		List<Tab> modules = new ArrayList<Tab>();
		modules.add(layoutModule);

		RoleLayout roleLayout = new RoleLayout("", "company id", "Admin", "Test Layout", "Test desc", true, modules);

		// PREPARE STUB
		given(roleLayoutApi.postRoleLayout(any(RoleLayout.class))).willReturn(roleLayout);

		mockMvc.perform(
				post("/role_layout").content(mapper.writeValueAsString(roleLayout)).contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest());

	}

	@DisplayName("Test to determine success of get single layout")
	@Test
	public void testRoleLayoutGetSuccess() throws Exception {
		List<String> columnsShow = new ArrayList<String>();
		columnsShow.add("column1");
		columnsShow.add("Column2");
		List<Condition> conditions = new ArrayList<Condition>();
		OrderBy orderBy = new OrderBy("column1", "Test1");
		Tab layoutModule = new Tab("tab id","Module id", columnsShow, orderBy, conditions);
		List<Tab> modules = new ArrayList<Tab>();
		modules.add(layoutModule);

		RoleLayout roleLayout = new RoleLayout("", "company id", "Admin", "Test Layout", "Test desc", false, modules);

		given(roleLayoutApi.getRoleLayout(any(String.class))).willReturn(roleLayout);

		String roleLayoutString = mockMvc.perform(get("/role_layout/layoutId").contentType(APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

		RoleLayout response = mapper.readValue(roleLayoutString, RoleLayout.class);
	}

	@DisplayName("Test to determine success of getAll layouts call")
	@Test
	public void testRoleLayoutGetAllSuccess() throws Exception {
		List<String> columnsShow = new ArrayList<String>();
		columnsShow.add("column1");
		columnsShow.add("Column2");
		List<Condition> conditions = new ArrayList<Condition>();
		OrderBy orderBy = new OrderBy("column1", "Test1");
		Tab layoutModule = new Tab("tab id","Module id", columnsShow, orderBy, conditions);
		List<Tab> modules = new ArrayList<Tab>();
		modules.add(layoutModule);

		RoleLayout roleLayout = new RoleLayout("", "company id", "Admin", "Test Layout", "Test desc", false, modules);

		List<RoleLayout> roleLayouts = new ArrayList<RoleLayout>();
		roleLayouts.add(roleLayout);
		Page<RoleLayout> pages = new PageImpl<RoleLayout>(roleLayouts);

		given(roleLayoutApi.getAllRoleLayouts(any(Pageable.class))).willReturn(pages);

		mockMvc.perform(get("/role_layouts").contentType(APPLICATION_JSON)).andExpect(status().isOk()).andReturn()
				.getResponse().getContentAsString();

	}

}
