package com.ngdesk.sam.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.UnsupportedEncodingException;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.managers.AuthProxy;
import com.ngdesk.sam.ApplicationTest;
import com.ngdesk.sam.controllers.dao.Controller;
import com.ngdesk.sam.controllers.dao.ControllersAPI;
import com.ngdesk.sam.controllers.dao.SubApp;

import brave.Tracer;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ControllersAPI.class)
@ContextConfiguration(classes = { ApplicationTest.class })
public class ControllersAPITest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ControllersAPI controllerAPI;

	@MockBean
	AuthProxy authProxy;
	
	@MockBean
	Tracer tracer;

	private final ObjectMapper mapper = new ObjectMapper();

	@DisplayName("Test to determine if controller is posted successfully")
	@Test
	public void testControllerPostSuccess() throws UnsupportedEncodingException, JsonProcessingException, Exception {

		List<SubApp> subApps = new ArrayList<SubApp>();
		subApps.add(new SubApp("NAME", "Online", null, 1));
		Controller controller = new Controller(null, "Test Controllers", subApps, "Offline", null, null, 1);

		// PREPARE STUB
		given(controllerAPI.postController(any(Controller.class))).willReturn(controller);

		// PERFORM MOCK TEST
		String controllerResponse = mockMvc
				.perform(post("/controller").content(mapper.writeValueAsString(controller))
						.contentType(APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

		Controller response = mapper.readValue(controllerResponse, Controller.class);
	}

	@DisplayName("Test to determine failure of post incase of wrong status")
	@Test
	public void testControllerPostFailForStatus() throws Exception {

		List<SubApp> subApps = new ArrayList<SubApp>();
		subApps.add(new SubApp("NAME", "Online", null, 1));
		Controller controller = new Controller(null, "Test Controllers", subApps, "INVALID_STATUS", null, null, 1);

		// PREPARE STUB
		given(controllerAPI.postController(any(Controller.class))).willReturn(controller);

		// PERFORM MOCK TEST
		mockMvc.perform(
				post("/controller").content(mapper.writeValueAsString(controller)).contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@DisplayName("Test to determine failure of post incase of blank controller name")
	@Test
	public void testControllerPostFailForBlankName() throws Exception {

		List<SubApp> subApps = new ArrayList<SubApp>();
		subApps.add(new SubApp("NAME", "Online", null, 1));
		Controller controller = new Controller(null, null, subApps, "INVALID_STATUS", null, null, 1);

		// PREPARE STUB
		given(controllerAPI.postController(any(Controller.class))).willReturn(controller);

		// PERFORM MOCK TEST
		mockMvc.perform(
				post("/controller").content(mapper.writeValueAsString(controller)).contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@DisplayName("Test to determine failure of post")
	@Test
	public void testControllerPostFail() throws Exception {

		List<SubApp> subApps = new ArrayList<SubApp>();
		subApps.add(new SubApp("NAME", "STATUS", null, 1));

		Controller controller = new Controller(null, "Test Controllers", subApps, "STATUS", null, null, 1);

		// PREPARE STUB
		given(controllerAPI.postController(any(Controller.class))).willReturn(controller);

		// PERFORM MOCK TEST
		mockMvc.perform(
				post("/controller").content(mapper.writeValueAsString(controller)).contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@DisplayName("Test to determine success for get All call")
	@Test
	public void testControllerGetAllSuccess() throws Exception {

		List<SubApp> subApps = new ArrayList<SubApp>();
		subApps.add(new SubApp("NAME", "Online", null, 1));
		Controller controller = new Controller(null, "Test Controllers", subApps, "Online", null, null, 1);

		List<Controller> controllers = new ArrayList<Controller>();
		controllers.add(controller);
		Page<Controller> pages = new PageImpl<>(controllers);

		// PREPARE STUB
		given(controllerAPI.getControllers(any(Pageable.class), any(String.class))).willReturn(pages);

		// PERFORM MOCK TEST
		mockMvc.perform(get("/controllers").contentType(APPLICATION_JSON)).andExpect(status().isOk()).andReturn()
				.getResponse().getContentAsString();
	}

	@DisplayName("Test to determine get of individual controller")
	@Test
	public void testControllerGetSuccess() throws Exception {

		List<SubApp> subApps = new ArrayList<SubApp>();
		subApps.add(new SubApp("NAME", "Online", null, 1));
		Controller controller = new Controller(null, "Test Controllers", subApps, "Offline", null, null, 1);

		// PREPARE STUB
		given(controllerAPI.getControllerById(any(String.class))).willReturn(controller);

		// PERFORM MOCK TEST
		String controllerString = mockMvc
				.perform(get("/controller/controller_id").contentType(mapper.writeValueAsString(controller))
						.contentType(APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

		Controller response = mapper.readValue(controllerString, Controller.class);
	}

}
