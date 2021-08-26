package com.ngdesk.module.layout.mobile.dao;

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
import com.ngdesk.module.layouts.mobile.dao.EditMobileLayoutAPI;
import com.ngdesk.module.mobile.layout.dao.CreateEditMobileLayout;

import brave.Tracer;

@ExtendWith(SpringExtension.class)
@WebMvcTest(EditMobileLayoutAPI.class)
@ContextConfiguration(classes = { NgDeskModulesServiceApplicationTests.class })
public class editMobileLayoutTesting {
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private EditMobileLayoutAPI editMobileLayoutAPI;

	@MockBean
	AuthProxy authProxy;
	
	@MockBean
	Tracer tracer;


	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	public void editMobileLayoutPostSuccess() throws Exception {
		List<String> fields = new ArrayList<String>();
		fields.add("TEST_FIELD1");
		fields.add("TEST_FIELD2");

		CreateEditMobileLayout createEditMobileLayout = new CreateEditMobileLayout(null, "TEST_CREATEEDITLAYOUT",
				"TEST_TYPE", fields, "TEST_ROLE", null, null, null, null);

		// PREPARE STUB
		given(editMobileLayoutAPI.postLayout(any(CreateEditMobileLayout.class), any(String.class)))
				.willReturn(createEditMobileLayout);

		// PERFORM MOCK TEST
		String editMobileLayoutString = mockMvc
				.perform(post("/module_id/edit_Mobile_Layout")
						.content(mapper.writeValueAsString(createEditMobileLayout)).contentType(APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

		CreateEditMobileLayout response = mapper.readValue(editMobileLayoutString, CreateEditMobileLayout.class);
	}

	@Test
	public void testEditMobileLayoutPostNameNotEmpty() throws Exception {

		List<String> fields = new ArrayList<String>();
		fields.add("TEST_FIELD1");
		fields.add("TEST_FIELD2");
		CreateEditMobileLayout createEditMobileLayout = new CreateEditMobileLayout(null, null, "Test_Type", fields,
				"TEST_ROLE", null, null, null, null);

		// PREPARE STUB
		given(editMobileLayoutAPI.postLayout(any(CreateEditMobileLayout.class), any(String.class)))
				.willReturn(createEditMobileLayout);

		// PERFORM MOCK TEST
		mockMvc.perform(post("/module_id/edit_Mobile_Layout").content(mapper.writeValueAsString(createEditMobileLayout))
				.contentType(APPLICATION_JSON)).andExpect(status().isBadRequest());

	}

	@Test
	public void testEditMobileLayoutPostDiscriptionNotEmpty() throws Exception {

		List<String> fields = new ArrayList<String>();
		fields.add("TEST_FIELD1");
		fields.add("TEST_FIELD2");
		CreateEditMobileLayout createEditMobileLayout = new CreateEditMobileLayout(null, "TEST_CREATEEDITLAYOUT", null,
				fields, "TEST_ROLE", null, null, null, null);

		// PREPARE STUB
		given(editMobileLayoutAPI.postLayout(any(CreateEditMobileLayout.class), any(String.class)))
				.willReturn(createEditMobileLayout);

		// PERFORM MOCK TEST
		mockMvc.perform(post("/module_id/edit_Mobile_Layout").content(mapper.writeValueAsString(createEditMobileLayout))
				.contentType(APPLICATION_JSON)).andExpect(status().isBadRequest());

	}

	@Test
	public void testEditMobileLayoutGetOneSuccess() throws Exception {
		List<String> fields = new ArrayList<String>();
		fields.add("TEST_FIELD1");
		fields.add("TEST_FIELD2");
		CreateEditMobileLayout createEditMobileLayout = new CreateEditMobileLayout(null, "Test_CreateLayout",
				"Test_Type", fields, "TEST_ROLE", null, null, null, null);

		// PREPARE STUB
		given(editMobileLayoutAPI.getOneEditMobileLayout(any(String.class), any(String.class)))
				.willReturn(createEditMobileLayout);

// PERFORM MOCK TEST
		String editMobileLayoutString = mockMvc
				.perform(get("/module_id/edit_mobile_layout/layout_id")
						.content(mapper.writeValueAsString(createEditMobileLayout)).contentType(APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

		CreateEditMobileLayout response = mapper.readValue(editMobileLayoutString, CreateEditMobileLayout.class);
	}

}
