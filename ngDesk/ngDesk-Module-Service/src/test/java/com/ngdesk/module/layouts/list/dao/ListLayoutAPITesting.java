package com.ngdesk.module.layouts.list.dao;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import com.ngdesk.module.NgDeskModulesServiceApplicationTests;
import com.ngdesk.module.layout.dao.Column;
import com.ngdesk.module.layout.dao.Condition;
import com.ngdesk.module.layout.dao.ListLayout;
import com.ngdesk.module.layout.dao.OrderBy;

import brave.Tracer;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ListLayoutAPI.class)
@ContextConfiguration(classes = { NgDeskModulesServiceApplicationTests.class })
public class ListLayoutAPITesting {
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ListLayoutAPI listLayoutAPI;

	@MockBean
	AuthProxy authProxy;
	
	@MockBean
	Tracer tracer;


	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	public void testListLayoutPostSuccess() throws Exception {

		List<Condition> condition = new ArrayList<Condition>();
		condition.add(null);
		condition.add(null);
		condition.add(null);
		condition.add(null);
		ListLayout listLayout = new ListLayout(null, "Agent", "list layout of agents", null, null, null,
				new OrderBy(null, null), new Column(null), condition, null, null, null, null);

		// PREPARE STUB
		given(listLayoutAPI.postListLayout(any(ListLayout.class), any(String.class))).willReturn(listLayout);

		// PERFORM MOCK TEST
		String listLayoutString = mockMvc
				.perform(post("/modules/module_id/list_layout").content(mapper.writeValueAsString(listLayout))
						.contentType(APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

		ListLayout response = mapper.readValue(listLayoutString, ListLayout.class);
	}

	@Test
	public void testListLayoutPostNameNotNull() throws Exception {

		List<Condition> condition = new ArrayList<Condition>();
		condition.add(null);
		condition.add(null);
		condition.add(null);
		condition.add(null);
		ListLayout listLayout = new ListLayout(null, null, "list layout of agents", null, null, null,
				new OrderBy(null, null), new Column(null), condition, null, null, null, null);

		// PREPARE STUB
		given(listLayoutAPI.postListLayout(any(ListLayout.class), any(String.class))).willReturn(listLayout);

		// PERFORM MOCK TEST
		mockMvc.perform(post("/modules/module_id/list_layout").content(mapper.writeValueAsString(listLayout))
				.contentType(APPLICATION_JSON)).andExpect(status().isBadRequest());

	}

	@Test
	public void testListLayoutPostDescriptionNotNull() throws Exception {

		List<Condition> condition = new ArrayList<Condition>();
		condition.add(null);
		condition.add(null);
		condition.add(null);
		condition.add(null);
		ListLayout listLayout = new ListLayout(null, "Agent", null, null, null, null, new OrderBy(null, null),
				new Column(null), condition, null, null, null, null);

		// PREPARE STUB
		given(listLayoutAPI.postListLayout(any(ListLayout.class), any(String.class))).willReturn(listLayout);

		// PERFORM MOCK TEST
		mockMvc.perform(post("/modules/module_id/list_layout").content(mapper.writeValueAsString(listLayout))
				.contentType(APPLICATION_JSON)).andExpect(status().isBadRequest());

	}

	@Test
	public void testListLayoutGetSuccess() throws Exception {

		List<Condition> condition = new ArrayList<Condition>();
		condition.add(null);
		condition.add(null);
		condition.add(null);
		condition.add(null);
		ListLayout listLayout = new ListLayout(null, "Agent", "list layout of agents", null, null, null,
				new OrderBy(null, null), new Column(null), condition, null, null, null, null);

		// PREPARE STUB
		given(listLayoutAPI.getListLayout(any(String.class), any(String.class))).willReturn(listLayout);

		// PERFORM MOCK TEST
		String listLayoutString = mockMvc
				.perform(get("/modules/module_id/list_layout/layout_id").content(mapper.writeValueAsString(listLayout))
						.contentType(APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

		ListLayout response = mapper.readValue(listLayoutString, ListLayout.class);
	}

}