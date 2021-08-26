package com.ngdesk.module.layouts.mobile.dao;

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
import com.ngdesk.module.mobile.layout.dao.CreateEditMobileLayout;

import brave.Tracer;

@ExtendWith(SpringExtension.class)
@WebMvcTest(CreateMobileLayoutAPI.class)
@ContextConfiguration(classes = { NgDeskModulesServiceApplicationTests.class })
public class MobileLayoutAPITesting {
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private CreateMobileLayoutAPI createMobileLayoutAPI;

	@MockBean
	AuthProxy authProxy;

	@MockBean
	Tracer tracer;

	
	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	public void testCreateMobileLayoutPostSuccess() throws Exception {

		List<String> fields = new ArrayList<String>();
		fields.add(null);
		fields.add(null);
		CreateEditMobileLayout layout = new CreateEditMobileLayout(null, "Test CreateMobileLayout", "Test Description",
				fields, "Test Role", null, null, null, null);

		// PREPARE STUB
		given(createMobileLayoutAPI.postMobileLayout(any(CreateEditMobileLayout.class), any(String.class)))
				.willReturn(layout);

		// PERFORM MOCK TEST
		String createMobileLayoutString = mockMvc
				.perform(post("/module_id/create_mobile_layouts").content(mapper.writeValueAsString(layout))
						.contentType(APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

		CreateEditMobileLayout response = mapper.readValue(createMobileLayoutString, CreateEditMobileLayout.class);
	}

	@Test
	public void testCreateMobileLayoutPostNameNotNull() throws Exception {

		List<String> fields = new ArrayList<String>();
		fields.add(null);
		fields.add(null);
		CreateEditMobileLayout layout = new CreateEditMobileLayout(null, null, "Test Description", fields, "Test Role",
				null, null, null, null);

		// PREPARE STUB
		given(createMobileLayoutAPI.postMobileLayout(any(CreateEditMobileLayout.class), any(String.class)))
				.willReturn(layout);

		// PERFORM MOCK TEST
		mockMvc.perform(post("/module_id/create_mobile_layouts").content(mapper.writeValueAsString(layout))
				.contentType(APPLICATION_JSON)).andExpect(status().isBadRequest());
	}

	@Test
	public void testCreateMobileLayoutPostDescriptionNotNull() throws Exception {

		List<String> fields = new ArrayList<String>();
		fields.add(null);
		fields.add(null);
		CreateEditMobileLayout layout = new CreateEditMobileLayout(null, null, "Test Description", fields, "Test Role",
				null, null, null, null);

		// PREPARE STUB
		given(createMobileLayoutAPI.postMobileLayout(any(CreateEditMobileLayout.class), any(String.class)))
				.willReturn(layout);

		// PERFORM MOCK TEST
		mockMvc.perform(post("/module_id/create_mobile_layouts").content(mapper.writeValueAsString(layout))
				.contentType(APPLICATION_JSON)).andExpect(status().isBadRequest());
	}

	@Test
	public void testCreateMobileLayoutGetSuccess() throws Exception {

		List<String> fields = new ArrayList<String>();
		fields.add(null);
		fields.add(null);
		CreateEditMobileLayout layout = new CreateEditMobileLayout(null, "Test CreateMobileLayout", null, fields,
				"Test Role", null, null, null, null);

		// PREPARE STUB
		given(createMobileLayoutAPI.getOneMobileLayout(any(String.class), any(String.class))).willReturn(layout);

		// PERFORM MOCK TEST
		String createMobileLayoutString = mockMvc
				.perform(get("/module_id/create_mobile_layouts/id").content(mapper.writeValueAsString(layout))
						.contentType(APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

		CreateEditMobileLayout response = mapper.readValue(createMobileLayoutString, CreateEditMobileLayout.class);
	}
}